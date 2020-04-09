package com.quarantine.thirtyseconds.ui.intro

import android.app.Activity
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
import com.firebase.ui.auth.AuthUI
import com.quarantine.thirtyseconds.R
import com.quarantine.thirtyseconds.databinding.FragmentIntroBinding
import com.quarantine.thirtyseconds.utils.PreferencesUtils
import com.quarantine.thirtyseconds.utils.REQUEST_CODE_SIGN_IN

class IntroFragment : Fragment() {

    private var _binding: FragmentIntroBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewModel: IntroViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIntroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val preferences = PreferencesUtils(context)

        val factory = IntroViewModelFactory()
        viewModel = ViewModelProvider(this, factory).get(IntroViewModel::class.java)

        viewModel.isUserSignedIn.observe(viewLifecycleOwner, Observer { userIsSignedIn ->
            if (userIsSignedIn) {
                //Go to profile fragment when its first time otherwise moves in to home fragment
                if (preferences.isUserFirstTime()) {
                    findNavController().navigate(R.id.action_introFragment_to_profileFragment)
                } else {
                    findNavController().navigate(R.id.action_introFragment_to_homeFragment)
                }
            }
        })

        binding.apply {
            start.setOnClickListener {
                val authIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(listOf(AuthUI.IdpConfig.GoogleBuilder().build()))
                    .build()
                startActivityForResult(authIntent, REQUEST_CODE_SIGN_IN)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                viewModel.signInComplete()
            } else {
                // Something happened when trying to sign in
                Toast.makeText(context, R.string.error_occurred, Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}