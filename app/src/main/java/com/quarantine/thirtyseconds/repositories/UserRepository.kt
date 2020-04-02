package com.quarantine.thirtyseconds.repositories

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.quarantine.thirtyseconds.utils.DataSnapshotLiveData

class UserRepository(
    private val auth: FirebaseAuth,
    database: FirebaseDatabase,
    storage: FirebaseStorage
) {
    private val storageRef = storage.getReference("profile_photos")
    private val nicknamesReference = database.getReference("nicknames")

    fun savePhotoToAuth(photoUri: Uri): Task<Void> {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setPhotoUri(photoUri)
            .build()
        return auth.currentUser!!.updateProfile(profileUpdates)
    }

    fun saveNicknameToAuth(nickname: String): Task<Void> {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(nickname)
            .build()
        return auth.currentUser!!.updateProfile(profileUpdates)
    }

    fun saveNicknameToDatabase(nickname: String): Task<Void> {
        return nicknamesReference.child(nickname).setValue(true)
    }

    fun getUser(): LiveData<FirebaseUser?> {
        return MutableLiveData<FirebaseUser?>().apply { value = auth.currentUser }
    }

    fun isNickNameAvailable(nickName: String): DataSnapshotLiveData {
        val query = nicknamesReference.child(nickName)
        return DataSnapshotLiveData(query)
    }

    fun uploadPhoto(uri: Uri): UploadTask = storageRef.child(auth.currentUser!!.uid).putFile(uri)

    fun getDownloadUrl() = storageRef.child(auth.currentUser!!.uid).downloadUrl

    companion object {
        @Volatile
        private var instance: UserRepository? = null

        fun getInstance(
            auth: FirebaseAuth,
            database: FirebaseDatabase,
            storage: FirebaseStorage
        ): UserRepository? {
            return instance ?: synchronized(UserRepository::class.java) {
                if (instance == null) {
                    instance = UserRepository(auth, database, storage)
                }
                return instance
            }
        }
    }

}