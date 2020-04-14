package com.quarantine.thirtyseconds.ui.gameplay

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.quarantine.thirtyseconds.databinding.FragmentGamePlayBinding
import com.quarantine.thirtyseconds.ui.profile.ProfileViewModel
import com.quarantine.thirtyseconds.utils.Result

class GamePlayFragment : Fragment() {

    private var _binding: FragmentGamePlayBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GamePlayViewModel by viewModels {
        GamePlayViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGamePlayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {

            viewModel.startNewGame()
            viewModel.gameCreated.observe(viewLifecycleOwner, Observer { result ->
                when (result) {
                    is Result.Success -> {
                        if (result.data) {
                            container.visibility = View.VISIBLE
                        }
                    }
                    is Result.InProgress -> {
                        //TODO make a progress loader
                        container.visibility = View.GONE
                    }
                    is Result.Error -> {
                        Toast.makeText(context, "Seems something went wrong", Toast.LENGTH_SHORT)
                            .show()
                        container.visibility = View.GONE
                    }
                }
            })

            viewModel.messageSent.observe(viewLifecycleOwner, Observer { result ->
                when (result) {
                    is Result.Success -> {
                        if (result.data) {
                            Toast.makeText(context, "Message sent", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    is Result.InProgress -> {
                        Toast.makeText(context, "Sending message", Toast.LENGTH_SHORT)
                            .show()
                    }
                    is Result.Error -> {
                        Toast.makeText(context, "Seems something went wrong", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            })

            btnSend.setOnClickListener {
                if (editTextChat.text.toString().isNotEmpty()) {
                    val message = editTextChat.text.toString()
                    viewModel.sendMessage(message)
                    editTextChat.text.clear()
                }
            }

            object : CountDownTimer(30000, 1000) {
                override fun onFinish() {
                }

                override fun onTick(millis: Long) {
                    timeText.text = "${millis / 1000}"
                }
            }.start()

        }
    }
}