package ru.netology.nmedia.repository

import com.google.firebase.appdistribution.gradle.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.Api
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException

class PostRepositoryNetwork(private val dao: PostDao): PostRepository {

    override val data = dao.getAll()
        .map(List<PostEntity>::toDto)
        .flowOn(Dispatchers.Default)

    override suspend fun getAll() {
        try {
            val posts = Api.service.getAll()
            dao.insert(posts.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeById(id: Long): Post {
        val posts = dao.getAll().first().map { it.toDto() }
        val originalPost = posts.find { it.id == id }
        val wasLiked = originalPost?.likeByMe ?: false

        try {
            dao.likeById(id)

            val post = if (wasLiked) {
                Api.service.dislikeById(id)
            } else {
                Api.service.likeById(id)
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
            val post = Api.service.shareById(id)
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
            Api.service.deleteById(id)
        } catch (e: Exception) {
            if (originalPost != null) {
                dao.insert(PostEntity.fromDto(originalPost))
            }
            throw AppError.from(e)
        }
    }

    override suspend fun save(post: Post): Post {
        try {
            println("DEBUG: Отправляем пост: $post")
            println("DEBUG: Attachment в отправляемом посте: ${post.attachment}")

            val postWithId = Api.service.save(post)

            println("DEBUG: Ответ сервера при сохранении поста: $postWithId")
            println("DEBUG: Attachment в ответе сервера: ${postWithId.attachment}")
            println("DEBUG: Есть attachment в ответе? ${postWithId.attachment != null}")

            dao.insert(PostEntity.fromDto(postWithId.copy(showed = true)))
            println("=== DEBUG save: Успешно ===")

            return postWithId
        } catch (e: Exception) {
            println("=== DEBUG save: ОШИБКА ===")
            println("DEBUG: ${e.javaClass.simpleName}: ${e.message}")
            e.printStackTrace()
            throw AppError.from(e)
        }
    }

//    override suspend fun save(post: Post): Post {
//        try {
//            val postWithId = Api.service.save(post)
//            dao.insert(PostEntity.fromDto(postWithId.copy(showed = true)))
//            return postWithId
//        } catch (e: Exception) {
//            throw AppError.from(e)
//        }
//    }

    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            try {
                val newerPosts = Api.service.getNewer(id)
                dao.insert(newerPosts.toEntity())
                emit(newerPosts.size)
            } catch (e: Exception) {
                emit(0)
            }
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

    override suspend fun getUnshowed() {
        try {
            dao.showAll()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveWithAttachment(post: Post, upload: MediaUpload) {
        try {
            val media = upload(upload)
            println("DEBUG: Media загружено: id=${media.id}")

            val postWithAttachment = post.copy(
                attachment = Attachment(
                    url = media.id,
                    type = AttachmentType.IMAGE,
                    description = "Image"
                )
            )

            println("DEBUG: Пост перед сохранением: $postWithAttachment")
            println("DEBUG: Attachment типа: ${postWithAttachment.attachment?.type?.name}")

            val savedPost = save(postWithAttachment)
            println("DEBUG: Пост сохранен: $savedPost")
            println("DEBUG: Attachment в сохраненном посте: ${savedPost.attachment}")

        } catch (e: Exception) {
            println("=== DEBUG saveWithAttachment: ОШИБКА ===")
            println("DEBUG: ${e.javaClass.simpleName}: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

//    override suspend fun saveWithAttachment(post: Post, upload: MediaUpload) {
//        try {
//            val media = upload(upload)
//            val postWithAttachment = post.copy(
//                attachment = Attachment(
//                    url = media.id,
//                    type = AttachmentType.IMAGE,
//                    description = "Image"
//                )
//            )
//            save(postWithAttachment)
//        } catch (e: AppError) {
//            throw e
//        } catch (e: IOException) {
//            throw NetworkError
//        } catch (e: Exception) {
//            throw UnknownError
//        }
//    }

    override suspend fun upload(upload: MediaUpload): Media {
        try {
            println("DEBUG: Путь к файлу: ${upload.file.absolutePath}")
            println("DEBUG: Размер файла: ${upload.file.length()} байт")

            val mediaPart = MultipartBody.Part.createFormData(
                "file",
                upload.file.name,
                upload.file.asRequestBody()
            )

            println("DEBUG: Отправляем запрос upload...")
            val result = Api.loggingService.upload(mediaPart)
            println("DEBUG: Результат upload: $result")
            println("=== DEBUG upload: Успешно ===")

            return result
        } catch (e: IOException) {
            println("=== DEBUG upload: IOException ===")
            println("DEBUG: ${e.message}")
            throw NetworkError
        } catch (e: Exception) {
            println("=== DEBUG upload: Exception ===")
            println("DEBUG: ${e.javaClass.simpleName}: ${e.message}")
            e.printStackTrace()
            throw UnknownError
        }
    }

//    override suspend fun upload(upload: MediaUpload): Media {
//        try {
//            val mediaPart = MultipartBody.Part.createFormData(
//                "file", upload.file.name, upload.file.asRequestBody()
//            )
//
//            return Api.service.upload(mediaPart)
//        } catch (e: IOException) {
//            throw NetworkError
//        } catch (e: Exception) {
//            throw UnknownError
//        }
//    }
}

private fun ApiService.upload(mediaPart: MultipartBody.Part) {}

private fun ApiService.save(post: Post) {}
