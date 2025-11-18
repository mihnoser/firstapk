package ru.netology.nmedia.repository

import retrofit2.Response
import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(): List<Post>
    fun likeById(id: Long): Post
    fun unlikeById(id: Long): Post
    fun shareById(id: Long): Post
    fun removeById(id: Long)
    fun save(post: Post): Post

    fun getAllAsync(callback: GetAllCallback)
    fun likeByIdAsync(id: Long, likedByMe: Boolean, callback: PostCallback<Post>)
    fun shareByIdAsync(id: Long, callback: PostCallback<Post>)
    fun removeByIdAsync(id: Long, callback: PostCallback<Unit>)
    fun saveAsync(post: Post, callback: PostCallback<Post>)

    interface PostCallback<T> {
        fun onSuccess(result: T)
        fun onError(e: Throwable)
    }

    interface GetAllCallback {
        fun onSuccess(posts: List<Post>) {}
        fun onError(e: Throwable)
    }

    fun handleError(response: Response<*>, defaultMessage: String = "Network error"): Throwable {
        return when (response.code()) {
            400 -> RuntimeException("Bad request")
            401 -> RuntimeException("Unauthorized")
            403 -> RuntimeException("Forbidden")
            404 -> RuntimeException("Not found")
            408 -> RuntimeException("Request timeout")
            500 -> RuntimeException("Internal server error")
            502 -> RuntimeException("Bad gateway")
            503 -> RuntimeException("Service unavailable")
            else -> RuntimeException("$defaultMessage: ${response.code()}")
        }
    }
}