package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.R.id.owned
import ru.netology.nmedia.databinding.CardAdBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.view.load
import ru.netology.nmedia.view.loadCircleCrop


interface OnInteractionListener {
    fun onLike(post: Post)
    fun onEdit(post: Post)
    fun onRemove(post: Post)
    fun onShare(post: Post)
    fun onPlayVideo(post: Post)
    fun onOpen(post: Post)
    fun onPreviewImage(post : Post){}
}

class PostAdapter(
    private val onInteractionListener: OnInteractionListener) :
    PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCalback) {
    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is Post -> R.layout.card_post
            null -> error("unknown item type")
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_post -> {
                val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, onInteractionListener)
            }
            R.layout.card_ad -> {
                val binding = CardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(binding)
            }
            else -> error("unknown view type: $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Ad -> (holder as? AdViewHolder)?.bind(item)
            is Post -> (holder as? PostViewHolder)?.bind(item)
            null -> error("unknown item type")
        }
    }
}

class AdViewHolder(
    private val binding: CardAdBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(ad: Ad) {
        binding.image.load("${BuildConfig.BASE_URL}/media/${ad.image}")
    }
}


class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published.toString()
            content.text = post.content

            menu.visibility = if (post.ownedByMe) View.VISIBLE else View.INVISIBLE



            avatar.loadCircleCrop("${BuildConfig.BASE_URL}/avatars/${post.authorAvatar}")

            like.isChecked = post.likedByMe
            like.text = formatNumber(post.likes.toLong())

            share.isChecked = post.shareByMe
            share.text = formatNumber(post.shared.toLong())

            if (post.attachment != null && post.attachment.type == AttachmentType.IMAGE) {
                attachmentContainer.visibility = View.VISIBLE

                val imageUrl = "${BuildConfig.BASE_URL}/media/${post.attachment.url}"
                attachmentImage.load(imageUrl)

                post.attachment.description?.let { description ->
                    attachmentDescription.visibility = View.VISIBLE
                    attachmentDescription.text = description
                } ?: run {
                    attachmentDescription.visibility = View.GONE
                }
            } else {
                attachmentContainer.visibility = View.GONE
            }

            if (!post.video.isNullOrEmpty()) {
                videoContainer.visibility = View.VISIBLE

                val videoClickListener = View.OnClickListener {
                    onInteractionListener.onPlayVideo(post)
                }

                videoContainer.setOnClickListener(videoClickListener)
                playButton.setOnClickListener(videoClickListener)
            } else {
                videoContainer.visibility = View.GONE
            }

            like.setOnClickListener {
                onInteractionListener.onLike(post)
            }

            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.menu_post)
                    menu.setGroupVisible(owned, post.ownedByMe)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            attachmentImage.setOnClickListener {
                onInteractionListener.onPreviewImage(post)
            }

            root.setOnClickListener {
                onInteractionListener.onOpen(post)
            }
        }

    }
}

object PostDiffCalback: DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }

        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }

}

fun formatNumber(count: Long): String {
    return when {
        count < 1000 -> count.toString()
        count < 10000 -> {
            val hundreds = (count % 1000) / 100
            if (hundreds == 0.toLong()) {
                "${count / 1000}K"
            } else {
                "${count / 1000}.${hundreds}K"
            }
        }

        count < 1000000 -> "${count / 1000}K"
        else -> {
            val hundredThousands = (count % 1000000) / 100000
            if (hundredThousands == 0.toLong()) {
                "${count / 1000000}M"
            } else {
                "${count / 1000000}.${hundredThousands}M"
            }
        }
    }
}