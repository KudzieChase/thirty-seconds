package com.quarantine.thirtyseconds.models

data class Message(
    val senderNickname: String = "",
    val type: MessageType = MessageType.GAMEBOT,
    val message: String = "",
    val timestamp: Long = 0L
)