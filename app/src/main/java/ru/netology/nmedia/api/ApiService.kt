package ru.netology.nmedia.api

import okhttp3.MultipartBody
import retrofit2.http.*
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.PushToken

interface ApiService {
    @GET("posts")
    suspend fun getAll(): List<Post>

    @GET("posts/latest")
    suspend fun getLatest(@Query("count") count: Int): List<Post>

    @GET("posts/{id}")
    suspend fun getById(@Path("id") id: Long): Post

    @DELETE("posts/{id}")
    suspend fun deleteById(@Path("id") id: Long)

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Post

    @DELETE("posts/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Long): Post

    @POST("posts/{id}/shares")
    suspend fun shareById(@Path("id") id: Long): Post

    @GET("posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): List<Post>

    @GET("posts/{id}/before")
    suspend fun getBefore(@Path("id") id: Long, @Query("count") count: Int): List<Post>

    @GET("posts/{id}/after")
    suspend fun getAfter(@Path("id") id: Long, @Query("count") count: Int): List<Post>

    @Multipart
    @POST("media")
    suspend fun upload(@Part media: MultipartBody.Part): Media

    @POST("posts")
    suspend fun save(@Body post: Post): Post

    @POST("users/push-tokens")
    suspend fun sendPushToken(@Body token: PushToken)

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun updateUser(@Field("login") login: String, @Field("pass") pass: String): AuthState

}