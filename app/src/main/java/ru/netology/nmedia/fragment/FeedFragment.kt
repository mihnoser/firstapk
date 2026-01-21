package ru.netology.nmedia.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.EditPostContract
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.fragment.NewPostFragment.Companion.textArg
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class FeedFragment : Fragment() {
//    private val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)
    private val viewModel: PostViewModel by activityViewModels()

    private lateinit var binding: FragmentFeedBinding

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

        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostAdapter(object : OnInteractionListener {

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

                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
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
        binding.list.adapter = adapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.load()
        }

        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)
            binding.empty.isVisible = state.empty
        }

        viewModel.newerCount.observe(viewLifecycleOwner) { state ->
            if (state > 0) binding.newerPostAdd.isVisible = true
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_load, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.retry) {
                        viewModel.load()
                    }
                    .show()
            }
            binding.swipeRefreshLayout.isRefreshing = state.refreshError
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }

        binding.add.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        binding.newerPostAdd.setOnClickListener {
            viewModel.loadUnshowed()
            binding.newerPostAdd.isVisible = false

            binding.list.postDelayed({
                binding.list.smoothScrollToPosition(0)
            }, 300)
        }

        return binding.root
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