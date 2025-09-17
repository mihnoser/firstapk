package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val author: String,
    val published: String,
    val content: String,
    val likes: Int = 12,
    val shared: Int = 25,
    val likeByMe: Boolean = false,
    val shareByMe: Boolean = false,
    val views: Int = 7,
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