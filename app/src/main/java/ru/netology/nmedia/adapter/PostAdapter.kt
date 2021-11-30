package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.databinding.PostCardBinding
import ru.netology.nmedia.dto.Post

class PostAdapter(
    private val listener: OnPostInteractionListener,
) : PagingDataAdapter<Post, RecyclerView.ViewHolder>(PostDiffItemCallback) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        PostViewHolder(
            PostCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            listener,
        )


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PostViewHolder -> {
                getItem(position)?.let {
                    holder.bind(it)
                }
            }
        }
    }

    object PostDiffItemCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
            oldItem == newItem
    }

}