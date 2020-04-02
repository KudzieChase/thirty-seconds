package com.quarantine.thirtyseconds.ui.intro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth

class IntroViewModelFactory : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(FirebaseAuth::class.java)
            .newInstance(FirebaseAuth.getInstance())
    }
}