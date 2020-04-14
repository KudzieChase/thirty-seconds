package com.quarantine.thirtyseconds.models

data class GameRound(
    val currentTeam: Int = 0,
    val currentDescriptor: String = "",
    val displayedWords: HashMap<String, GameCard> = hashMapOf(),
    val timeRemaining: Int = 30,
    val rolledDice: Int = 0,
    val roundOver: Boolean = false
)