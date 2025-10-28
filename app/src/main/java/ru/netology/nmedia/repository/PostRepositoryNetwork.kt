package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
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
            .url(url = "${BASE_URL}api/slow/posts")
            .build()

        val call = client.newCall(request)

        val response = call.execute()

        val textBody = response.body.string()

        return gson.fromJson(textBody, type)
    }

    override fun save(post: Post): Post {
        val request = Request.Builder()
            .url(url = "${BASE_URL}api/slow/posts")
            .post(gson.toJson(post).toRequestBody(jsonType))
            .build()

        val call = client.newCall(request)

        val response = call.execute()

        val textBody = response.body.string()

        return gson.fromJson(textBody, Post::class.java)
    }

    override fun likeById(id: Long): Post {
        val request = Request.Builder()
            .url("${BASE_URL}api/slow/posts/$id/likes")
            .post("".toRequestBody(jsonType))
            .build()

        return client.newCall(request).execute().use { response ->
            val textBody = response.body?.string() ?: throw RuntimeException("Empty response")
            if (!response.isSuccessful) {
                throw RuntimeException("Failed to like post: ${response.code}")
            }
            gson.fromJson(textBody, Post::class.java)
        }
    }

    override fun unlikeById(id: Long): Post {
        val request = Request.Builder()
            .url("${BASE_URL}api/slow/posts/$id/likes")
            .delete()
            .build()

        return client.newCall(request).execute().use { response ->
            val textBody = response.body?.string() ?: throw RuntimeException("Empty response")
            if (!response.isSuccessful) {
                throw RuntimeException("Failed to unlike post: ${response.code}")
            }
            gson.fromJson(textBody, Post::class.java)
        }
    }

    override fun shareById(id: Long): Post {
        val request = Request.Builder()
            .url("${BASE_URL}api/slow/posts/$id/shares")
            .post("".toRequestBody(jsonType))
            .build()

        return client.newCall(request).execute().use { response ->
            val textBody = response.body?.string() ?: throw RuntimeException("Empty response")
            if (!response.isSuccessful) {
                throw RuntimeException("Failed to share post: ${response.code}")
            }
            gson.fromJson(textBody, Post::class.java)
        }
    }

    override fun removeById(id: Long) {
        val request = Request.Builder()
            .url(url = "${BASE_URL}api/slow/posts/$id")
            .delete()
            .build()

        val call = client.newCall(request)

        val response = call.execute()

        if (!response.isSuccessful) {
            throw RuntimeException("Failed to delete post: ${response.code}")
        }
    }
}