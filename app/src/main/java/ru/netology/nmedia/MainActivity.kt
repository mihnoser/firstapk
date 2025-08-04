package ru.netology.nmedia

import android.os.Bundle
import kotlin.math.abs
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val post = Post(
            1,
            "Нетология. Университет интернет-профессий будущего",
            "21 мая в 18:36",
            "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
            999999,
            1125,
            false,
            false
        )

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
            like.setImageResource(R.drawable.ic_like_24)
            countlike.text = formatNumber(post.likes)
            countshare.text = formatNumber(post.shared)

            like.setOnClickListener {
                post.likeByMe = !post.likeByMe
                if (post.likeByMe) {
                    like.setImageResource(R.drawable.ic_liked_24)
                } else {
                    like.setImageResource(R.drawable.ic_like_24)
                }
                if (post.likeByMe) {
                    post.likes++
                } else {
                    post.likes--
                }
                countlike.text = formatNumber(post.likes)
            }

            share.setOnClickListener {
                post.shareByMe = !post.shareByMe
                post.shared++
                countshare.text = formatNumber(post.shared)
            }

            avatar.setOnClickListener{

            }
        }
    }
}