package ru.netology.nmedia.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewModel.PostViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel: PostViewModel by viewModels()

        val newPostLauncher = registerForActivityResult(NewPostContract) { result ->
            result ?: return@registerForActivityResult
            viewModel.save(content = result)
        }

        val editPostLauncher = registerForActivityResult(EditPostContract) { result ->
            result ?: return@registerForActivityResult
            viewModel.save(result)
        }

        val adapter = PostAdapter(object  : OnInteractionListener {
            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }
            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, post.content)
                }
                val chooser = Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(chooser)
            }
            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }
            override fun onEdit(post: Post) {
                viewModel.edit(post)
                editPostLauncher.launch(post.content)
            }

            override fun onPlayVideo(post: Post) {
                post.video?.let { videoUrl ->
                    if (isValidVideoUrl(videoUrl)) {
                        openVideo(videoUrl)
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Некорректная ссылка на видео",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        })
        binding.list.adapter = adapter
        viewModel.data.observe(this) { posts ->
            val new = posts.size > adapter.currentList.size && adapter.currentList.isNotEmpty()
            adapter.submitList(posts) {
                if (new) {
                    binding.list.smoothScrollToPosition(0)
                }

            }
        }

        binding.add.setOnClickListener {
            newPostLauncher.launch()
        }

        /* viewModel.edited.observe(this) {
            if (it.id != 0) {
                binding.content.setText(it.content)
                AndroidUtils.showKeyboard(binding.content)
                binding.editGroup.visibility = View.VISIBLE
            } else {
                binding.editGroup.visibility = View.GONE
            }
        }
        binding.save.setOnClickListener {
            with(binding.content) {
                if (text.isNullOrBlank()) {
                    Toast.makeText(
                        this@MainActivity,
                        R.string.error_empty_content,
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }

                viewModel.save(text.toString())

                binding.content.setText("")
                binding.content.clearFocus()

                AndroidUtils.hideKeyboard(binding.content)
            }

        }

        binding.cancel.setOnClickListener {
            viewModel.cancelEdit()
            binding.content.setText("")
            binding.content.clearFocus()
            AndroidUtils.hideKeyboard(binding.content)
        } */

    }

    private fun openVideo(videoUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))

            // Проверяем, есть ли приложения для обработки этого Intent
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(
                    this,
                    "Не найдено приложение для открытия видео",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Ошибка при открытии видео: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun isValidVideoUrl(url: String): Boolean {
        return Patterns.WEB_URL.matcher(url).matches() &&
                url.contains("rutube.ru", ignoreCase = true)
    }
}