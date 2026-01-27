package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException
import javax.inject.Inject

class PostRepositoryNetwork @Inject constructor(
    private val postDao: PostDao,
    private val apiService: ApiService,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb,
): PostRepository {

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = 5, enablePlaceholders = false),
        pagingSourceFactory = { postDao.getPagingSource() },
        remoteMediator = PostRemoteMediator(
            apiService = apiService,
            postDao = postDao,
            postRemoteKeyDao = postRemoteKeyDao,
            appDb = appDb,
            )
    ).flow
        .map { it.map(PostEntity::toDto) }

    override suspend fun likeById(id: Long): Post {
        val posts = postDao.getAll().first().map { it.toDto() }
        val originalPost = posts.find { it.id == id }
        val wasLiked = originalPost?.likedByMe ?: false

        try {
            postDao.likeById(id)

            val post = if (wasLiked) {
                apiService.dislikeById(id)
            } else {
                apiService.likeById(id)
            }

            val currentPost = postDao.getAll().first().find { it.id == id }
            postDao.insert(PostEntity.fromDto(post.copy(showed = currentPost?.showed ?: true)))
            return post

        } catch (e: Exception) {
            if (originalPost != null) {
                postDao.insert(PostEntity.fromDto(originalPost))
            }
            throw AppError.from(e)
        }
    }

    override suspend fun shareById(id: Long): Post {
        val posts = postDao.getAll().first().map { it.toDto() }
        val originalPost = posts.find { it.id == id }

        try {
            postDao.shareById(id)
            val post = apiService.shareById(id)
            val currentPost = postDao.getAll().first().find { it.id == id }
            postDao.insert(PostEntity.fromDto(post.copy(showed = currentPost?.showed ?: true)))
            return post
        } catch (e: Exception) {
            if (originalPost != null) {
                postDao.insert(PostEntity.fromDto(originalPost))
            }
            throw AppError.from(e)
        }
    }

    override suspend fun removeById(id: Long) {
        val posts = postDao.getAll().first().map { it.toDto() }
        val originalPost = posts.find { it.id == id }

        try {
            postDao.removeById(id)
            apiService.deleteById(id)
        } catch (e: Exception) {
            if (originalPost != null) {
                postDao.insert(PostEntity.fromDto(originalPost))
            }
            throw AppError.from(e)
        }
    }

    override suspend fun save(post: Post): Post {
        try {
            val postWithId = apiService.save(post)
            postDao.insert(PostEntity.fromDto(postWithId.copy(showed = true)))
            return postWithId
        } catch (e: Exception) {
            e.printStackTrace()
            throw AppError.from(e)
        }
    }

    override suspend fun getUnshowed() {
        try {
            postDao.showAll()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveWithAttachment(post: Post, upload: MediaUpload) {
        try {
            val media = upload(upload)

            val postWithAttachment = post.copy(
                attachment = Attachment(
                    url = media.id,
                    type = AttachmentType.IMAGE,
                    description = "Image"
                )
            )


            val savedPost = save(postWithAttachment)

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun upload(upload: MediaUpload): Media {
        try {

            val mediaPart = MultipartBody.Part.createFormData(
                "file",
                upload.file.name,
                upload.file.asRequestBody()
            )

            val result = apiService.upload(mediaPart)

            return result
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }

    override suspend fun updateUser(login: String, pass: String): AuthState {
        try {
            val authState = apiService.updateUser(login, pass)
            return authState
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}