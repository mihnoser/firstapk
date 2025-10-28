package ru.netology.nmedia.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date

/*class PostRepositoryFilesImpl(private val context: Context): PostRepository {

    private var nextId = 1
    private var posts = listOf<Post>()
        set (value) {
            field = value
            sync()
        }


    private val data = MutableLiveData(posts)

    init {
        val file = context.filesDir.resolve(FILENAME)
        if (file.exists()) {
            context.openFileInput(FILENAME).bufferedReader().use {
                posts = gson.fromJson(it, type)
                nextId = (posts.maxOfOrNull { it.id }?.inc() ?: 1) as Int
                data.value = posts
            }
        }
    }

    private fun sync() {
        context.openFileOutput(FILENAME, Context.MODE_PRIVATE).bufferedWriter().use {
            it.write(gson.toJson(posts))
        }
    }

    override fun getAll(): LiveData<List<Post>> = data
    override fun likeById(id: Long) {
        posts = posts.map {
            if (it.id.toInt().toLong() != id) it else it.copy(likeByMe = !it.likeByMe, likes = if (it.likeByMe) it.likes - 1 else it.likes + 1)
        }
        data.value = posts
    }

    override fun shareById(id: Long) {
        posts = posts.map {
            if (it.id != id) it else it.copy(shareByMe = !it.shareByMe, shared = it.shared + 1)
        }
        data.value = posts
    }

    override fun removeById(id: Long) {
        posts = posts.filter { it.id != id }
        data.value = posts
    }

    override fun save(post: Post): Post {
        posts = if (post.id == 0L) {
            listOf(post.copy(id = nextId++.toLong(), author = "Me", published = 22/10/2025)) + posts
        } else {
            posts.map { if (it.id != post.id) it else it.copy(content = post.content) }
        }
        data.value = posts
        return TODO("Provide the return value")
    }

    companion object {
        private const val FILENAME = "posts.json"

        private val gson = Gson()
        private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type

    }


}*/