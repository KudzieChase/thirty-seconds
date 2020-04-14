package com.quarantine.thirtyseconds.ui.gameplay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.quarantine.thirtyseconds.repositories.GamePlayRepository

class GamePlayViewModelFactory : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(GamePlayRepository::class.java)
            .newInstance(
                GamePlayRepository.getInstance(
                    FirebaseAuth.getInstance(),
                    Firebase.database
                )
            )
    }
}