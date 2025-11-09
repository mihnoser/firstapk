package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    val authorAvatar: String? = null,
    val published: Long,
    val content: String,
    val likes: Int = 0,
    val shared: Int = 0,
    val likedByMe: Boolean = false,
    val shareByMe: Boolean = false,
    val views: Int = 0,
    val video: String? = null,
    val attachment: Attachment? = null
)