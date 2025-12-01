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
        val posts = dao.getAll().value ?: emptyList()
        val originalPostEntity = posts.find { it.id == id }
        val wasLiked = originalPostEntity?.likeByMe ?: false

        try {
            dao.likeById(id)

            val post = if (wasLiked) {
                PostApi.service.dislikeById(id)
            } else {
                PostApi.service.likeById(id)
            }

            return post
        } catch (e: Exception) {
            if (originalPostEntity != null) {
                dao.insert(originalPostEntity)
            }
            throw e
        }
    }

    override suspend fun shareById(id: Long): Post {
        try {
            dao.shareById(id)
            val post = PostApi.service.shareById(id)
            dao.insert(PostEntity.fromDto(post))
            return post
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            dao.removeById(id)
            PostApi.service.deleteById(id)
        } catch (e: Exception) {
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