package com.quarantine.thirtyseconds.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.quarantine.thirtyseconds.models.Game
import com.quarantine.thirtyseconds.models.Message
import com.quarantine.thirtyseconds.utils.DataSnapshotLiveData

class GamePlayRepository(
    val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) {

    var teamAPoints = 0
    var teamBPoints = 0
    private val gamesReference = database.getReference("games")
    private val _key = gamesReference.push().key!!
    private val key get() = _key

    private val messageReference = gamesReference.child(key).child("messages")
    private val roundReference = gamesReference.child(key).child("currentRound")
    private val teamAScoreReference = gamesReference.child(key).child("teamA_score")
    private val teamBScoreReference = gamesReference.child(key).child("teamB_score")

    fun newGame(): Task<Void> {
        //Creates a new game
        return gamesReference.child(key).setValue(Game())
    }

    fun endGame(): Task<Void> {
        return gamesReference.child(key).child("gameOver").setValue(true)
    }

    fun incrementTeamAScore(points: Int): Task<Void> {
        teamAPoints += points
        return teamAScoreReference.setValue(teamAPoints)
    }

    fun decrementTeamAScore(points: Int): Task<Void> {
        teamAPoints -= points
        return teamAScoreReference.setValue(teamAPoints)
    }

    fun incrementTeamBScore(points: Int): Task<Void> {
        teamBPoints += points
        return teamBScoreReference.setValue(teamBPoints)
    }

    fun decrementTeamBScore(points: Int): Task<Void> {
        teamBPoints -= points
        return teamBScoreReference.setValue(teamBPoints)
    }

    fun queryTeamAPoints(): DataSnapshotLiveData {
        val query = teamAScoreReference
        return DataSnapshotLiveData(query)
    }

    fun queryTeamBPoints(): DataSnapshotLiveData {
        val query = teamBScoreReference
        return DataSnapshotLiveData(query)
    }

    fun sendMessage(message: Message): Task<Void> {
        //Generate a new message key and sends a message
        val messageKey = messageReference.push().key!!
        return messageReference.child(messageKey).setValue(message)
    }

    fun getUser(): LiveData<FirebaseUser?> {
        return MutableLiveData<FirebaseUser?>().apply { value = auth.currentUser }
    }

    fun getUserNickName(): LiveData<String> {
        return MutableLiveData<String>().apply { getUser().value.let { value = it?.displayName } }
    }

    companion object {
        @Volatile
        private var instance: GamePlayRepository? = null

        fun getInstance(auth: FirebaseAuth, database: FirebaseDatabase): GamePlayRepository? {
            return instance ?: synchronized(GamePlayRepository::class.java) {
                if (instance == null) {
                    instance = GamePlayRepository(auth, database)
                }
                return instance
            }
        }
    }

}