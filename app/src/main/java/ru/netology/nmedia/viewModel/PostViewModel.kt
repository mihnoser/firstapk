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

    fun likeById(id: Long) {
        val currentPosts = _data.value?.posts ?: return
        val post = currentPosts.find { it.id == id } ?: return

        repository.likeByIdAsync(id, post.likedByMe, object : PostRepository.PostCallback<Post> {
            override fun onSuccess(result: Post) {
                updatePostInList(result)
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    fun shareById(id: Long) {
        repository.shareByIdAsync(id, object : PostRepository.PostCallback<Post> {
            override fun onSuccess(result: Post) {
                updatePostInList(result)
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    fun removeById(id: Long) {
        repository.removeByIdAsync(id, object : PostRepository.PostCallback<Unit> {
            override fun onSuccess(result: Unit) {
                load()
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    fun save(content: String) {
        edited.value?.let {
            val text = content.trim()
            if (it.content != text) {
                repository.saveAsync(it.copy(content = text), object : PostRepository.PostCallback<Post> {
                    override fun onSuccess(result: Post) {
                        _postCreated.postValue(Unit)
                        edited.postValue(empty)
                    }

                    override fun onError(e: Exception) {
                        _data.postValue(FeedModel(error = true))
                    }
                })
            } else {
                edited.postValue(empty)
            }
        }
    }

    fun load() {
        _data.postValue(FeedModel(loading = true))
        repository.getAllAsync(object : PostRepository.PostCallback<List<Post>> {
            override fun onSuccess(result: List<Post>) {
                _data.postValue(FeedModel(posts = result, empty = result.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    private fun updatePostInList(updatedPost: Post) {
        val currentPosts = _data.value?.posts ?: emptyList()
        val updatedPosts = currentPosts.map { post ->
            if (post.id == updatedPost.id) updatedPost else post
        }
        _data.postValue(FeedModel(posts = updatedPosts, empty = updatedPosts.isEmpty()))
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun cancelEdit() {
        edited.value = empty
    }
}