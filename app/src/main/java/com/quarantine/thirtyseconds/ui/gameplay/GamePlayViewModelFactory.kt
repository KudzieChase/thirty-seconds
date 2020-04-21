package com.quarantine.thirtyseconds.ui.gameplay

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.quarantine.thirtyseconds.repositories.GamePlayRepository

class GamePlayViewModelFactory(
    private val app: Application,
    private val gameKey: String? = null
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
                Application::class.java,
                String::class.java,
                GamePlayRepository::class.java
            ).newInstance(
                app,
                gameKey,
                GamePlayRepository.getInstance(FirebaseAuth.getInstance(), Firebase.database)
            )
    }
}