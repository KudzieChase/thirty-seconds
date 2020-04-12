package com.quarantine.thirtyseconds.ui.gameplay

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quarantine.thirtyseconds.databinding.ItemCardBinding
import com.quarantine.thirtyseconds.models.GameCard
import com.quarantine.thirtyseconds.models.GameCardDiff

class GameCardAdapter : ListAdapter<GameCard, GameCardAdapter.EntryHolder>(GameCardDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryHolder {
        return EntryHolder(
            ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: EntryHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EntryHolder(private val binding: ItemCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: GameCard) {
            binding.run {
                this.entry = entry
            }
        }
    }

}