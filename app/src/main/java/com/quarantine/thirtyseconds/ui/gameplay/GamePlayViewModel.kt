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

    private val _playerIsCurrentDescriptor = MutableLiveData<Boolean>()
    val playerIsCurrentDescriptor: LiveData<Boolean>
        get() = _playerIsCurrentDescriptor

    private val _playersTeamIsPlaying = MutableLiveData<Boolean>()
    val playersTeamIsPlaying: LiveData<Boolean>
        get() = _playersTeamIsPlaying

    val messages = repository.getMessages()
    val words = repository.getWords()
    val time = repository.getTime()

    val currentUser = repository.getUser()
    private val username = repository.getUserNickName()

    private fun getTimer() = object : CountDownTimer(30000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            repository.setTime((millisUntilFinished/1000).toInt())
        }

        override fun onFinish() {
            // TODO: end round
        }
    }

    init {
        _gameCreated.value = Result.Success(false)
        _playerIsCurrentDescriptor.value = repository.playerIsCurrentDescriptor
        _playersTeamIsPlaying.value = repository.playersTeamIsPlaying
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
        getTimer().start()
    }

    fun sendDescriptorMessage(messageText: String) {
        val message = Message(
            senderNickname = username.value!!,
            message = messageText,
            type = MessageType.DESCRIPTION
        )
        repository.sendMessage(message)
    }

}