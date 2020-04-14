package com.quarantine.thirtyseconds.ui.gameplay

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.quarantine.thirtyseconds.databinding.FragmentGamePlayBinding
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
            val messagesAdapter = MessagesAdapter(listOf())
            messageList.layoutManager = LinearLayoutManager(context)
            messageList.adapter = messagesAdapter

            val wordsAdapter = GameCardAdapter()
            wordsList.layoutManager = LinearLayoutManager(context)
            wordsList.adapter = wordsAdapter

            viewModel.startNewGame()

            viewModel.time.observe(viewLifecycleOwner, Observer { secondsRemaining ->
                timeText.text = "$secondsRemaining"
            })

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

            viewModel.messages.observe(viewLifecycleOwner, Observer { result ->
                when (result) {
                    is Result.Success -> {
                        messagesAdapter.updateMessages(result.data)
                        messageList.smoothScrollToPosition(result.data.size - 1)
                    }
                    is Result.InProgress -> {
                        messagesAdapter.updateMessages(listOf())
                    }
                    is Result.Error -> {
                        messagesAdapter.updateMessages(listOf())
                        result.exception.message?.let {
                            Log.e("GamePlayFragment", it)
                        }
                    }
                }
            })

            viewModel.words.observe(viewLifecycleOwner, Observer { words ->
                wordsAdapter.submitList(words)
            })

            btnSend.setOnClickListener {
                if (editTextChat.text.toString().isNotEmpty()) {
                    val message = editTextChat.text.toString()
                    viewModel.sendDescriptorMessage(message)
                    editTextChat.text.clear()
                }
            }

        }
    }
}