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
import ru.netology.nmedia.adapter.EventAdapter
import ru.netology.nmedia.adapter.OnEventInteractionListener
import ru.netology.nmedia.databinding.FragmentEventsFeedBinding
import ru.netology.nmedia.dto.Event
import ru.netology.nmedia.entity.EventTypeEmbeddable
import ru.netology.nmedia.ui.NewEventFragment.Companion.datetimeArg
import ru.netology.nmedia.ui.NewEventFragment.Companion.typeArg
import ru.netology.nmedia.ui.NewPostFragment.Companion.photoArg
import ru.netology.nmedia.ui.NewPostFragment.Companion.textArg
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.EventsViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class EventsFeedFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    val eventViewModel: EventsViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentEventsFeedBinding.inflate(
            inflater,
            container,
            false
        )

        val adapter = EventAdapter(
            object : OnEventInteractionListener {
                override fun onEdit(event: Event) {
                    findNavController().navigate(
                        R.id.action_eventsFeedFragment_to_newEventFragment,
                        Bundle().apply {
                            textArg = event.content
                            datetimeArg = event.datetime
                            typeArg = EventTypeEmbeddable.fromDto(event.type).eventType
                            photoArg = event.attachment?.url
                        }
                    )
                    eventViewModel.edit(event)
                }

                override fun onRemove(event: Event) {
                    eventViewModel.removeById(event.id)
                }

                override fun onLike(event: Event) {
                    if (!authViewModel.authenticated) {
                        showSignInRequiredDialog()
                        return
                    }
                    if (event.likedByMe) {
                        eventViewModel.dislikeById(event.id)
                    } else {
                        eventViewModel.likeById(event.id)
                    }
                }

                override fun onViewAttachment(event: Event) {
                    findNavController().navigate(
                        R.id.action_eventsFeedFragment_to_attachmentFragment,
                        Bundle().apply {
                            textArg = event.attachment?.url
                        }
                    )
                }

                override fun onParticipate(event: Event) {
                    if (!authViewModel.authenticated) {
                        showSignInRequiredDialog()
                        return
                    }
                    if (event.participatedByMe) {
                        eventViewModel.rejectParticipateById(event.id)
                    } else {
                        eventViewModel.participateById(event.id)
                    }
                }

                override fun onShare(event: Event) {
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, event.content)
                        type = "text/plain"
                    }

                    val shareIntent =
                        Intent.createChooser(intent, getString(R.string.chooser_share_event))
                    startActivity(shareIntent)
                }
            }
        )

        /** Проверка на наличие элементов в адаптере*/
        adapter.addLoadStateListener { loadState ->
            binding.emptyText.isVisible = loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && adapter.itemCount < 1
        }

        binding.list.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener(adapter::refresh)

        eventViewModel.dataState.observe(viewLifecycleOwner, { state ->
            binding.progress.visibility = if (state.loading) View.VISIBLE else View.GONE
            binding.swipeRefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { eventViewModel.refresh() }
                    .show()
            }
        })

        lifecycleScope.launchWhenCreated {
            eventViewModel.data.collectLatest(adapter::submitData)
        }



        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { state ->
                binding.swipeRefresh.isRefreshing =
                    state.refresh is LoadState.Loading ||
                            state.prepend is LoadState.Loading ||
                            state.append is LoadState.Loading
            }
        }

        authViewModel.data.observe(viewLifecycleOwner, { adapter.refresh() })

        authViewModel.data.observe(viewLifecycleOwner) {
            binding.fab.visibility = if (authViewModel.authenticated) View.VISIBLE else View.INVISIBLE
        }

        binding.list.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_eventsFeedFragment_to_newEventFragment)
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