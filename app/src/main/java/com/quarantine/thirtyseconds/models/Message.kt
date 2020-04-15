package com.quarantine.thirtyseconds.models

import com.google.firebase.database.ServerValue

data class Message(
    var senderNickname: String = "",
    val type: MessageType = MessageType.GAMEBOT,
    val message: String = "",
    val timestamp: Long = 0L
) {
    fun toMap() = hashMapOf(
        "senderNickname" to senderNickname,
        "type" to type,
        "message" to message,
        "timestamp" to ServerValue.TIMESTAMP
    )
}