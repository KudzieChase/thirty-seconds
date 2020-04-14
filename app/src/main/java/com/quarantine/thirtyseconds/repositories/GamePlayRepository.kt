package com.quarantine.thirtyseconds.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.quarantine.thirtyseconds.models.*
import com.quarantine.thirtyseconds.utils.DataSnapshotLiveData
import com.quarantine.thirtyseconds.utils.Result

class GamePlayRepository(
    val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) {
    private val members = GameMembers()
    private var gameStarted = false
    private var currentTeam = -1

    var playerIsCurrentDescriptor = false
    var playersTeamIsPlaying = false

    var teamAPoints = 0
    var teamBPoints = 0
    private val gamesReference = database.getReference("games")
    private var _key = "-M4siw7Rcv8vsXi4tLXg"
    private val key get() = _key

    private val messageReference = gamesReference.child(key).child("messages")
    private val joinRequestsRef = gamesReference.child(key).child("joinRequests")
    private val roundReference = gamesReference.child(key).child("currentRound")
    private val teamAScoreReference = gamesReference.child(key).child("teamA_score")
    private val teamBScoreReference = gamesReference.child(key).child("teamB_score")

    // Messages
    private val _messagesLiveData = MutableLiveData<Result<List<Message>>>()
    val messagesLiveData: LiveData<Result<List<Message>>>
        get() = _messagesLiveData

    private val joinEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
            val username = dataSnapshot.key!!
            if (members.addMember(username)) {
                gamesReference.child(key).updateChildren(
                    hashMapOf<String, Any>("members" to members)
                )
                sendBotMessage("$username has joined the game")
            }
            dataSnapshot.ref.removeValue()

            // If we have at least 4 members, the game may start
            if (members.size() >= 4 && !gameStarted) {
                sendBotMessage("Game is starting with\n$members")
                gameStarted = true
                // TODO: Start a round

            }
        }
        override fun onCancelled(p0: DatabaseError) { }
        override fun onChildMoved(p0: DataSnapshot, p1: String?) { }
        override fun onChildChanged(p0: DataSnapshot, p1: String?) { }
        override fun onChildRemoved(p0: DataSnapshot) { }
    }

    private val gameListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val game = dataSnapshot.getValue<Game>()!!

            // Messages
            val messages = arrayListOf<Message>()
            for (message in game.messages.values) {
                messages.add(message)
            }
            _messagesLiveData.value = Result.Success(messages)
        }

        override fun onCancelled(error: DatabaseError) {
            error.toException().printStackTrace()

            // Messages
            _messagesLiveData.value = Result.Error(error.toException())
        }
    }

    init {
        _messagesLiveData.value = Result.InProgress
    }

    fun newGame(): Task<Void> {
        //Creates a new game
        val game = Game()
        game.messages[messageReference.push().key!!] = Message(
            senderNickname = "gamebot",
            message = "You've created a new game. Invite your friends" +
                    " using the code " + key, // TODO: Use string resource instead
            type = MessageType.GAMEBOT,
            timestamp = System.currentTimeMillis()
        )
        members.addMember(auth.currentUser!!.displayName!!)
        game.members = members
        playerIsCurrentDescriptor = true
        playersTeamIsPlaying = true

        // Wait for join requests and add them to the members list
        joinRequestsRef.addChildEventListener(joinEventListener)
        gamesReference.child(key).addValueEventListener(gameListener)
        return gamesReference.child(key).setValue(game)
    }

    fun joinGame(gameKey: String) {
        _key = gameKey
        joinRequestsRef.child(gameKey).setValue(true)
        playerIsCurrentDescriptor = false
    }

    fun endGame(): Task<Void> {
        // Stop waiting for members to join
        joinRequestsRef.removeEventListener(joinEventListener)
        gamesReference.child(key).removeEventListener(gameListener)
        return gamesReference.child(key).child("gameOver").setValue(true)
    }

    fun newRound() {
        // Change teams
        currentTeam = if (currentTeam == -1 || currentTeam == 1) { 0 } else { 1 }

        // Get a new descriptor
        val descriptor = members.nextDescriptor(currentTeam)
        roundReference.updateChildren(
            hashMapOf<String, Any>(
                "currentDescriptor" to descriptor,
                "currentTeam" to currentTeam
            )
        )

        val team = if (currentTeam == 0) { "A" } else { "B"}
        sendBotMessage("It's Team $team's turn. " +
                "$descriptor will be describing the words. " +
                "Time started")

        // Display the words
        // Once the words are displayed, the fragment will start the timer

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