package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import ru.netology.nmedia.dto.Post
import java.util.concurrent.TimeUnit

class PostRepositoryNetwork(): PostRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999/"
        val jsonType = "application/json".toMediaType()
    }

    override fun getAll(): List<Post> {
        val request = Request.Builder()
            .url("${BASE_URL}api/slow/posts")
            .build()

        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("Failed to load posts: ${response.code}")
            }
            val textBody = response.body?.string() ?: throw RuntimeException("body is null")
            gson.fromJson(textBody, type)
        }
    }

    override fun save(post: Post): Post {
        val request = Request.Builder()
            .url("${BASE_URL}api/slow/posts")
            .post(gson.toJson(post).toRequestBody(jsonType))
            .build()

        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("Failed to save post: ${response.code}")
            }
            val textBody = response.body?.string() ?: throw RuntimeException("body is null")
            gson.fromJson(textBody, Post::class.java)
        }
    }

    override fun likeById(id: Long): Post = executeLikeRequest(id, true)
    override fun unlikeById(id: Long): Post = executeLikeRequest(id, false)

    private fun executeLikeRequest(id: Long, like: Boolean): Post {
        val request = if (like) {
            Request.Builder()
                .url("${BASE_URL}api/slow/posts/$id/likes")
                .post("".toRequestBody(jsonType))
                .build()
        } else {
            Request.Builder()
                .url("${BASE_URL}api/slow/posts/$id/likes")
                .delete()
                .build()
        }

        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("Failed to ${if (like) "like" else "unlike"} post: ${response.code}")
            }
            val textBody = response.body?.string() ?: throw RuntimeException("body is null")
            gson.fromJson(textBody, Post::class.java)
        }
    }

    override fun shareById(id: Long): Post {
        val request = Request.Builder()
            .url("${BASE_URL}api/slow/posts/$id/shares")
            .post("".toRequestBody(jsonType))
            .build()

        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("Failed to share post: ${response.code}")
            }
            val textBody = response.body?.string() ?: throw RuntimeException("body is null")
            gson.fromJson(textBody, Post::class.java)
        }
    }

    override fun removeById(id: Long) {
        val request = Request.Builder()
            .url("${BASE_URL}api/slow/posts/$id")
            .delete()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("Failed to remove post: ${response.code}")
            }
        }
    }

    override fun getAllAsync(callback: PostRepository.PostCallback<List<Post>>) {
        val request = Request.Builder()
            .url("${BASE_URL}api/slow/posts")
            .build()

        client.newCall(request).enqueue(createCallback(callback) { response ->
            val textBody = response.body?.string() ?: throw RuntimeException("body is null")
            gson.fromJson(textBody, type)
        })
    }

    override fun likeByIdAsync(id: Long, likedByMe: Boolean, callback: PostRepository.PostCallback<Post>) {
        val request = if (likedByMe) {
            Request.Builder()
                .url("${BASE_URL}api/slow/posts/$id/likes")
                .delete()
                .build()
        } else {
            Request.Builder()
                .url("${BASE_URL}api/slow/posts/$id/likes")
                .post("".toRequestBody(jsonType))
                .build()
        }

        client.newCall(request).enqueue(createCallback(callback) { response ->
            val textBody = response.body?.string() ?: throw RuntimeException("body is null")
            gson.fromJson(textBody, Post::class.java)
        })
    }

    override fun shareByIdAsync(id: Long, callback: PostRepository.PostCallback<Post>) {
        val request = Request.Builder()
            .url("${BASE_URL}api/slow/posts/$id/shares")
            .post("".toRequestBody(jsonType))
            .build()

        client.newCall(request).enqueue(createCallback(callback) { response ->
            val textBody = response.body?.string() ?: throw RuntimeException("body is null")
            gson.fromJson(textBody, Post::class.java)
        })
    }

    override fun removeByIdAsync(id: Long, callback: PostRepository.PostCallback<Unit>) {
        val request = Request.Builder()
            .url("${BASE_URL}api/slow/posts/$id")
            .delete()
            .build()

        client.newCall(request).enqueue(createCallback(callback) { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("Failed to remove post: ${response.code}")
            }
            Unit
        })
    }

    override fun saveAsync(post: Post, callback: PostRepository.PostCallback<Post>) {
        val request = Request.Builder()
            .url("${BASE_URL}api/slow/posts")
            .post(gson.toJson(post).toRequestBody(jsonType))
            .build()

        client.newCall(request).enqueue(createCallback(callback) { response ->
            val textBody = response.body?.string() ?: throw RuntimeException("body is null")
            gson.fromJson(textBody, Post::class.java)
        })
    }

    private inline fun <T> createCallback(
        callback: PostRepository.PostCallback<T>,
        crossinline parser: (Response) -> T
    ): Callback {
        return object : Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    if (!response.isSuccessful) {
                        val error = when (response.code) {
                            404 -> RuntimeException("Post not found")
                            500 -> RuntimeException("Server error")
                            else -> RuntimeException("Error: ${response.code}")
                        }
                        callback.onError(error)
                        return
                    }

                    val result = parser(response)
                    callback.onSuccess(result)
                } catch (e: Exception) {
                    callback.onError(e)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }
        }
    }
}