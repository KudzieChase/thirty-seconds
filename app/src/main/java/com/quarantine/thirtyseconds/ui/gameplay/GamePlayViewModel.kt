package com.quarantine.thirtyseconds.ui.gameplay

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.quarantine.thirtyseconds.models.Message
import com.quarantine.thirtyseconds.models.MessageType
import com.quarantine.thirtyseconds.repositories.GamePlayRepository
import com.quarantine.thirtyseconds.utils.Result

class GamePlayViewModel(
    private val repository: GamePlayRepository
) : ViewModel() {

    private val _gameCreated = MutableLiveData<Result<Boolean>>()
    val gameCreated: MutableLiveData<Result<Boolean>>
        get() = _gameCreated

    val playerIsCurrentDescriptor: LiveData<Boolean> = repository.playerIsCurrentDescriptor
    val playersTeamIsPlaying: LiveData<Boolean> = repository.playersTeamIsPlaying
    val messages = repository.messagesLiveData
    val words = repository.wordsLiveData
    val time = repository.timeLiveData
    var timeStarted = false

    private var timer: CountDownTimer? = null

    init {
        _gameCreated.value = Result.Success(false)
    }

    fun startNewGame() {
        _gameCreated.value = Result.InProgress
        repository.newGame().addOnSuccessListener {
            _gameCreated.value = Result.Success(true)
        }.addOnFailureListener {
            _gameCreated.value = Result.Error(it)
        }
    }

    fun startTimer() {
        timeStarted = true
        if (timer == null) {
            timer = object : CountDownTimer(31000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    repository.setTime((millisUntilFinished/1000).toInt())
                }

                override fun onFinish() {
                    timer = null
                    repository.sendBotMessage("Time is up!")
                    repository.endRound()
                    repository.sendBotMessage(repository.getScores())
                    repository.newRound().addOnCompleteListener {
                        timeStarted = false
                    }
                }
            }
        }
        timer?.start()
    }

    fun sendDescriptorMessage(messageText: String) {
        // Check if descriptor gave away the answer
        if (repository.descriptionIsValid(messageText)) {
            val message = Message(
                message = messageText,
                type = MessageType.DESCRIPTION
            )
            repository.sendMessage(message)
        } else {
            repository.sendBotMessage("Descriptor typed one of the words in the card." +
                    " Their team lost 1 point")
            repository.decrementScore()
        }
    }

}