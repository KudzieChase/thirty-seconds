package com.quarantine.thirtyseconds.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import coil.api.load
import coil.transform.CircleCropTransformation
import com.quarantine.thirtyseconds.R
import com.quarantine.thirtyseconds.databinding.FragmentProfileBinding
import com.quarantine.thirtyseconds.utils.NicknameTakenException
import com.quarantine.thirtyseconds.utils.REQUEST_CODE_IMAGE_CHOOSER
import com.quarantine.thirtyseconds.utils.Result

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = ProfileViewModelFactory()
        viewModel = ViewModelProvider(this, factory).get(ProfileViewModel::class.java)

        binding.apply {
            viewModel.currentUser.observe(viewLifecycleOwner, Observer { user ->
                user?.let {
                    username.editText?.setText(it.displayName)
                    profileImage.load(it.photoUrl) {
                        crossfade(true)
                        transformations(CircleCropTransformation())
                    }
                }
            })

            viewModel.uploadedPhotoUrl.observe(viewLifecycleOwner, Observer { result ->
                when (result) {
                    is Result.Success -> {
                        btnDone.isEnabled = true
                        profileImage.load(result.data) {
                            crossfade(true)
                            placeholder(R.drawable.ic_person_outline)
                            transformations(CircleCropTransformation())
                        }
                        Toast.makeText(context, R.string.photo_uploaded, Toast.LENGTH_SHORT)
                            .show()
                    }
                    is Result.InProgress -> {
                        // TODO: Maybe display a loading bar?
                        btnDone.isEnabled = false
                        Toast.makeText(context, "Uploading image", Toast.LENGTH_LONG)
                            .show()
                    }
                    is Result.Error -> {
                        btnDone.isEnabled = true
                        Toast.makeText(context, result.exception.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            })

            viewModel.profiledSaved.observe(viewLifecycleOwner, Observer { result ->
                when (result) {
                    is Result.Success -> {
                        if (result.data) {
                            findNavController().navigate(R.id.action_profileFragment_to_homeFragment)
                        }
                    }
                    is Result.InProgress -> {
                        // TODO: Display a progress bar maybe?
                        username.isEnabled = false
                        btnDone.isEnabled = false
                    }
                    is Result.Error -> {
                        username.isEnabled = true
                        btnDone.isEnabled = true
                        val toastMessage = if (result.exception is NicknameTakenException) {
                            getString(R.string.nickname_already_taken)
                        } else {
                            result.exception.message
                        }
                        Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show()

                    }
                }
            })

            addPlaceholder.setOnClickListener {
                val intent = Intent().apply {
                    type = "image/*"
                    action = Intent.ACTION_GET_CONTENT
                }
                startActivityForResult(intent, REQUEST_CODE_IMAGE_CHOOSER)
            }

            btnDone.setOnClickListener {
                val newNickname = username.editText?.text.toString()

                // TODO: Add extra validation to the username
                //  e.g. Not allowing spaces or special characters
                if (newNickname.isNotEmpty()) {
                    viewModel.saveProfile(newNickname)
                } else {
                    // TODO: Require the user to provide a nickname
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE_CHOOSER) {
            data?.data?.let {
                viewModel.uploadPhoto(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}