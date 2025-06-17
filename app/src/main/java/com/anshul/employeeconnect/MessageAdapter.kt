package com.anshul.employeeconnect

import android.R
import android.R.attr.right
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.util.LayoutDirection
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColorStateList
import androidx.recyclerview.widget.RecyclerView
import com.anshul.employeeconnect.databinding.MessageCardViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.graph.Graph
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.io.Resources
import com.google.firebase.database.collection.LLRBNode

class MessageAdapter(
    val context: Context,
    var messages : ArrayList<Messages>
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MessageViewHolder {
        val binding = MessageCardViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: MessageViewHolder,
        position: Int
    ) {
        messages.sortBy { it.time }

        holder.adapterBinding.message.text = messages[position].message
        holder.adapterBinding.messageTime.text = toNormalTime(messages[position].time)

        if(messages[position].senderId == FirebaseAuth.getInstance().currentUser?.uid.toString()){
            holder.adapterBinding.senderName.text = "You"
            holder.adapterBinding.messageLayout.backgroundTintList = ColorStateList.valueOf((0xFFD3D3D3).toInt())

        }else{
            holder.adapterBinding.senderName.text = messages[position].senderName
        }

    }

    override fun getItemCount(): Int {
        return messages.size
    }

    inner class MessageViewHolder(val adapterBinding : MessageCardViewBinding) : RecyclerView.ViewHolder(adapterBinding.root) {

    }

    fun toNormalTime(time: Long?): String {
        if (time == null) return "--"

        val calendar = Calendar.getInstance().apply {
            timeInMillis = time
        }

        val dateFormat = SimpleDateFormat("MMM dd")
        val formattedDate = dateFormat.format(calendar.time)


        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)

        val (hour, ampm) = when {
            hourOfDay == 0 -> Pair(12, "AM")  // Midnight (0:00) → 12 AM
            hourOfDay < 12 -> Pair(hourOfDay, "AM")  // 1-11 AM
            hourOfDay == 12 -> Pair(12, "PM")  // Noon (12:00) → 12 PM
            else -> Pair(hourOfDay - 12, "PM")  // 13-23 → 1-11 PM
        }

        return String.format("%s ; %d:%02d %s", formattedDate, hour, minutes, ampm)
    }



}