package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(): List<Post>
    fun likeById(id: Long): Post
    fun unlikeById(id: Long): Post
    fun shareById(id: Long): Post
    fun removeById(id: Long)
    fun save(post: Post): Post

    fun getAllAsync(callback: PostCallback<List<Post>>)
    fun likeByIdAsync(id: Long, likedByMe: Boolean, callback: PostCallback<Post>)
    fun shareByIdAsync(id: Long, callback: PostCallback<Post>)
    fun removeByIdAsync(id: Long, callback: PostCallback<Unit>)
    fun saveAsync(post: Post, callback: PostCallback<Post>)

    interface PostCallback<T> {
        fun onSuccess(result: T)
        fun onError(e: Exception)
    }
}