package ru.netology.nmedia.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.netology.nmedia.auth.AppAuth
import java.util.concurrent.TimeUnit

private val authInterceptor = Interceptor { chain ->
    val token = AppAuth.getInstance().authStateFlow.value.token

    val newRequest = if (!token.isNullOrEmpty()) {
        chain.request().newBuilder()
            .addHeader("Authorization", token)
            .build()
    } else {
        chain.request()
    }

    chain.proceed(newRequest)
}

private val loggingClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    })
    .addInterceptor(authInterceptor)
    .build()


private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(authInterceptor)
    .build()

private const val BASE_URL = "http://10.0.2.2:9999/api/"

private val loggingRetrofit = Retrofit.Builder()
    .client(loggingClient)
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

private val retrofit = Retrofit.Builder()
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

object Api {
    val service: PostApi by lazy {
        retrofit.create(PostApi::class.java)
    }

    val loggingService: PostApi by lazy {
        loggingRetrofit.create(PostApi::class.java)
    }

}