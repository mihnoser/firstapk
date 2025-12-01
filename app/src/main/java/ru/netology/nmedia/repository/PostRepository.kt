package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val data: LiveData<List<Post>>
    suspend fun getAllAsync()
    suspend fun likeById(id: Long): Post
    suspend fun shareById(id: Long): Post
    suspend fun removeById(id: Long)
    suspend fun save(post: Post): Post

    /*fun handleError(response: Response<*>, defaultMessage: String = "Network error"): Throwable {
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
    }*/
}