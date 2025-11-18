package ru.netology.nmedia.repository

import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.Post

class PostRepositoryNetwork(): PostRepository {

    override fun getAll(): List<Post> {
        return PostApi.service.getAll()
            .execute()
            .body()
            .orEmpty()
    }

    override fun save(post: Post): Post {
        return PostApi.service.save(post)
            .execute()
            .body() ?: throw RuntimeException()
    }

    override fun likeById(id: Long): Post {
        return PostApi.service.likeById(id)
            .execute()
            .body() ?: throw RuntimeException()
    }

    override fun unlikeById(id: Long): Post {
        return PostApi.service.dislikeById(id)
            .execute()
            .body() ?: throw RuntimeException()
    }

    override fun shareById(id: Long): Post {
        return PostApi.service.shareById(id)
            .execute()
            .body() ?: throw RuntimeException()
    }

    override fun removeById(id: Long) {
        PostApi.service.deleteById(id)
            .execute()
    }

    override fun getAllAsync(callback: PostRepository.GetAllCallback) {
        PostApi.service.getAll()
            .enqueue(object : Callback<List<Post>> {
                override fun onResponse(
                    call: Call<List<Post>>,
                    response: Response<List<Post>>
                ) {
                    if (!response.isSuccessful) {
                        val error = handleError(response, "Failed to load posts")
                        callback.onError(error)
                        return
                    }
                    callback.onSuccess(response.body().orEmpty())
                }

                override fun onFailure(call: Call<List<Post>>, throwable: Throwable) {
                    callback.onError(throwable)
                }
            })
    }

    override fun likeByIdAsync(id: Long, likedByMe: Boolean, callback: PostRepository.PostCallback<Post>) {
        val call = if (likedByMe) {
            PostApi.service.dislikeById(id)
        } else {
            PostApi.service.likeById(id)
        }

        call.enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (!response.isSuccessful) {
                    val error = handleError(response, "Failed to like post")
                    callback.onError(error)
                    return
                }
                callback.onSuccess(response.body() ?: throw RuntimeException("body is null"))
            }

            override fun onFailure(call: Call<Post>, throwable: Throwable) {
                callback.onError(throwable)
            }
        })
    }

    override fun shareByIdAsync(id: Long, callback: PostRepository.PostCallback<Post>) {
        PostApi.service.shareById(id)
            .enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    if (!response.isSuccessful) {
                        val error = handleError(response, "Failed to like post")
                        callback.onError(error)
                        return
                    }
                    callback.onSuccess(response.body() ?: throw RuntimeException("body is null"))
                }

                override fun onFailure(call: Call<Post>, throwable: Throwable) {
                    callback.onError(throwable)
                }
            })
    }

    override fun removeByIdAsync(id: Long, callback: PostRepository.PostCallback<Unit>) {
        PostApi.service.deleteById(id)
            .enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (!response.isSuccessful) {
                        val error = handleError(response, "Failed to like post")
                        callback.onError(error)
                        return
                    }
                    callback.onSuccess(Unit)
                }

                override fun onFailure(call: Call<Unit>, throwable: Throwable) {
                    callback.onError(throwable)
                }
            })
    }

    override fun saveAsync(post: Post, callback: PostRepository.PostCallback<Post>) {
        PostApi.service.save(post)
            .enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    if (!response.isSuccessful) {
                        val error = handleError(response, "Failed to like post")
                        callback.onError(error)
                        return
                    }
                    callback.onSuccess(response.body() ?: throw RuntimeException("body is null"))
                }

                override fun onFailure(call: Call<Post>, throwable: Throwable) {
                    callback.onError(throwable)
                }
            })
    }
}