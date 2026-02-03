package ru.netology.nmedia.dto

sealed interface FeedItem {
    val id: Long
}

data class Post(
    override val id: Long,
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
    val attachment: Attachment? = null,
    val showed: Boolean = false,
    val authorId: Long = 0,
    val ownedByMe: Boolean = false
) : FeedItem

data class Ad(
    override val id: Long,
    val image: String,
) : FeedItem