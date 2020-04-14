package com.quarantine.thirtyseconds.ui.gameplay

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.quarantine.thirtyseconds.models.Message
import com.quarantine.thirtyseconds.models.MessageType
import com.quarantine.thirtyseconds.repositories.GamePlayRepository
import com.quarantine.thirtyseconds.utils.Result
import java.sql.Timestamp

class GamePlayViewModel(
    private val repository: GamePlayRepository
) : ViewModel() {

    private val _gameCreated = MutableLiveData<Result<Boolean>>()
    val gameCreated: MutableLiveData<Result<Boolean>>
        get() = _gameCreated

    private val _messageSent = MutableLiveData<Result<Boolean>>()
    val messageSent: MutableLiveData<Result<Boolean>>
        get() = _messageSent

    val currentUser = repository.getUser()
    private val username = repository.getUserNickName()

    init {
        _messageSent.value = Result.Success(false)
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


    fun sendMessage(messageText: String) {
        val message = Message(
            senderNickname = username.value!!,
            message = messageText,
            type = MessageType.INTERPRETATION,
            timestamp = System.currentTimeMillis()
        )
        _messageSent.value = Result.InProgress
        
        repository.sendMessage(message).addOnSuccessListener {
            _messageSent.value = Result.Success(true)
        }.addOnFailureListener {
            _messageSent.value = Result.Error(it)
        }
    }

    fun retrieveMessages() {
        //TODO retrieve some messages
    }

}