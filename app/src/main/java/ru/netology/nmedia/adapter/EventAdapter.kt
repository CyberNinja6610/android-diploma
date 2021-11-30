package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.databinding.EventCardBinding
import ru.netology.nmedia.dto.Event

class EventAdapter(
    private val listener: OnEventInteractionListener
) : PagingDataAdapter<Event, RecyclerView.ViewHolder>(EventDiffItemCallback){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        EventViewHolder(
            EventCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            listener
        )


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is EventViewHolder -> {
                getItem(position)?.let {
                    holder.bind(it)
                }
            }
        }
    }

    object EventDiffItemCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean =
            oldItem == newItem
    }
}