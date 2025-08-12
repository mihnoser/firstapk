package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post

typealias OnItemLikeListener = (post: Post) -> Unit
typealias OnItemShareListener = (post: Post) -> Unit

class PostAdapter(
    private val onItemLikeListener: OnItemLikeListener,
    private val onItemShareListener: OnItemShareListener) :
    ListAdapter<Post, PostViewHolder>(PostDiffCalback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onItemLikeListener, onItemShareListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}


class PostViewHolder(
    private val binding: CardPostBinding,
    private val onItemLikeListener: OnItemLikeListener,
    private val onItemShareListener: OnItemShareListener,
    ) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            countlike.text = formatNumber(post.likes)
            countshare.text = formatNumber(post.shared)
            like.setImageResource(
                if (post.likeByMe) R.drawable.ic_liked_24 else R.drawable.ic_like_24
            )
            like.setOnClickListener {
                onItemLikeListener(post)
            }

            share.setOnClickListener {
                onItemShareListener(post)
            }
        }
    }
}

object PostDiffCalback: DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }

}

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