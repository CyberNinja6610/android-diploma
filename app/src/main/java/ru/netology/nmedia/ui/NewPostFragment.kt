package ru.netology.nmedia.ui

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.utils.AndroidUtils
import ru.netology.nmedia.utils.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel
import java.io.*


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NewPostFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_new_item, menu)
    }
    private var fragmentBinding: FragmentNewPostBinding? = null
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {
                if (fragmentBinding?.edit?.text?.isBlank() == true) {
                    Toast.makeText(
                        requireContext(),
                        R.string.error_empty_content,
                        Toast.LENGTH_LONG
                    ).show()
                    return true
                }
                fragmentBinding?.let {
                    viewModel.changeContent(it.edit.text.toString())
                    viewModel.save()
                    AndroidUtils.hideKeyboard(requireView())
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        var Bundle.textArg: String? by StringArg
        var Bundle.photoArg: String? by StringArg
    }

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)

        val binding = FragmentNewPostBinding.inflate(
            inflater,
            container,
            false
        )
        viewModel.changePhoto(null)


        arguments?.textArg?.let(binding.edit::setText)

        arguments?.photoArg?.let { url->
            val uri = Uri.parse(url)
            viewModel.changePhoto(uri)
        }

        fragmentBinding = binding

        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    Activity.RESULT_OK -> viewModel.changePhoto(it.data?.data)
                }
            }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .galleryOnly()
                .galleryMimeTypes(
                    arrayOf(
                        "image/png",
                        "image/jpeg",
                    )
                )
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .cameraOnly()
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.removePhoto.setOnClickListener {
            viewModel.changePhoto(null)
        }
        viewModel.photo.observe(viewLifecycleOwner) {


            if (it.uri == null) {
                binding.photoContainer.visibility = View.GONE
                return@observe
            }

            binding.photoContainer.visibility = View.VISIBLE
            binding.photo.setImageURI(it.uri)
            Glide.with(binding.photo)
                .load("${it.uri}")
                .error(R.drawable.ic_baseline_error_24)
                .timeout(10_000)
                .into(binding.photo)

        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            viewModel.load()
            findNavController().navigateUp()
        }
        return binding.root
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}
