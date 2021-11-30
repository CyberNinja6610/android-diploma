package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ru.netology.nmedia.databinding.JobCardBinding
import ru.netology.nmedia.dto.Job

class JobAdapter(
    private val listener: OnJobInteractionListener
) : ListAdapter<Job, JobViewHolder>(EventDiffItemCallback){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder =
        JobViewHolder(
            JobCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            listener
        )


    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    object EventDiffItemCallback : DiffUtil.ItemCallback<Job>() {
        override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean =
            oldItem == newItem
    }
}