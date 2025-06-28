package com.anshul.employeeconnect

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.anshul.employeeconnect.databinding.FragmentTaskInformationBinding
import com.google.firebase.Firebase
import com.google.firebase.database.database
import java.text.DateFormat

class FragmentTaskInformation : DialogFragment() {

    lateinit var binding : FragmentTaskInformationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTaskInformationBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    var id = "-"
    var uid = "-"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        id = arguments?.getString("taskId")!!
        uid = arguments?.getString("uid")!!

        if(id != "-" && uid != "-"){
            setInfo()
        }

        binding.btnOk.setOnClickListener {
            if(binding.statusToggle.isChecked){
                Toast.makeText(requireContext(),
                    "Task will be automatically deleted.",
                    Toast.LENGTH_SHORT).show()
            }
            dismiss()
        }

    }

    fun setInfo(){
        val database = Firebase.database
        val myRef = database.getReference("users")
            .child(uid)
            .child("tasks")
            .child(id)
        myRef.get().addOnSuccessListener {task ->

            if(task.exists()) {

                val data = task.getValue(UserTasks::class.java)

                val title = data!!.taskTitle
                val desc = data!!.taskDesc
                val location = data!!.taskLocation
                val date = data!!.taskDate
                val startTime = toNormalTime(data!!.taskStartTime)
                val endTime = toNormalTime(data!!.taskEndTime)
                val status = data.status
                var admin = data.admin

                if(admin == uid){
                    admin = "You"
                }else{
                    database.getReference("teams").child(admin).get().addOnSuccessListener {
                        if(it.exists()){
                            admin = it.child("teamName").value.toString()
                        }
                    }
                }



                binding.textInfo.setText(
                    "Title : ${title}\n" +
                            "Date : ${date}\n" +
                            "Start Time : ${startTime}\n" +
                            "End Time : ${endTime}\n" +
                            "Location : ${location}\n" +
                            "Description : ${desc}\n" +
                            "Assigned by : ${admin}"
                )

                if (status == true) {
                    binding.statusToggle.isChecked = true
                } else {
                    binding.statusToggle.isChecked = false
                }
            }else{
                Toast.makeText(requireContext(), "Task not found", Toast.LENGTH_SHORT).show()
            }

        }

        binding.statusToggle.setOnCheckedChangeListener { l, m ->

            if(binding.statusToggle.isChecked){
                myRef.child("status").setValue(true)
            }else{
                myRef.child("status").setValue(false)
            }

        }

    }

    fun toNormalTime(time: Long?): String {
        if (time == null) return "--"

        val calendar = Calendar.getInstance().apply {
            timeInMillis = time*1000
        }

        val dateFormat = SimpleDateFormat("MMM dd, yyyy")
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