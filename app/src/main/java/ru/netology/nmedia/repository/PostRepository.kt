package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val data: Flow<List<Post>>
    fun getNewer(id: Long): Flow<Int>
    suspend fun getAll()
    suspend fun likeById(id: Long): Post
    suspend fun shareById(id: Long): Post
    suspend fun removeById(id: Long)
    suspend fun save(post: Post): Post
    suspend fun getUnshowed()
    suspend fun saveWithAttachment(post: Post, upload: MediaUpload)
    suspend fun upload(upload: MediaUpload): Media
    suspend fun updateUser(login : String, pass : String) : AuthState

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