package com.quarantine.thirtyseconds.utils

import androidx.lifecycle.LiveData
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

/**
 * Helper class that updates LiveData value based on
 * Datasnapshot changes.
 */
class DataSnapshotLiveData(
    private val query: Query,
    private val updateInRealtime: Boolean = false
) : LiveData<Result<DataSnapshot>>() {

    private val listener = object : ValueEventListener {
        override fun onCancelled(error: DatabaseError) {
            value = Result.Error(error.toException())
        }

        override fun onDataChange(dataSnapshot: DataSnapshot) {
            value = Result.Success(dataSnapshot)
        }
    }

    override fun onActive() {
        super.onActive()
        value = Result.InProgress
        if (updateInRealtime) {
            query.addValueEventListener(listener)
        } else {
            query.addListenerForSingleValueEvent(listener)
        }
    }

    override fun onInactive() {
        super.onInactive()
        query.removeEventListener(listener)
    }
}