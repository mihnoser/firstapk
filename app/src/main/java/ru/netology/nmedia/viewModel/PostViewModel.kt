package ru.netology.nmedia.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryNetwork
import ru.netology.nmedia.util.SingleLiveEvent
import kotlin.concurrent.thread

private val empty = Post(
    id = 0,
    author = "",
    published = 0,
    content = "",
    likes = 0,
    shared = 0,
    likedByMe = false,
    shareByMe = false,
    views = 0,
    video = null
)

class PostViewModel(application: Application): AndroidViewModel(application) {

    private val repository: PostRepository = PostRepositoryNetwork()
    private val _data = MutableLiveData<FeedModel>()
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated
    init {
        load()
    }

    fun likeById(id: Long) = thread {
        try {
            val updatedPost = repository.likeById(id)
            updatePostInList(updatedPost)
        } catch (e: Exception) {
            _data.postValue(FeedModel(error = true))
        }
    }

    fun unlikeById(id: Long) = thread {
        try {
            val updatedPost = repository.unlikeById(id)
            updatePostInList(updatedPost)
        } catch (e: Exception) {
            _data.postValue(FeedModel(error = true))
        }
    }

    private fun updatePostInList(updatedPost: Post) {
        val currentPosts = _data.value?.posts ?: emptyList()
        val updatedPosts = currentPosts.map { post ->
            if (post.id == updatedPost.id) updatedPost else post
        }
        _data.postValue(FeedModel(posts = updatedPosts, empty = updatedPosts.isEmpty()))
    }

    fun shareById(id: Long) = thread {
        try {
            val updatedPost = repository.shareById(id)
            updatePostInList(updatedPost)
        } catch (e: Exception) {
            _data.postValue(FeedModel(error = true))
        }
    }

    fun removeById(id: Long) = thread {
        repository.removeById(id)
        load()
    }
    fun save(content: String) {
        thread {
            edited.value?.let {
                val text = content.trim()
                if (it.content != text) {
                    repository.save(it.copy(content = text))
                    _postCreated.postValue(Unit)
                }
            }
            edited.postValue(empty)
        }
    }

    fun load() {
        thread {
            _data.postValue(FeedModel(loading = true))
            try {
                val posts = repository.getAll()
                _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
            } catch (_: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun cancelEdit() {
        edited.value = empty
    }

}