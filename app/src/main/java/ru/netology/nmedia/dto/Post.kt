package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    val published: Long,
    val content: String,
    val likes: Int = 12,
    val shared: Int = 25,
    val likedByMe: Boolean = false,
    val shareByMe: Boolean = false,
    val views: Int = 7,
    val video: String? = null
)
