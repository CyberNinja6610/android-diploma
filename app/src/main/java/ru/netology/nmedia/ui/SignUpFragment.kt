package ru.netology.nmedia.ui

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentSignUpBinding
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.utils.AndroidUtils.hideKeyboard
import ru.netology.nmedia.viewmodel.AuthViewModel
import javax.inject.Inject


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SignUpFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    private val authViewModel: AuthViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignUpBinding.inflate(
            inflater,
            container,
            false
        )

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
                    Activity.RESULT_OK -> authViewModel.changePhoto(it.data?.data)
                }
            }


        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.GALLERY)
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
                .provider(ImageProvider.CAMERA)
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.login.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.login.error = null
            }
        }

        binding.password.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.password.error = null
            }
        }

        binding.confirmPassword.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.confirmPassword.error = null
            }
        }

        binding.name.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.name.error = null
            }
        }

        binding.signUp.setOnClickListener {
            val login = binding.login.editText?.text.toString()
            val password = binding.password.editText?.text.toString()
            val confirmPassword = binding.confirmPassword.editText?.text.toString()
            val name = binding.name.editText?.text.toString()

            var isValid = true
            if (login.isBlank()) {
                isValid = false
                binding.login.error = getString(R.string.fill_login)
            }
            if (password.isBlank()) {
                isValid = false
                binding.password.error = getString(R.string.fill_password)
            }

            if (confirmPassword.isBlank()) {
                isValid = false
                binding.confirmPassword.error = getString(R.string.fill_confirm_password)
            }
            if (name.isBlank()) {
                isValid = false
                binding.name.error = getString(R.string.fill_name)
            }

            if (password.isNotBlank() && confirmPassword.isNotBlank() && password != confirmPassword) {
                isValid = false
                binding.password.error = getString(R.string.passwords_not_equal)
                binding.confirmPassword.error = getString(R.string.passwords_not_equal)
            }


            if (!isValid) {
                return@setOnClickListener
            }

            appAuth.setRegistration(
                login,
                password,
                name,
                authViewModel.photo.value?.uri?.toFile()?.let {
                    val upload = MediaUpload(it)
                    MultipartBody.Part.createFormData(
                        "file", upload.file.name, upload.file.asRequestBody()
                    )
                })
        }

        authViewModel.photo.observe(viewLifecycleOwner) {
            if (it?.uri != null) {
                Glide.with(binding.photo)
                    .load(it.uri)
                    .placeholder(R.drawable.ic_add_an_avatar_96)
                    .timeout(10_000)
                    .transform(MultiTransformation(FitCenter(), CircleCrop()))
                    .into(binding.photo)
                binding.removePhoto.isVisible = true
            } else {
                binding.photo.setImageResource(R.drawable.ic_add_an_avatar_96)
                binding.removePhoto.isVisible = false
            }
        }

        binding.removePhoto.setOnClickListener {
            authViewModel.changePhoto(null)
        }

        authViewModel.data.observe(viewLifecycleOwner) {
            if (authViewModel.authenticated) {
                hideKeyboard(requireView())
                authViewModel.changePhoto(null)
                findNavController().navigate(R.id.fragment_posts_feed)
            }
        }

        return binding.root
    }
}