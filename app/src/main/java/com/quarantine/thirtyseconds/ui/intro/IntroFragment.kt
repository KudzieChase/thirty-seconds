package com.quarantine.thirtyseconds.ui.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.quarantine.thirtyseconds.databinding.FragmentIntroBinding

class IntroFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentIntroBinding.inflate(inflater, container, false)
        binding.apply {
            start.setOnClickListener {
                Toast.makeText(context, "Start Here", Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }
}