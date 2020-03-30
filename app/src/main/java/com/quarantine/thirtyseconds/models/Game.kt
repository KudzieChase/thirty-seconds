package com.quarantine.thirtyseconds.models

data class Game(
    val gameOver: Boolean = false,
    val members: GameMembers = GameMembers(),
    val teamA_score: Int = 0,
    val teamB_score: Int = 0,
    val currentRound: GameRound = GameRound(),
    val messages: List<Message> = listOf()
)