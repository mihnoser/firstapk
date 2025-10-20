package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
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
){
    fun toDto(): Post = Post(
        id = id,
        author = author,
        published = published,
        content = content,
        likes = likes,
        shared = shared,
        likeByMe = likeByMe,
        shareByMe = shareByMe,
        views = views,
        video = video
    )

    companion object {
        fun fromDto(post: Post): PostEntity = with(post) {
            PostEntity(
                id = id,
                author = author,
                published = published,
                content = content,
                likes = likes,
                shared = shared,
                likeByMe = likeByMe,
                shareByMe = shareByMe,
                views = views,
                video = video
            )
        }
    }
}

fun Post.toEntity(): PostEntity = PostEntity(
    id = id,
    author = author,
    published = published,
    content = content,
    likes = likes,
    shared = shared,
    likeByMe = likeByMe,
    shareByMe = shareByMe,
    views = views,
    video = video
)