package com.quarantine.thirtyseconds.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import com.quarantine.thirtyseconds.models.Game
import com.quarantine.thirtyseconds.models.GameCard
import com.quarantine.thirtyseconds.models.Message
import com.quarantine.thirtyseconds.models.MessageType
import com.quarantine.thirtyseconds.utils.DataSnapshotLiveData
import com.quarantine.thirtyseconds.utils.Result

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
        return messageReference.push().setValue(message.toMap())
    }

    fun sendBotMessage(messageText: String) {
        val message = Message(
            senderNickname = "gamebot",
            message = messageText,
            type = MessageType.GAMEBOT
        )
        sendMessage(message)
    }

    fun getMessages(): LiveData<Result<List<Message>>> {
        val liveData = DataSnapshotLiveData(messageReference, true)
        return Transformations.map(liveData) { result ->
            when (result) {
                is Result.Success -> {
                    val snapshot  = result.data
                    val list = ArrayList<Message>()
                    for (snap in snapshot.children) {
                        list.add(snap.getValue<Message>()!!)
                    }
                    return@map Result.Success(list)
                }
                is Result.InProgress -> return@map Result.InProgress
                is Result.Error -> return@map Result.Error(result.exception)
            }
        }
    }

    fun getWords(): LiveData<Result<List<GameCard>>> {
        // TODO: Load the words from database
        val wordsLiveData = MutableLiveData<Result<List<GameCard>>>()
        wordsLiveData.value = Result.Success(
            listOf(
                GameCard("Kendrick Lamar", true),
                GameCard("Rihanna", true),
                GameCard("PC", true),
                GameCard("Car", false),
                GameCard("Harare", true)
            )
        )
        return wordsLiveData
    }

    fun getTime(): LiveData<Int> {
        val ref = gamesReference.child(key).child("currentRound")
            .child("timeRemaining")
        val liveData = DataSnapshotLiveData(ref, true)
        return Transformations.map(liveData) { result ->
            when (result) {
                is Result.Success -> {
                    val snapshot = result.data
                    return@map snapshot.getValue<Int>()
                }
                is Result.InProgress -> {
                    return@map 0
                }
                is Result.Error -> {
                    // Might be helpful to return -1
                    // in case of error
                    return@map 0
                }
            }
        }
    }

    fun setTime(secondsRemaining: Int) {
        gamesReference.child(key).child("currentRound")
            .child("timeRemaining").setValue(secondsRemaining)
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