package ru.netology.nmedia.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.snapshots.Snapshot.Companion.observe
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.EditPostActivity
import ru.netology.nmedia.activity.EditPostContract
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewModel.PostViewModel

class FeedFragment : Fragment() {

    private lateinit var viewModel: PostViewModel

    private val editPostLauncher = registerForActivityResult(
        EditPostContract
    ) { result ->
        result?.let { content ->
            viewModel.save(content)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        viewModel = viewModels<PostViewModel>(ownerProducer = ::requireParentFragment).value

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
                editPostLauncher.launch(post.content)
            }

            override fun onOpen(post: Post) {
                findNavController().navigate(R.id.postFragment, Bundle().apply {
                    putInt(PostFragment.KEY_POST_ID, post.id.toInt()) })
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

        })
        binding.list.adapter = adapter
        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)
            binding.progress.isVisible = state.loading
            binding.empty.isVisible = state.empty
            binding.errorGroup.isVisible = state.error
        }

        binding.retry.setOnClickListener {
            viewModel.load()
        }


        binding.add.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
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