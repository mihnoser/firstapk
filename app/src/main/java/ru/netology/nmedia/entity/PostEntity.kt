package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val authorAvatar: String? = null,
    val published: Long,
    val content: String,
    val likes: Int = 12,
    val shared: Int = 25,
    val likeByMe: Boolean = false,
    val shareByMe: Boolean = false,
    val views: Int = 7,
    val video: String? = null,
    val showed: Boolean = true
)
//    fun toDto(): Post = Post(
//        id = id,
//        author = author,
//        authorAvatar = authorAvatar,
//        published = published,
//        content = content,
//        likes = likes,
//        shared = shared,
//        likedByMe = likeByMe,
//        shareByMe = shareByMe,
//        views = views,
//        video = video
//    )
//
//    companion object {
//        fun fromDto(post: Post): PostEntity = with(post) {
//            PostEntity(
//                id = id,
//                author = author,
//                authorAvatar = authorAvatar,
//                published = published,
//                content = content,
//                likes = likes,
//                shared = shared,
//                likeByMe = likedByMe,
//                shareByMe = shareByMe,
//                views = views,
//                video = video
//            )
//        }
//    }
//}
//
//fun Post.toEntity(): PostEntity = PostEntity(
//    id = id,
//    author = author,
//    authorAvatar = authorAvatar,
//    published = published,
//    content = content,
//    likes = likes,
//    shared = shared,
//    likeByMe = likedByMe,
//    shareByMe = shareByMe,
//    views = views,
//    video = video
//)
{
    fun toDto() = Post(id, author, authorAvatar, published, content, likes, shared, likeByMe, shareByMe, views, video, showed)

    companion object {
    fun fromDto(dto: Post) =
        PostEntity(dto.id, dto.author, dto.authorAvatar, dto.published,dto.content,  dto.likes, dto.shared, dto.likeByMe, dto.shareByMe, dto.views, dto.video, dto.showed)

}
}

    fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
    fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)