package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity

class PostRepositoryImpl (
    private val dao: PostDao
) : PostRepository {


    override fun getAll(): LiveData<List<Post>> = dao.get().map { listPosts->
        listPosts.map { entity ->
            entity.toDto()
        }
    }

    override fun save(post: Post) {
        dao.save(PostEntity.fromDto(post))
//        dao.save(post.toEntity())
    }

    override fun likeById(id: Long) {
        dao.likeById(id)
    }

    override fun shareById(id: Long) {
        dao.shareById(id)
    }

    override fun removeById(id: Long) {
        dao.removeById(id)
    }
}