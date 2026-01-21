package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
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
    val likedByMe: Boolean = false,
    val shareByMe: Boolean = false,
    val views: Int = 7,
    val video: String? = null,
    @Embedded
    var attachment: AttachmentEmbeddable?,
    val showed: Boolean = true,
    val authorId: Long,
    val ownedByMe: Boolean = false
) {

    fun toDto() = Post(
        id = id,
        author = author,
        authorAvatar = authorAvatar,
        published = published,
        content = content,
        likes = likes,
        shared = shared,
        likedByMe = likedByMe,
        shareByMe = shareByMe,
        views = views,
        video = video,
        attachment = attachment?.let {
            val result = it.toDto()
            result
        },
        showed = showed,
        authorId = authorId,
        ownedByMe = ownedByMe
    )

    companion object {
        fun fromDto(dto: Post) = PostEntity(
            id = dto.id,
            author = dto.author,
            authorAvatar = dto.authorAvatar,
            published = dto.published,
            content = dto.content,
            likes = dto.likes,
            shared = dto.shared,
            likedByMe = dto.likedByMe,
            shareByMe = dto.shareByMe,
            views = dto.views,
            video = dto.video,
            attachment = AttachmentEmbeddable.fromDto(dto.attachment),
            showed = dto.showed,
            authorId = dto.authorId,
            ownedByMe = dto.ownedByMe
        )
    }
}

data class AttachmentEmbeddable(
    var url: String,
    var description: String? = null,
    var type: String
) {
    fun toDto(): Attachment? {
        return try {
            val attachmentType = enumValueOf<AttachmentType>(type)
            Attachment(url, attachmentType, description)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(
                url = it.url,
                description = it.description,
                type = it.type.name
            )
        }
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map {
    PostEntity.fromDto(it.copy(showed = true))
}