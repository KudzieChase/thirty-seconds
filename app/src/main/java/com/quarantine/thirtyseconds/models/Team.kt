package com.quarantine.thirtyseconds.models

data class Team(
    val name: String = "",
    var score: Int = 0,
    val members: ArrayList<String> = arrayListOf()
) {
    fun hasMember(username: String) = members.contains(username)

    fun addMember(username: String) = members.add(username)

    fun removeMember(username: String) = members.remove(username)

    override fun toString(): String {
        var memberNames = ""
        for (member in members) {
            memberNames += "$member;"
        }
        return "$name: $memberNames"
    }
}
