package com.quarantine.thirtyseconds.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quarantine.thirtyseconds.databinding.ItemHomeBinding
import com.quarantine.thirtyseconds.models.Home
import com.quarantine.thirtyseconds.models.HomeDiff


class HomeAdapter(
    private val onClick: HomeItemClick
) : ListAdapter<Home, HomeAdapter.ItemHolder>(HomeDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(
            ItemHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    inner class ItemHolder(private val binding: ItemHomeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(homeItem: Home, onClick: HomeItemClick) {
            binding.run {
                this.homeItem = homeItem
                clickHandler = onClick
            }
        }
    }
}