package com.quarantine.thirtyseconds.models

import androidx.recyclerview.widget.DiffUtil

data class GameCard(
    val entry: String,
    val isActive: Boolean
)

object GameCardDiff : DiffUtil.ItemCallback<GameCard>() {
    override fun areItemsTheSame(oldItem: GameCard, newItem: GameCard): Boolean =
        oldItem.entry == newItem.entry

    override fun areContentsTheSame(oldItem: GameCard, newItem: GameCard): Boolean =
        oldItem == newItem
}