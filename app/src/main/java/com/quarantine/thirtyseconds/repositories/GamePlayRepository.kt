package com.quarantine.thirtyseconds.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    private var game = Game()
    private var gameStarted = false
    private var currentTeam = -1

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

    // Which team is playing
    private val _playersTeamIsPlaying = MutableLiveData<Boolean>()
    val playersTeamIsPlaying: LiveData<Boolean>
        get() = _playersTeamIsPlaying

    // Descriptor vs Interpreter
    private val _playerIsCurrentDescriptor = MutableLiveData<Boolean>()
    val playerIsCurrentDescriptor: LiveData<Boolean>
        get() = _playerIsCurrentDescriptor

    // Words
    private val _wordsLiveData = MutableLiveData<List<GameCard>>()
    val wordsLiveData: LiveData<List<GameCard>>
            get() = _wordsLiveData

    // Time
    private val _timeLiveData = MutableLiveData<Int>()
    val timeLiveData: LiveData<Int>
        get() = _timeLiveData

    // Messages
    private val _messagesLiveData = MutableLiveData<Result<List<Message>>>()
    val messagesLiveData: LiveData<Result<List<Message>>>
        get() = _messagesLiveData

    private val joinEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
            val username = dataSnapshot.key!!
            if (game.addMember(username)) {
                gamesReference.child(key).updateChildren(
                    hashMapOf<String, Any>("teams" to game.teams)
                )
                sendBotMessage("$username has joined the game")
            }
            dataSnapshot.ref.removeValue()

            // If we have at least 4 members, the game may start
            if (game.membersNumber() >= 4 && !gameStarted) {
                sendBotMessage("Game is starting with\n${game.teamsString()}")
                gameStarted = true
                // Start the first round
                newRound()
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
            val username = auth.currentUser!!.displayName!!

            // Current Round
            val isDescriptor = game.currentRound.currentDescriptor == username
            _playerIsCurrentDescriptor.value = isDescriptor
            _playersTeamIsPlaying.value =
                game.teams[game.currentRound.currentTeam].hasMember(username)

            _playersTeamIsPlaying.value = !game.currentRound.roundOver

            // Words
            val words = arrayListOf<GameCard>()
            if (isDescriptor) {
                for (word in game.currentRound.displayedWords.values) {
                    words.add(word)
                }
            }
            _wordsLiveData.value = words

            // Time
            _timeLiveData.value = game.currentRound.timeRemaining

            // Messages
            // TODO: Move messages out of here so that the RecyclerView stops scrolling when time changes
            val messages = arrayListOf<Message>()
            for (message in game.messages.values) {
                messages.add(message)
            }
            _messagesLiveData.value = Result.Success(messages.sortedBy { it.timestamp })
        }

        override fun onCancelled(error: DatabaseError) {
            error.toException().printStackTrace()

            // Words
            _wordsLiveData.value = listOf()

            // Time
            _timeLiveData.value = 0

            // Messages
            _messagesLiveData.value = Result.Error(error.toException())
        }
    }

    init {
        _wordsLiveData.value = listOf()
        _timeLiveData.value = 0
        _messagesLiveData.value = Result.InProgress
        _playersTeamIsPlaying.value = false
        _playerIsCurrentDescriptor.value = false
    }

    fun newGame(): Task<Void> {
        //Creates a new game
        game = Game()
        game.messages[messageReference.push().key!!] = Message(
            senderNickname = "gamebot",
            message = "You've created a new game. Invite your friends" +
                    " using the code " + key, // TODO: Use string resource instead
            type = MessageType.GAMEBOT,
            timestamp = System.currentTimeMillis()
        )
        val username = auth.currentUser!!.displayName!!
        game.addMember(username)
        game.currentRound.currentDescriptor = username
        // Wait for join requests and add them to the members list
        joinRequestsRef.addChildEventListener(joinEventListener)
        gamesReference.child(key).addValueEventListener(gameListener)
        return gamesReference.child(key).setValue(game)
    }

    fun joinGame(gameKey: String) {
        _key = gameKey
        joinRequestsRef.child(gameKey).setValue(true)
        _playerIsCurrentDescriptor.value = false
    }

    fun endGame(): Task<Void> {
        // Stop waiting for members to join
        joinRequestsRef.removeEventListener(joinEventListener)
        gamesReference.child(key).removeEventListener(gameListener)
        return gamesReference.child(key).child("gameOver").setValue(true)
    }

    fun newRound(): Task<Void> {
        // Change teams
        currentTeam = if (currentTeam == -1 || currentTeam == 1) { 0 } else { 1 }

        // Get a new descriptor
        val descriptor = game.nextDescriptor(currentTeam)
        sendBotMessage("It's ${game.teams[currentTeam]}'s turn. " +
                "$descriptor will be describing the words. " +
                "Time started")

        return roundReference.updateChildren(
            hashMapOf<String, Any>(
                "currentDescriptor" to descriptor,
                "currentTeam" to currentTeam,
                "roundOver" to false,
                // TODO: Load words from database, instead of using these
                "displayedWords" to hashMapOf(
                    "kendrick" to GameCard("Kendrick Lamar", true),
                    "riri" to GameCard("Rihanna", true),
                    "pc" to GameCard("PC", true),
                    "car" to GameCard("Car", true),
                    "harare" to GameCard("Harare", true)
                )
            )
        )
    }

    fun endRound() {
        // Remove the displayed words
        roundReference.child("displayedWords").setValue(null)
        roundReference.child("roundOver").setValue(true)
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

    fun setTime(secondsRemaining: Int) {
        roundReference.child("timeRemaining").setValue(secondsRemaining)
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