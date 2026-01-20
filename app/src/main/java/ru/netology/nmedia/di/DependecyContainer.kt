package ru.netology.nmedia.di

import android.content.Context
import androidx.room.Room
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryNetwork
import java.util.concurrent.TimeUnit



class DependecyContainer(

    private val context: Context
) {

    companion object {

        private const val BASE_URL = "http://10.0.2.2:9999/api/"

        @Volatile
        private var instance: DependecyContainer? = null

        fun initApp(context: Context) {
            instance = DependecyContainer(context)
        }

        fun getInstance(): DependecyContainer {
            return instance!!
        }


    }

    val appAuth = AppAuth(context)

    private val authInterceptor = Interceptor { chain ->
        val token = appAuth.authStateFlow.value.token

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

    private val appBd = Room.databaseBuilder(context, AppDb::class.java, "app.db")
    .build()

    val apiService = retrofit.create<ApiService>()

    private val postDao = appBd.postDao()

    val repository: PostRepository = PostRepositoryNetwork(
        postDao,
        apiService,
    )
}