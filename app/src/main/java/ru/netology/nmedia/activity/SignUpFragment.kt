package ru.netology.nmedia.activity

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentSignUpBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.viewmodel.AuthViewModel

class SignUpFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
    )

    private var fragmentBinding: FragmentSignUpBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.removeGroup(R.id.unauthenticated)
    }

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
        fragmentBinding = binding


        binding.name.requestFocus()




        viewModel.changePhoto(null)

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
                    Activity.RESULT_OK -> {
                        val uri: Uri? = it.data?.data
                        viewModel.changePhoto(uri)
                    }
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
        }





        binding.signUp.setOnClickListener {
            AndroidUtils.hideKeyboard(requireView())
            if (binding.login.text.isNullOrBlank() || binding.password.text.isNullOrBlank() || binding.name.text.isNullOrBlank()) {
                Toast.makeText(
                    requireActivity(),
                    getString(R.string.emptyFieldsError),
                    Toast.LENGTH_LONG
                )
                    .show()
            } else if (binding.password.text.toString() != binding.confirmPassword.text.toString()) {
                Toast.makeText(
                    requireActivity(),
                    getString(R.string.differentPasswords),
                    Toast.LENGTH_LONG
                )
                    .show()
            } else {
                viewModel.signUp(
                    login = binding.login.text.toString(),
                    pass = binding.password.text.toString(),
                    name = binding.name.text.toString()
                )
            }
        }

        viewModel.authRespState.observe(viewLifecycleOwner) {
            if (it.isLoading) {
                binding.progress.visibility = View.VISIBLE
                binding.signUp.isEnabled = false
            } else {
                binding.progress.visibility = View.GONE
                binding.signUp.isEnabled = true
                if (it.error != null) {
                    Toast.makeText(requireActivity(), it.error, Toast.LENGTH_LONG)
                        .show()
                }
            }
        }

        viewModel.authDone.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}