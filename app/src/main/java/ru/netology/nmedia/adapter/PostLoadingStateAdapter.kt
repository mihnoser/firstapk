package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ItemLoadingBinding

class PostLoadingStateAdapter(
    private val retryListener: () -> Unit,
    private val isHeader: Boolean = false,
)  : LoadStateAdapter<PostLoadingViewHolder>() {

    override fun onBindViewHolder(
        holder: PostLoadingViewHolder,
        loadState: LoadState
    ) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): PostLoadingViewHolder {
        val layoutId = if (isHeader) {
            R.layout.item_loading_prepend
        } else {
            R.layout.item_loading_append
        }

        val binding = ItemLoadingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return PostLoadingViewHolder(binding, retryListener)
    }
}

class PostLoadingViewHolder(
    private val itemLoadingBinding: ItemLoadingBinding,
    private val retryListener: () -> Unit,
) : RecyclerView.ViewHolder(itemLoadingBinding.root) {

    fun bind(loadState: LoadState, isHeader: Boolean = false) {
        itemLoadingBinding.apply {
            if (isHeader) {
                progress.isVisible = loadState is LoadState.Loading
                retryButton.isVisible = false
            } else {
                progress.isVisible = loadState is LoadState.Loading
                retryButton.isVisible = loadState is LoadState.Error
                retryButton.setOnClickListener {
                    retryListener()
                }
            }
        }
    }
}