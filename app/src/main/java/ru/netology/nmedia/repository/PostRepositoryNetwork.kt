package ru.netology.nmedia.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException

class PostRepositoryNetwork(private val dao: PostDao): PostRepository {
    override val data: Flow<List<Post>> = dao.getAll()
        .map { posts -> posts.map { it.toDto() } }
        .catch { e -> throw AppError.from(e) }

    override suspend fun getAll() {
        try {
            val posts = PostApi.service.getAll()
            dao.insert(posts.map { PostEntity.fromDto(it.copy(showed = true)) })
        } catch (e: Exception) {
            throw AppError.from(e)
        }

    }

    override suspend fun likeById(id: Long): Post {
        val posts = dao.getAll().first().map { it.toDto() }
        val originalPost = posts.find { it.id == id }
        val wasLiked = originalPost?.likeByMe ?: false

        try {
            dao.likeById(id)

            val post = if (wasLiked) {
                PostApi.service.dislikeById(id)
            } else {
                PostApi.service.likeById(id)
            }

            val currentPost = dao.getAll().first().find { it.id == id }
            dao.insert(PostEntity.fromDto(post.copy(showed = currentPost?.showed ?: true)))
            return post

        } catch (e: Exception) {
            if (originalPost != null) {
                dao.insert(PostEntity.fromDto(originalPost))
            }
            throw AppError.from(e)
        }
    }

    override suspend fun shareById(id: Long): Post {
        val posts = dao.getAll().first().map { it.toDto() }
        val originalPost = posts.find { it.id == id }

        try {
            dao.shareById(id)
            val post = PostApi.service.shareById(id)
            val currentPost = dao.getAll().first().find { it.id == id }
            dao.insert(PostEntity.fromDto(post.copy(showed = currentPost?.showed ?: true)))
            return post
        } catch (e: Exception) {
            if (originalPost != null) {
                dao.insert(PostEntity.fromDto(originalPost))
            }
            throw AppError.from(e)
        }
    }

    override suspend fun removeById(id: Long) {
        val posts = dao.getAll().first().map { it.toDto() }
        val originalPost = posts.find { it.id == id }

        try {
            dao.removeById(id)
            PostApi.service.deleteById(id)
        } catch (e: Exception) {
            if (originalPost != null) {
                dao.insert(PostEntity.fromDto(originalPost))
            }
            throw AppError.from(e)
        }
    }

    override suspend fun save(post: Post): Post {
        try {
            val postWithId = PostApi.service.save(post)
            dao.insert(PostEntity.fromDto(postWithId.copy(showed = true)))
            return postWithId
        } catch (e: Exception) {
            throw AppError.from(e)
        }
    }

    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val newerPosts = PostApi.service.getNewer(id)
            dao.insert(newerPosts.map { PostEntity.fromDto(it.copy(showed = false)) })
            emit(newerPosts.size)
        }
    }.catch { e -> throw AppError.from(e) }

    override suspend fun getUnshowed() {
        try {
            dao.showAll()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}