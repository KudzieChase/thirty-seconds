package com.quarantine.thirtyseconds.models

data class Game(
    val gameOver: Boolean = false,
    val currentRound: GameRound = GameRound(),
    val teams: List<Team> = listOf(Team(name = "Team A"), Team(name = "Team B")),
    val messages: HashMap<String, Message> = hashMapOf()
) {
    fun membersNumber() = teams[0].members.size + teams[1].members.size

    fun addMember(username: String): Boolean {
        if (!teams[0].hasMember(username) && !teams[1].hasMember(username)) {
            teams[LAST_ADDED_TEAM].addMember(username)
            nextTeam()
            return true
        }
        return false
    }

    fun nextDescriptor(currentTeam: Int): String {
        return if (currentTeam == 0) {
            nextTeamADescriptor()
        } else {
            nextTeamBDescriptor()
        }
    }

    fun teamsString(): String {
        return "${teams[0]}\n\n${teams[1]}"
    }

    private fun nextTeamADescriptor(): String {
        if (TEAM_A_DESCRIPTOR == teams[0].members.size - 1) {
            TEAM_A_DESCRIPTOR = 0
        } else {
            TEAM_A_DESCRIPTOR++
        }
        return teams[0].members[TEAM_A_DESCRIPTOR]
    }

    private fun nextTeamBDescriptor(): String {
        if (TEAM_B_DESCRIPTOR == teams[1].members.size - 1) {
            TEAM_B_DESCRIPTOR = 0
        } else {
            TEAM_B_DESCRIPTOR++
        }
        return teams[1].members[TEAM_B_DESCRIPTOR]
    }

    private fun nextTeam() {
        LAST_ADDED_TEAM = if (LAST_ADDED_TEAM == 0) { 1 } else { 0 }
    }

    companion object {
        // 0 means Team A
        // 1 means Team B
        var LAST_ADDED_TEAM = 0
        var TEAM_A_DESCRIPTOR = -1
        var TEAM_B_DESCRIPTOR = -1
    }
}