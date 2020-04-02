package com.quarantine.thirtyseconds.ui.intro

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class IntroViewModel(auth: FirebaseAuth) : ViewModel() {

    private val _isUserSignedIn = MutableLiveData<Boolean>()
    val isUserSignedIn: LiveData<Boolean>
        get() = _isUserSignedIn

    init {
        _isUserSignedIn.value = auth.currentUser != null
    }

    fun signInComplete() {
        _isUserSignedIn.value = true
    }

}