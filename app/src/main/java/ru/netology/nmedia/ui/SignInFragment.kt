package ru.netology.nmedia.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentSignInBinding
import ru.netology.nmedia.utils.AndroidUtils
import ru.netology.nmedia.viewmodel.AuthViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SignInFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth
    private val viewModel: AuthViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       val binding = FragmentSignInBinding.inflate(
           inflater,
           container,
           false
       )

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

        binding.signIn.setOnClickListener {
            val login = binding.login.editText?.text.toString()
            val password = binding.password.editText?.text.toString()
            var isValid = true;
            if (login.isBlank()) {
                isValid = false;
                binding.login.error = getString(R.string.fill_login)
            }
            if (password.isBlank()) {
                isValid = false;
                binding.password.error = getString(R.string.fill_password)
            }
            if (!isValid) {
                return@setOnClickListener
            }
            appAuth.authorize(login, password)
            AndroidUtils.hideKeyboard(requireView())
        }

        viewModel.data.observe(viewLifecycleOwner) {
            if(viewModel.authenticated) {
                findNavController().navigate(R.id.fragment_posts_feed)
            }
        }

        return binding.root
    }

}