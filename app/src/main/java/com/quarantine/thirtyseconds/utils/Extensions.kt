package com.quarantine.thirtyseconds.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.google.firebase.database.ktx.getValue
import java.lang.Exception

inline fun <reified T : Any> DataSnapshotLiveData.toResult(): LiveData<Result<T>> {
    return Transformations.map(this) { result ->
        when(result) {
            is Result.Success -> {
                try {
                    Result.Success(result.data.getValue<T>()!!)
                } catch (e: Exception) {
                    Result.Error(e)
                }
            }
            is Result.Error -> Result.Error(result.exception)
            is Result.InProgress -> Result.InProgress
        }
    }
}