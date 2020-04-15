package com.quarantine.thirtyseconds.ui.gameplay

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.quarantine.thirtyseconds.R
import com.quarantine.thirtyseconds.models.Message
import com.quarantine.thirtyseconds.models.MessageType
import com.quarantine.thirtyseconds.repositories.GamePlayRepository
import com.quarantine.thirtyseconds.utils.Result

class GamePlayViewModel(
    private val context: Application,
    private val gameKey: String? = null,
    private val repository: GamePlayRepository
) : AndroidViewModel(context) {

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
                    repository.sendBotMessage(context.getString(R.string.time_is_up))
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
            repository.sendBotMessage(context.getString(R.string.descriptor_gave_away_the_answer))
            repository.decrementScore()
        }
    }

}