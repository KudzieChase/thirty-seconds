package com.quarantine.thirtyseconds.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.quarantine.thirtyseconds.repositories.UserRepository

class ProfileViewModelFactory : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(UserRepository::class.java)
            .newInstance(UserRepository.getInstance(FirebaseAuth.getInstance(),
                Firebase.database, Firebase.storage))
    }
}
