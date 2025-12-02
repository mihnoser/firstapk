package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity

class PostRepositoryNetwork(private val dao: PostDao): PostRepository {
    override val data: LiveData<List<Post>> = dao.getAll().map {
        it.map(PostEntity::toDto)
    }

    override suspend fun getAllAsync() {
        val posts = PostApi.service.getAll()
        dao.insert(posts.map(PostEntity::fromDto))
    }

    override suspend fun likeById(id: Long): Post {
        val posts = data.value ?: emptyList()
        val originalPost = posts.find { it.id == id }
        val wasLiked = originalPost?.likedByMe ?: false

        try {
            dao.likeById(id)

            val post = if (wasLiked) {
                PostApi.service.dislikeById(id)
            } else {
                PostApi.service.likeById(id)
            }

            return post

        } catch (e: Exception) {
            dao.likeById(id)
            throw e
        }
    }

    override suspend fun shareById(id: Long): Post {
        val posts = data.value ?: emptyList()
        val originalPost = posts.find { it.id == id }

        try {
            dao.shareById(id)
            val post = PostApi.service.shareById(id)
            return post
        } catch (e: Exception) {
            if (originalPost != null) {
                dao.insert(PostEntity.fromDto(originalPost))
            }
            throw e
        }
    }

    override suspend fun removeById(id: Long) {
        val posts = data.value ?: emptyList()
        val originalPost = posts.find { it.id == id }

        try {
            dao.removeById(id)
            PostApi.service.deleteById(id)
        } catch (e: Exception) {
            if (originalPost != null) {
                dao.insert(PostEntity.fromDto(originalPost))
            }
            throw e
        }
    }

    override suspend fun save(post: Post): Post {
        try {
            val postWithId = PostApi.service.save(post)
            dao.insert(PostEntity.fromDto(postWithId))
            return postWithId
        } catch (e: Exception) {
            throw e
        }
    }
}