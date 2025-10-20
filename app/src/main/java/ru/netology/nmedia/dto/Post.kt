package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    val published: String,
    val content: String,
    val likes: Long = 12,
    val shared: Long = 25,
    val likeByMe: Boolean = false,
    val shareByMe: Boolean = false,
    val views: Long = 7,
    val video: String? = null
)
