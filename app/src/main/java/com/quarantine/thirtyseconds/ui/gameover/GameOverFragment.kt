package com.quarantine.thirtyseconds.ui.gameover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.quarantine.thirtyseconds.databinding.FragmentGameOverBinding

class GameOverFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentGameOverBinding.inflate(inflater, container, false)
        binding.apply {

        }
        return binding.root
    }
}