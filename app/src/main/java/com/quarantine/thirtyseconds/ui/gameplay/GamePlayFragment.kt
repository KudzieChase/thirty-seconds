package com.quarantine.thirtyseconds.ui.gameplay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.quarantine.thirtyseconds.databinding.FragmentGamePlayBinding

class GamePlayFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentGamePlayBinding.inflate(inflater, container, false)

        binding.apply {

        }

        return binding.root
    }
}