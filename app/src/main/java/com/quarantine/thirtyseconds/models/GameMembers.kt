package com.quarantine.thirtyseconds.models

data class GameMembers(
    val teamA: ArrayList<String> = arrayListOf(),
    val teamB: ArrayList<String> = arrayListOf()
) {
    fun size() = teamA.size + teamB.size

    fun nextTeamADescriptor(): String {
        if (TEAM_A_DESCRIPTOR == teamA.size - 1) {
            TEAM_A_DESCRIPTOR = 0
        } else {
            TEAM_A_DESCRIPTOR++
        }
        return teamA[TEAM_A_DESCRIPTOR]
    }

    fun nextTeamBDescriptor(): String {
        if (TEAM_B_DESCRIPTOR == teamB.size - 1) {
            TEAM_B_DESCRIPTOR = 0
        } else {
            TEAM_B_DESCRIPTOR++
        }
        return teamB[TEAM_B_DESCRIPTOR]
    }

    fun addMember(username: String): Boolean {
        var memberWasAdded = false
        if (LAST_ADDED_TEAM == 0) {
            if (!teamA.contains(username) && !teamB.contains(username)) {
                teamA.add(username)
                memberWasAdded = true
            }
        } else {
            if (!teamA.contains(username) && !teamB.contains(username)) {
                teamB.add(username)
                memberWasAdded = true
            }
        }
        if (memberWasAdded) {
            nextTeam()
        }
        return memberWasAdded
    }

    private fun nextTeam() {
        LAST_ADDED_TEAM = if (LAST_ADDED_TEAM == 0) { 1 } else { 0 }
    }

    override fun toString(): String {
        var members = "Team A: "
        for (member in teamA) { members += "$member;" }
        members += "\n\nTeam B: "
        for (member in teamB) { members += "$member;" }
        return members
    }

    companion object {
        // 0 means Team A
        // 1 means Team B
        var LAST_ADDED_TEAM = 0
        var TEAM_A_DESCRIPTOR = -1
        var TEAM_B_DESCRIPTOR = -1
    }
}