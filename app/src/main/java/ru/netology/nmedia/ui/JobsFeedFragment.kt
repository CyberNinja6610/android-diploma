package ru.netology.nmedia.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.*
import ru.netology.nmedia.databinding.FragmentJobsFeedBinding
import ru.netology.nmedia.dto.Job
import ru.netology.nmedia.ui.NewJobFragment.Companion.finishArg
import ru.netology.nmedia.ui.NewJobFragment.Companion.nameArg
import ru.netology.nmedia.ui.NewJobFragment.Companion.positionArg
import ru.netology.nmedia.ui.NewJobFragment.Companion.siteArg
import ru.netology.nmedia.ui.NewJobFragment.Companion.startArg
import ru.netology.nmedia.utils.AndroidUtils
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.JobViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class JobsFeedFragment : Fragment() {

    private val viewModel: JobViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentJobsFeedBinding.inflate(
            inflater,
            container,
            false
        )

        val adapter = JobAdapter(
            object : OnJobInteractionListener {
                override fun onEdit(job: Job) {
                    findNavController().navigate(
                        R.id.action_jobsFeedFragment_to_newJobFragment,
                        Bundle().apply {
                            startArg = job.start
                            finishArg = job.finish
                            nameArg = job.name
                            positionArg = job.position
                            siteArg = job.link
                        }
                    )
                    viewModel.edit(job)
                    viewModel.save()
                    AndroidUtils.hideKeyboard(requireView())
                }

                override fun onRemove(job: Job) {
                    viewModel.removeById(job.id)
                }
            }
        )

        binding.list.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }

        viewModel.dataState.observe(viewLifecycleOwner, { state ->
            binding.progress.visibility = if (state.loading) View.VISIBLE else View.GONE
            binding.swipeRefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.refresh() }
                    .show()
            }
        })

        viewModel.data.observe(viewLifecycleOwner, {state ->
            binding.emptyText.visibility = if (state.empty) View.VISIBLE else View.GONE
            adapter.submitList(state.jobs)
        })

        binding.list.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.fragment_new_job)
        }

        return binding.root
    }
}