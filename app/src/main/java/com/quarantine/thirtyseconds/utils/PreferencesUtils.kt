package com.quarantine.thirtyseconds.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferencesUtils(context: Context?) {

    companion object {
        const val SHARED_PREFERENCES_NAME = "thirty_seconds_prefs"
        const val KEY_IS_FIRST_TIME = "is_first_time"
    }

    private val sharedPreferences: SharedPreferences =
        context!!.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)

    fun isUserFirstTime(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_FIRST_TIME, true)
    }

    fun makeUserFirstTime() {
        if (isUserFirstTime()) {
            sharedPreferences.edit(commit = true) {
                putBoolean(KEY_IS_FIRST_TIME, false)
            }
        }
    }
}