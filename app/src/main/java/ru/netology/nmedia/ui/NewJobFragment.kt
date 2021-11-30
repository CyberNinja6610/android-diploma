package ru.netology.nmedia.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewJobBinding
import ru.netology.nmedia.utils.AndroidUtils
import ru.netology.nmedia.utils.DateUtils.convertToDate
import ru.netology.nmedia.utils.DateUtils.convertToTimestamp
import ru.netology.nmedia.utils.LongArg
import ru.netology.nmedia.utils.StringArg
import ru.netology.nmedia.viewmodel.JobViewModel


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NewJobFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    private val viewModel: JobViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    companion object {
        var Bundle.startArg: Long? by LongArg
        var Bundle.finishArg: Long? by LongArg
        var Bundle.nameArg: String? by StringArg
        var Bundle.positionArg: String? by StringArg
        var Bundle.siteArg: String? by StringArg
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_new_item, menu)
    }

    private var fragmentBinding: FragmentNewJobBinding? = null


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {
                fragmentBinding?.let { binding ->
                    val start = binding.start.editText?.text.toString()
                    val position = binding.position.editText?.text.toString()
                    val name = binding.name.editText?.text.toString()
                    var isValid = true;
                    if (start.isBlank()) {
                        binding.start.error = getString(R.string.choose_date)
                        isValid = false
                    }
                    if (position.isBlank()) {
                        binding.position.error = getString(R.string.edit_position)
                        isValid = false
                    }
                    if (name.isBlank()) {
                        binding.name.error = getString(R.string.edit_company)
                        isValid = false
                    }
                    if (!isValid) {
                        return true
                    }
                    viewModel.changeContent(
                        start.convertToTimestamp()?: 0L,
                        name,
                        position,
                        if (binding.finish.editText?.text.isNullOrBlank()) null
                        else binding.finish.editText?.text.toString().convertToTimestamp(),
                        if (binding.link.editText?.text.isNullOrBlank()) null else binding.link.editText?.text.toString(),
                    )
                    viewModel.save()
                    AndroidUtils.hideKeyboard(requireView())
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)

        val binding = FragmentNewJobBinding.inflate(
            inflater,
            container,
            false
        )

        arguments?.apply {
            nameArg?.let { value ->
                binding.name.editText?.setText(value)
            }
            positionArg?.let { value ->
                binding.position.editText?.setText(value)
            }
            startArg?.let { value ->
                binding.start.editText?.setText(value.convertToDate())
            }

            siteArg?.let { value ->
                binding.link.editText?.setText(value)
            }
            finishArg?.let { value ->
                if (value == 0L) {
                    binding.finish.editText?.setText("")
                } else {
                    binding.finish.editText?.setText(value.convertToDate())
                }
            }
        }

        val datePickerStart =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.choose_date))
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

        val datePickerEnd =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.choose_date))
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

        datePickerStart.addOnPositiveButtonClickListener {
            binding.start.editText?.setText(it.convertToDate())
        }
        datePickerEnd.addOnPositiveButtonClickListener {
            binding.finish.editText?.setText(it.convertToDate())
        }

        binding.start.editText?.setOnClickListener { _ ->
            binding.finish.error = null
            activity?.supportFragmentManager?.let {
                if (!datePickerStart.isAdded) {
                    datePickerStart.show(it, "MATERIAL_DATE_PICKER_START")
                }
            }
        }

        binding.start.setEndIconOnClickListener {
            it?.setOnClickListener { v ->
                (v as TextInputLayout?)?.editText?.setText("")
            }
        }

        binding.finish.editText?.setOnClickListener { _ ->
            activity?.supportFragmentManager?.let {
                if (!datePickerEnd.isAdded) {
                    datePickerEnd.show(it, "MATERIAL_DATE_PICKER_END")
                }
            }
        }

        binding.finish.setEndIconOnClickListener {
            it?.setOnClickListener { v ->
                (v as TextInputLayout?)?.editText?.setText("")
            }
        }

        binding.start.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.start.error = null;
            }
        }

        binding.finish.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.finish.error = null;
            }
        }

        binding.position.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.position.error = null;
            }
        }

        binding.name.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.name.error = null;
            }
        }

        viewModel.jobCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        viewModel.dataState.observe(viewLifecycleOwner) {
            if (it.error) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.api_error),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
        fragmentBinding = binding
        return binding.root
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}
