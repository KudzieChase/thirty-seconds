package com.quarantine.thirtyseconds.models

import androidx.recyclerview.widget.DiffUtil
import com.quarantine.thirtyseconds.R

data class Home(
    val destinationName: String,
    val icon: Int,
    val destinationID: Int = 0
)

object HomeDiff : DiffUtil.ItemCallback<Home>() {
    override fun areItemsTheSame(oldItem: Home, newItem: Home): Boolean =
        oldItem.destinationName == newItem.destinationName

    override fun areContentsTheSame(oldItem: Home, newItem: Home): Boolean = oldItem == newItem
}

val homeItems = listOf(
    Home(
        "New Round",
        R.drawable.ic_new_selector,
        R.id.action_homeFragment_to_gamePlayFragment
    ),
    Home(
        "Profile",
        R.drawable.ic_account_circle_selector,
        R.id.action_homeFragment_to_profileFragment
    ),
    Home(
        "Tutorial",
        R.drawable.ic_help_outline_selector,
        R.id.action_homeFragment_to_tutorialFragment
    ),
    Home(
        "Invite Friends",
        R.drawable.ic_contact_mail_selector
    )
)