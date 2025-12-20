package ru.netology.nmedia.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.AppActivity.Companion.textArg
import ru.netology.nmedia.activity.EditPostContract
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostViewHolder
import ru.netology.nmedia.databinding.FragmentPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewModel.PostViewModel

class PostFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    private val editPostLauncher = registerForActivityResult(
        EditPostContract
    ) { result ->
        result?.let { content ->
            viewModel.save()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostBinding.inflate(inflater, container, false)
        val postId = arguments?.getLong(KEY_POST_ID, -1L) ?: -1L
        val viewHolder = PostViewHolder(binding.post, object : OnInteractionListener {

            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }

            override fun onShare(post: Post) {
                viewModel.shareById(post.id)

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

            override fun onOpen(post: Post) {
                findNavController().navigate(R.id.postFragment, Bundle().apply {
                    putLong(PostFragment.KEY_POST_ID, post.id)
                })
            }

            override fun onPlayVideo(post: Post) {
                post.video?.let { videoUrl ->
                    if (isValidVideoUrl(videoUrl)) {
                        openVideo(videoUrl)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Некорректная ссылка на видео",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onPreviewImage(post: Post) {
                findNavController().navigate(R.id.action_feedFragment_to_imagePreviewFragment,
                    Bundle().apply {
                        textArg = post.attachment?.url
                    })
            }
        })

        viewModel.data.observe(viewLifecycleOwner) { feedModel ->
            val posts = feedModel.posts
            val post = posts.find { it.id == postId} ?: run {
                findNavController().navigateUp()
                return@observe
            }
            viewHolder.bind(post)
        }

        return binding.root
    }

    companion object {
        const val KEY_POST_ID = "postId"
    }

    private fun openVideo(videoUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))

            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Не найдено приложение для открытия видео",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
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