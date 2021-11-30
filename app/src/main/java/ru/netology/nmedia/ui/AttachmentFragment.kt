package ru.netology.nmedia.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentAttachmentBinding
import ru.netology.nmedia.utils.StringArg
import android.graphics.drawable.ColorDrawable
import android.view.*
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.ActivityApp
import ru.netology.nmedia.view.load

@ExperimentalCoroutinesApi
class AttachmentFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private var fragmentBinding: FragmentAttachmentBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val activity = requireActivity()
        if (activity is ActivityApp) {
            val statusBarColor = ContextCompat.getColor(requireActivity(),R.color.black)
            activity.window.statusBarColor = statusBarColor
            activity.supportActionBar?.setBackgroundDrawable(ColorDrawable(statusBarColor))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_attachment, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.close -> {
                findNavController().navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAttachmentBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentBinding = binding

        arguments?.textArg?.let(binding.attachmentImage::load)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentBinding = null
        val activity = requireActivity()
        if (activity is ActivityApp) {
            val statusBarColor = ContextCompat.getColor(requireActivity(),R.color.colorPrimaryDark)
            activity.supportActionBar?.setBackgroundDrawable(ColorDrawable(statusBarColor))
            activity.window.statusBarColor = statusBarColor
        }
    }
}