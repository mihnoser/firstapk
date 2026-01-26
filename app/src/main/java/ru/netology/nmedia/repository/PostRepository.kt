package ru.netology.nmedia.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val data: Flow<PagingData<Post>>
//    fun getNewer(id: Long): Flow<Int>
//    suspend fun getAll()
    suspend fun likeById(id: Long): Post
    suspend fun shareById(id: Long): Post
    suspend fun removeById(id: Long)
    suspend fun save(post: Post): Post
    suspend fun getUnshowed()
    suspend fun saveWithAttachment(post: Post, upload: MediaUpload)
    suspend fun upload(upload: MediaUpload): Media
    suspend fun updateUser(login : String, pass : String) : AuthState
}