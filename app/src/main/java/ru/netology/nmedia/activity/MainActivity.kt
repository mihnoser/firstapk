package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.viewModel.PostViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel: PostViewModel by viewModels()
        viewModel.data.observe(this) { post ->


            fun formatNumber(count: Int): String {
                return when {
                    count < 1000 -> count.toString()
                    count < 10000 -> {
                        val hundreds = (count % 1000) / 100
                        if (hundreds == 0) {
                            "${count / 1000}K"
                        } else {
                            "${count / 1000}.${hundreds}K"
                        }
                    }

                    count < 1000000 -> "${count / 1000}K"
                    else -> {
                        val hundredThousands = (count % 1000000) / 100000
                        if (hundredThousands == 0) {
                            "${count / 1000000}M"
                        } else {
                            "${count / 1000000}.${hundredThousands}M"
                        }
                    }
                }
            }

            with(binding) {
                author.text = post.author
                published.text = post.published
                content.text = post.content
                countlike.text = formatNumber(post.likes)
                countshare.text = formatNumber(post.shared)
                like.setImageResource(
                    if (post.likeByMe) R.drawable.ic_liked_24 else R.drawable.ic_like_24
                )
            }

            binding.like.setOnClickListener {
                viewModel.like()
            }

            binding.share.setOnClickListener {
                viewModel.share()
            }
        }

    }
}
