package com.quarantine.thirtyseconds.ui.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.quarantine.thirtyseconds.utils.Result
import com.quarantine.thirtyseconds.repositories.UserRepository
import com.quarantine.thirtyseconds.utils.NicknameTakenException

class ProfileViewModel(
    private val repository: UserRepository
) : ViewModel() {

    private val _uploadedPhotoUrl = MutableLiveData<Result<String>>()
    val uploadSuccessful: LiveData<Result<String>>
        get() = _uploadedPhotoUrl

    private val _profileSaved = MutableLiveData<Result<Boolean>>()
    val profiledSaved: LiveData<Result<Boolean>>
        get() = _profileSaved

    val currentUser = repository.getUser()

    init {
        _profileSaved.value = Result.Success(false)
    }

    /**
     * Uploads the user's picture to Cloud Storage and saves the
     * download URL to Firebase Auth
     */
    fun uploadPhoto(photoUri: Uri) {
        _uploadedPhotoUrl.value = Result.InProgress
        repository.uploadPhoto(photoUri).continueWithTask { task ->
            if (!task.isSuccessful) {
                _uploadedPhotoUrl.value = Result.Error(task.exception!!)
            }
            repository.getDownloadUrl()
        }.addOnSuccessListener { uri ->
            // TODO: Test if this automatically updates
            // the photo without the need of _uploadedPhotoUrl

            // If it does, change the livedata to use a Boolean
            // just linke profileSaved
            repository.savePhotoToAuth(uri).addOnSuccessListener {

            }.addOnFailureListener {

            }
            // _uploadedPhotoUrl.value = Result.Success(uri.toString())
        }.addOnFailureListener {
            _uploadedPhotoUrl.value = Result.Error(it)
        }
    }

    /**
     * Checks if the new nickname is available
     * and saves it to Firebase Auth
     */
    fun saveProfile(newNickname: String) {
        currentUser.value?.let { user ->
            // Check if the user already has a nickname
            user.displayName?.let { oldNickname ->
                // Check if the nickname didn't change
                if (oldNickname == newNickname) {
                    _profileSaved.value = Result.Success(true)
                }
            }
            _profileSaved.value = Result.InProgress
            // TODO: Find a way to use the repository instead
            // repository.isNickNameAvailable(newNickname)
            // ----------------------------------------------
            // Welcome to callback hell :)
            // Check-in time is now, Check-out time is never
            Firebase.database.getReference("nicknames")
                .child(newNickname)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        _profileSaved.value = Result.Error(error.toException())
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val exists = dataSnapshot.getValue<Boolean>()!!
                        if (exists) {
                            _profileSaved.value = Result.Error(NicknameTakenException())
                        } else {
                            repository.saveNicknameToAuth(newNickname).addOnSuccessListener {
                                repository.saveNicknameToDatabase(newNickname)
                                    .addOnSuccessListener {
                                        _profileSaved.value = Result.Success(true)
                                    }.addOnFailureListener {
                                        _profileSaved.value = Result.Error(it)
                                    }
                            }.addOnFailureListener {
                                _profileSaved.value = Result.Error(it)
                            }
                        }
                    }
                })

        }

    }
}