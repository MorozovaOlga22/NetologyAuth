package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentRegistrationBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.viewmodel.AuthViewModel

class RegistrationFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
    )

    private var fragmentBinding: FragmentRegistrationBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRegistrationBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentBinding = binding

        binding.login.requestFocus()

        binding.signIn.setOnClickListener {
            AndroidUtils.hideKeyboard(requireView())
            if (binding.login.text.isNullOrBlank() || binding.password.text.isNullOrBlank()) {
                Toast.makeText(requireActivity(), getString(R.string.emptyLoginPasswordError), Toast.LENGTH_LONG)
                    .show()
            } else {
                viewModel.signIn(
                    login = binding.login.text.toString(),
                    pass = binding.password.text.toString()
                )
            }
        }

        viewModel.authRespState.observe(viewLifecycleOwner) {
            if (it.isLoading) {
                binding.progress.visibility = View.VISIBLE
                binding.signIn.isEnabled = false
            } else {
                binding.progress.visibility = View.GONE
                binding.signIn.isEnabled = true
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