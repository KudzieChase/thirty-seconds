package com.quarantine.thirtyseconds.ui.home

import android.view.View
import com.quarantine.thirtyseconds.models.Home

interface HomeItemClick {
    fun onClick(view: View, homeItem: Home)
}