package ru.netology.nmedia.ui

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.viewmodel.EventsViewModel
import java.util.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.databinding.FragmentNewEventBinding
import ru.netology.nmedia.utils.AndroidUtils
import ru.netology.nmedia.utils.DateUtils.convertToDate
import ru.netology.nmedia.utils.DateUtils.convertToDateTime
import ru.netology.nmedia.utils.DateUtils.convertToInstant
import ru.netology.nmedia.utils.StringArg
import java.time.*


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NewEventFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu,inflater)
        inflater.inflate(R.menu.menu_new_item, menu)
    }

    private var fragmentBinding: FragmentNewEventBinding? = null


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {
                var isValid = true
                fragmentBinding?.apply{
                    if (edit.editText?.text?.isBlank() == true) {
                        edit.error = getString(R.string.fill_event_content)
                        isValid = false
                    }
                    if (date.editText?.text?.isBlank() == true) {
                        date.error = getString(R.string.fill_event_date)
                        isValid = false
                    }
                    if (time.editText?.text?.isBlank() == true) {
                        time.error = getString(R.string.fill_event_time)
                        isValid = false
                    }
                    if (!isValid) {
                        return true
                    }
                        viewModel.changeContent(
                            edit.editText?.text.toString(),
                            "${date.editText?.text.toString()} ${time.editText?.text.toString()}".convertToInstant(),
                            resources.getResourceEntryName(type.checkedRadioButtonId).uppercase(),
                        )
                    viewModel.save()
                    AndroidUtils.hideKeyboard(requireView())
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        var Bundle.textArg: String? by StringArg
        var Bundle.datetimeArg: String? by StringArg
        var Bundle.typeArg: String? by StringArg
        var Bundle.photoArg: String? by StringArg
    }

    private val viewModel: EventsViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)

        val binding = FragmentNewEventBinding.inflate(
            inflater,
            container,
            false
        )

        arguments?.datetimeArg?.let {
            val date = it.convertToDateTime()?: " "
            val split = date.split(" ")
            binding.date.editText?.setText(split[0])
            binding.time.editText?.setText(split[1])
        }

        arguments?.typeArg?.let {
            resources.getIdentifier(it.lowercase(), "id", context?.packageName).let { id->
                val radio: RadioButton = binding.root.findViewById(id)
                radio.isChecked = true
            }
        }
        arguments?.textArg?.let {
            binding.edit.editText?.setText(it)
        }

        arguments?.photoArg?.let { url ->
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

        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.choose_date))
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()


        binding.date.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.date.error = null
            }
        }

        binding.time.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.time.error = null
            }
        }

        binding.date.editText?.setOnClickListener { _ ->
            binding.date.error = null
            activity?.supportFragmentManager?.let {
                if (!datePicker.isAdded) {
                    datePicker.show(it, "MATERIAL_DATE_PICKER_START")
                }
            }
        }

        datePicker.addOnPositiveButtonClickListener {
            binding.date.editText?.setText(it.convertToDate())
        }


        val timePicker =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setTitleText(getString(R.string.choose_time))
                .build()

        binding.time.editText?.setOnClickListener { _ ->
            binding.time.error = null
            activity?.supportFragmentManager?.let {
                if (!timePicker.isAdded) {
                    timePicker.show(it, "MATERIAL_PICKER_PICKER_START")
                }
            }
        }

        timePicker.addOnPositiveButtonClickListener { _ ->
            binding.time.editText?.setText(getString(R.string.datetime_format, "%02d".format(timePicker.hour), "%02d".format(timePicker.minute)))
        }

        binding.date.setEndIconOnClickListener {
            it?.setOnClickListener { v ->
                (v as TextInputLayout?)?.editText?.setText("")
            }
        }

        binding.time.setEndIconOnClickListener {
            it?.setOnClickListener { v ->
                (v as TextInputLayout?)?.editText?.setText("")
            }
        }
        binding.edit.setOnClickListener {

                binding.edit.error = null

        }


        viewModel.eventCreated.observe(viewLifecycleOwner) {
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

