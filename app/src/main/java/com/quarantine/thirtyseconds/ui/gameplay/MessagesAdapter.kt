package com.quarantine.thirtyseconds.ui.gameplay

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quarantine.thirtyseconds.databinding.ItemReceivedMessageBinding
import com.quarantine.thirtyseconds.databinding.ItemSentMessageBinding
import com.quarantine.thirtyseconds.models.Message
import com.quarantine.thirtyseconds.models.MessageType
import java.lang.IllegalArgumentException

class MessagesAdapter(private var messages: List<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_SENT = 1
        const val VIEW_TYPE_RECEIVED = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                SentMessagesHolder(
                    ItemSentMessageBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            VIEW_TYPE_RECEIVED -> {
                ReceivedMessagesHolder(
                    ItemReceivedMessageBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> throw IllegalArgumentException("Woah there chief! Invalid view type")
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder.itemViewType) {
            VIEW_TYPE_SENT -> {
                (holder as SentMessagesHolder).bind(message)
            }
            VIEW_TYPE_RECEIVED -> {
                (holder as ReceivedMessagesHolder).bind(message)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return when (message.type) {
            MessageType.DESCRIPTION -> VIEW_TYPE_SENT
            else -> VIEW_TYPE_RECEIVED
        }
    }

    fun updateMessages(updates: List<Message>) {
        //TODO evaluate if this is even necessary ?
        messages = updates
        notifyDataSetChanged()
    }


    inner class SentMessagesHolder(private val binding: ItemSentMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.run {
                this.message = message
            }
        }
    }

    inner class ReceivedMessagesHolder(private val binding: ItemReceivedMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.run {
                this.message = message
            }
        }
    }
}