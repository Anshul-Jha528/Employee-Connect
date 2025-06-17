package com.anshul.employeeconnect

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anshul.employeeconnect.databinding.FragmentTaskBinding
import com.anshul.employeeconnect.databinding.TaskItemBinding
import android.content.Context
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database

class MyTasksAdapter(
    var context: Context ,
    var userTasks : ArrayList<UserTasks>,
    var fragment : TaskFragment
) : RecyclerView.Adapter<MyTasksAdapter.viewHolder>() {

//    var userTasks : ArrayList<UserTasks> = tasks.sortByDescending { it.taskStartTime } as ArrayList<UserTasks>


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): viewHolder {

        val binding = TaskItemBinding.inflate(LayoutInflater.from( parent.context), parent , false)
        return viewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: viewHolder,
        position: Int
    ) {
        userTasks.sortBy { it.taskStartTime }

        holder.adapterBinding.taskTitle.text = userTasks[position].taskTitle
        holder.adapterBinding.taskDesc.text = userTasks[position].taskDesc
        holder.adapterBinding.taskDate.text = userTasks[position].taskDate
        holder.adapterBinding.taskStartTime.text = "From :" + toNormalTime( userTasks[position].taskStartTime)
        holder.adapterBinding.taskEndTime.text = "To :" + toNormalTime( userTasks[position].taskEndTime)
        if(userTasks[position].status) {holder.adapterBinding.taskCardView.backgroundTintList = context.getColorStateList((com.anshul.employeeconnect.R.color.light_green))}
        else {holder.adapterBinding.taskCardView.backgroundTintList = context.getColorStateList((com.anshul.employeeconnect.R.color.blue))}



        if(userTasks[position].status == false && FirebaseAuth.getInstance().currentUser?.uid.toString()==fragment.uid) {
            fragment.scheduleNotification(fragment.requireContext(), userTasks[position].taskStartTime*1000, userTasks[position].taskId)
            val time = System.currentTimeMillis()

            if (userTasks[position].taskEndTime != null) {

                if (userTasks[position].taskEndTime!! * 1000 <= time) {
                    holder.adapterBinding.taskCardView.backgroundTintList =
                        context.getColorStateList(com.anshul.employeeconnect.R.color.red)
                }
            } else if (userTasks[position].taskStartTime * 1000 <= time) {
                holder.adapterBinding.taskCardView.backgroundTintList =
                    context.getColorStateList(com.anshul.employeeconnect.R.color.red)
            }
        }

        holder.adapterBinding.taskCardView.setOnClickListener {
            fragment.showInfo(userTasks[position].taskId)
        }


    }

    override fun getItemCount(): Int {
        return userTasks.size
    }

    fun getId(position: Int) : String{
        return userTasks[position].taskId
    }

    inner class viewHolder(val adapterBinding : TaskItemBinding) : RecyclerView.ViewHolder(adapterBinding.root){

    }

//    fun toNormalTime(time : Long?) : String?{
//
//        if(time == null) return "--"
//
//        var calendar = Calendar.getInstance()
//        calendar.timeInMillis = time
//
//        var hour = 12
//        var ampm = "AM"
//        if(calendar.time.hours >12) {
//            hour = calendar.time.hours - 12
//            ampm = "PM"
//        }
//            else if(calendar.time.hours == 0) 12
//            else calendar.time.hours
//        if(calendar.time.hours==12) ampm = "PM"
//
//        return hour.toString() + " : " + calendar.time.minutes + " " + ampm
//    }

    fun toNormalTime(time: Long?): String {
        if (time == null) return "--"

        val calendar = Calendar.getInstance().apply {
            timeInMillis = time*1000
        }

        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)

        val (hour, ampm) = when {
            hourOfDay == 0 -> Pair(12, "AM")  // Midnight (0:00) → 12 AM
            hourOfDay < 12 -> Pair(hourOfDay, "AM")  // 1-11 AM
            hourOfDay == 12 -> Pair(12, "PM")  // Noon (12:00) → 12 PM
            else -> Pair(hourOfDay - 12, "PM")  // 13-23 → 1-11 PM
        }

        return String.format("%d:%02d %s", hour, minutes, ampm)
    }

}