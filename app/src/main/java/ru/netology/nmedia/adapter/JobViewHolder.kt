package ru.netology.nmedia.adapter

import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.JobCardBinding
import ru.netology.nmedia.dto.Job
import ru.netology.nmedia.utils.DateUtils.convertToDate

class JobViewHolder(
    private val binding: JobCardBinding,
    private val listener: OnJobInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(job: Job) {
        binding.apply {
            jobStart.text = job.start.convertToDate()
            jobFinish.text = if(job.finish != null) job.start.convertToDate() else ""
            position.text = job.position
            company.text = job.name
            link.text = job.link
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_item)
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.remove -> {
                                listener.onRemove(job)
                                true
                            }
                            R.id.edit -> {
                                listener.onEdit(job)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }
        }
    }
}