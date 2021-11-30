package ru.netology.nmedia.adapter

import android.view.View
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.EventCardBinding
import ru.netology.nmedia.dto.Event
import ru.netology.nmedia.utils.DateUtils.convertToDateTime

class EventViewHolder(
    private val binding: EventCardBinding,
    private val listener: OnEventInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(event: Event) {
        binding.apply {
            author.text = event.author
            avatar.setImageResource(R.drawable.ic_baseline_photo_camera_24)
            datetime.text = event.datetime.convertToDateTime()
            type.text = event.type.title
            content.text = event.content
            published.text = event.published.convertToDateTime()
            participate.text =
                if (event.participatedByMe) binding.root.context.getString(R.string.reject_participate)
                else binding.root.context.getString(R.string.participate)
            menu.visibility = if (event.ownedByMe) View.VISIBLE else View.INVISIBLE
            count.text = event.participantsIds.size.toString()
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_item)
                    menu.setGroupVisible(R.id.owned, event.ownedByMe)
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.remove -> {
                                listener.onRemove(event)
                                true
                            }
                            R.id.edit -> {
                                listener.onEdit(event)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }

            like.isChecked = event.likedByMe
            like.text = event.likeOwnerIds.size.toString()
            like.setOnClickListener {
                listener.onLike(event)
            }

            share.setOnClickListener {
                listener.onShare(event)
            }

            participate.setOnClickListener {
                listener.onParticipate(event)
            }

            attachment.setOnClickListener {
                listener.onViewAttachment(event)
            }

            event.authorAvatar?.let {
                Glide.with(binding.avatar)
                    .load("${event.authorAvatar}")
                    .placeholder(R.drawable.ic_baseline_photo_camera_24)
                    .error(R.drawable.ic_baseline_error_24)
                    .timeout(10_000)
                    .transform(MultiTransformation(FitCenter(), CircleCrop()))
                    .into(binding.avatar)
            }

            if (event.attachment?.url.isNullOrEmpty()) {
                binding.attachment.visibility = View.GONE
            } else {
                binding.attachment.visibility = View.VISIBLE
                Glide.with(binding.attachment)
                    .load("${event.attachment?.url}")
                    .override(1000, 500)
                    .centerCrop()
                    .error(R.drawable.ic_baseline_error_24)
                    .timeout(10_000)
                    .into(binding.attachment)
            }

        }
    }
}