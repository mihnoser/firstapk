package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(): List<Post>
    fun likeById(id: Long): Post
    fun unlikeById(id: Long): Post
    fun shareById(id: Long): Post
    fun removeById(id: Long)
    fun save(post: Post): Post

    fun getAllAsync(callback: GetAllCallback)
    fun likeByIdAsync(id: Long, callback: PostCallback)
    fun unlikeByIdAsync(id: Long, callback: PostCallback)
    fun shareByIdAsync(id: Long, callback: PostCallback)
    fun removeByIdAsync(id: Long, callback: RemoveCallback)
    fun saveAsync(post: Post, callback: PostCallback)

    interface GetAllCallback {
        fun onSuccess(posts: List<Post>)
        fun onError(e: Exception)
    }

    interface PostCallback {
        fun onSuccess(post: Post)
        fun onError(e: Exception)
    }

    interface RemoveCallback {
        fun onSuccess()
        fun onError(e: Exception)
    }
}