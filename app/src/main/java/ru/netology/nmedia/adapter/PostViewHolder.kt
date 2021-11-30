package ru.netology.nmedia.adapter

import android.view.View
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.PostCardBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.utils.DateUtils.convertToDateTime

class PostViewHolder(
    private val binding: PostCardBinding,
    private val listener: OnPostInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            avatar.setImageResource(R.drawable.ic_baseline_photo_camera_24)
            published.text = post.published.convertToDateTime()
            content.text = post.content
            like.isChecked = post.likedByMe
            like.text = post.likeOwnerIds.size.toString()
            menu.visibility = if(post.ownedByMe) View.VISIBLE else View.INVISIBLE
            like.setOnClickListener {
                listener.onLike(post)
            }

            share.setOnClickListener {
                listener.onShare(post)
            }

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_item)
                    menu.setGroupVisible(R.id.owned, post.ownedByMe)
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.remove -> {
                                listener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                listener.onEdit(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }

            attachment.setOnClickListener  {
                listener.onViewAttachment(post)
            }

            post.authorAvatar?.let {
                Glide.with(binding.avatar)
                    .load("${post.authorAvatar}")
                    .placeholder(R.drawable.ic_baseline_photo_camera_24)
                    .error(R.drawable.ic_baseline_error_24)
                    .timeout(10_000)
                    .transform(MultiTransformation(FitCenter(), CircleCrop()))
                    .into(binding.avatar)
            }


            if(!post.attachment?.url.isNullOrEmpty()) {
                binding.attachment.visibility = View.VISIBLE
                Glide.with(binding.attachment)
                    .load("${post.attachment?.url}")
                    .override(1000,500)
                    .centerCrop()
                    .error(R.drawable.ic_baseline_error_24)
                    .timeout(10_000)
                    .into(binding.attachment)
            } else {
                binding.attachment.visibility = View.GONE
            }
        }
    }
}