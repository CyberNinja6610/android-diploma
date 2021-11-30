package ru.netology.nmedia.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnPostInteractionListener
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentPostsFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.ui.NewPostFragment.Companion.photoArg
import ru.netology.nmedia.ui.NewPostFragment.Companion.textArg
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class PostsFeedFragment : Fragment() {
    private val authViewModel: AuthViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )
    @Inject
    lateinit var auth: AppAuth
    val postViewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentPostsFeedBinding.inflate(
            inflater,
            container,
            false
        )


        val adapter = PostAdapter(
            object : OnPostInteractionListener {
                override fun onEdit(post: Post) {
                    findNavController().navigate(
                        R.id.action_postsFeedFragment_to_NewPostFragment,
                        Bundle().apply {
                            textArg = post.content
                            photoArg = post.attachment?.url
                        }
                    )
                    postViewModel.edit(post)
                }

                override fun onRemove(post: Post) {
                    postViewModel.removeById(post.id)
                }

                override fun onLike(post: Post) {
                    if (!authViewModel.authenticated) {
                        showSignInRequiredDialog()
                        return
                    }
                    if (post.likedByMe) {
                        postViewModel.dislikeById(post.id)
                    } else {
                        postViewModel.likeById(post.id)
                    }
                }

                override fun onShare(post: Post) {
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, post.content)
                        type = "text/plain"
                    }

                    val shareIntent =
                        Intent.createChooser(intent, getString(R.string.chooser_share_post))
                    startActivity(shareIntent)
                }

                override fun onViewAttachment(post: Post) {
                    findNavController().navigate(
                        R.id.action_postsFeedFragment_to_attachmentFragment,
                        Bundle().apply {
                            textArg = post.attachment?.url
                        }
                    )
                }
            },
        )

        adapter.addLoadStateListener { loadState ->
            binding.emptyText.isVisible = loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && adapter.itemCount < 1
        }

        binding.list.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener(adapter::refresh)

        postViewModel.dataState.observe(viewLifecycleOwner, { state ->
            binding.progress.visibility = if (state.loading) View.VISIBLE else View.GONE
            binding.swipeRefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { postViewModel.refresh() }
                    .show()
            }
        })

        lifecycleScope.launchWhenCreated {
            postViewModel.data.collectLatest(adapter::submitData)
        }

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { state ->
                binding.swipeRefresh.isRefreshing =
                    state.refresh is LoadState.Loading ||
                            state.prepend is LoadState.Loading ||
                            state.append is LoadState.Loading
            }
        }

        authViewModel.data.observe(viewLifecycleOwner) {
            binding.fab.visibility = if (authViewModel.authenticated) View.VISIBLE else View.GONE
        }

        binding.list.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )

        authViewModel.data.observe(viewLifecycleOwner, { adapter.refresh() })

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_postsFeedFragment_to_NewPostFragment)
        }
        return binding.root
    }

    private fun showSignInRequiredDialog() {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(getString(R.string.auth))
            .setMessage(getString(R.string.sign_in_required))
            .setNeutralButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }
            .setNegativeButton(getString(R.string.sign_up)) { _, _ ->
                findNavController().navigate(R.id.fragment_sign_up)
            }
            .setPositiveButton(getString(R.string.sign_in)) { _, _ ->
                findNavController().navigate(R.id.fragment_sign_in)
            }
            .show()
    }
}