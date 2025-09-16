package ru.netology.nmedia.dao

import ru.netology.nmedia.dto.Post

interface PostDao {
    fun get(): List<Post>
    fun save(post: Post): Post
    fun likeById(id: Int)
    fun shareById(id: Int)
    fun removeById(id: Int)
}