package com.quarantine.thirtyseconds.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.quarantine.thirtyseconds.models.*
import com.quarantine.thirtyseconds.utils.Result

class GamePlayRepository(
    val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) {
    private var game = Game()
    private var gameStarted = false
    private var currentTeam = -1
    private var words = ArrayList<GameCard>()
    private var messages = ArrayList<Message>()

    private val gamesReference = database.getReference("games")
    private var _key = "-M4siw7Rcv8vsXi4tLXg"
    val key get() = _key

    private val messageReference = gamesReference.child(key).child("messages")
    private val joinRequestsRef = gamesReference.child(key).child("joinRequests")
    private val roundReference = gamesReference.child(key).child("currentRound")

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
                newRound("Team A is playing. You are describing", auth.currentUser!!.uid!!)
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

            // Words
            words = ArrayList()
            if (isDescriptor) {
                for (word in game.currentRound.displayedWords.values) {
                    words.add(word)
                }
            }
            _wordsLiveData.value = words

            // Time
            _timeLiveData.value = game.currentRound.timeRemaining

            // Messages
            val newMessages = arrayListOf<Message>()
            for (message in game.messages.values) {
                newMessages.add(message)
            }
            if (messages != newMessages) {
                messages = newMessages
                _messagesLiveData.value = Result.Success(messages.sortedBy { it.timestamp })
            }
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

    fun newGame(botMessage: String): Task<Void> {
        //Creates a new game
        game = Game()
        game.messages[messageReference.push().key!!] = Message(
            senderNickname = "gamebot",
            message = botMessage,
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

    fun getNextDescriptor() = game.nextDescriptor(currentTeam)

    fun getCurrentTeamName() = game.teams[currentTeam].name

    fun newRound(botMessage: String, descriptor: String): Task<Void> {
        // Change teams
        currentTeam = if (currentTeam == -1 || currentTeam == 1) { 0 } else { 1 }

        sendBotMessage(botMessage)

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

    fun incrementScore() {
        val ref = gamesReference.child(key).child("teams")
            .child("$currentTeam").child("score")
        ref.runTransaction(object : Transaction.Handler {

            override fun doTransaction(data: MutableData): Transaction.Result {
                var currentScore = data.getValue(Int::class.java)
                    ?: return Transaction.success(data)
                data.value = ++currentScore
                game.teams[currentTeam].score = currentScore
                if (currentScore >= 34) {
                    // TODO: End the game
                }
                return Transaction.success(data)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                dataSnapshot: DataSnapshot?
            ) {

            }
        })
    }

    fun decrementScore() {
        val ref = gamesReference.child(key).child("teams")
            .child("$currentTeam").child("score")
        ref.runTransaction(object : Transaction.Handler {

            override fun doTransaction(data: MutableData): Transaction.Result {
                var currentScore = data.getValue(Int::class.java)
                    ?: return Transaction.success(data)
                // We cant have negative scores
                if (currentScore > 0) {
                    data.value = --currentScore
                    game.teams[currentTeam].score = currentScore
                }
                return Transaction.success(data)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                dataSnapshot: DataSnapshot?
            ) {

            }
        })
    }

    fun getTeamA() = game.teams[0]

    fun getTeamB() = game.teams[1]

    fun sendMessage(message: Message): Task<Void> {
        message.senderNickname = auth.currentUser!!.displayName!!
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

    fun descriptionIsValid(description: String): Boolean {
        var expressionParts: List<String>
        for (expression in words) {
            expressionParts = expression.entry.split(" ")
            for (word in expressionParts) {
                if (description.contains(word, true)) {
                    return false
                }
            }
        }
        return true
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