package com.anshul.employeeconnect

import android.content.DialogInterface
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.anshul.employeeconnect.databinding.ActivityAddTaskBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.tasks.Tasks
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat.CLOCK_12H
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.Month
import java.time.MonthDay
import java.time.Year
import javax.security.auth.callback.Callback

class AddTaskActivity : AppCompatActivity() {

    lateinit var activityAddTaskBinding: ActivityAddTaskBinding



    var uid : String = ""
    var name : String = ""
    var location : String = ""
    var desc : String = ""
    var date : String = ""
    var day = Calendar.DAY_OF_MONTH
    var month = Calendar.MONTH
    var year = Calendar.YEAR
    var startTime : Timestamp = Timestamp.now()
    var endTime : Timestamp? = null
    var admin = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        activityAddTaskBinding = ActivityAddTaskBinding.inflate(layoutInflater)

        setContentView(activityAddTaskBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(activityAddTaskBinding.root.id)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        uid = intent.getStringExtra("uid")!!
        admin = intent.getStringExtra("teamId")!!

        supportActionBar?.title = "Add Task"

        activityAddTaskBinding.selectStartTime.setOnClickListener {
            pickStartDate()
        }
        activityAddTaskBinding.selectEndTime.setOnClickListener {
            pickEndDate()
        }

        activityAddTaskBinding.clear.setOnClickListener {
            clear()
        }

        activityAddTaskBinding.save.setOnClickListener {
            if (checkInfo()) {
                checkNotOverlap { check ->
                    if(check) showAlertDialog()
                }
            }else{
                Toast.makeText(applicationContext, "Fill all the details", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private var interstitialAd: InterstitialAd? = null

    fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, "ca-app-pub-9376656451768331/5119343542", adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            })
    }

    fun getData() : Boolean{

        if(activityAddTaskBinding.taskTitle.text != null &&
            activityAddTaskBinding.taskLocation.text != null &&
            activityAddTaskBinding.taskDesc.text != null) {

            name = activityAddTaskBinding.taskTitle.text.toString()
            location = activityAddTaskBinding.taskLocation.text.toString()
            desc = activityAddTaskBinding.taskDesc.text.toString()
            return true
        }else{
            activityAddTaskBinding.taskTitle.error = "Required"
            activityAddTaskBinding.taskLocation.error = "Required"
            activityAddTaskBinding.taskDesc.error = "Required"
            return false
        }
    }

    fun pickStartDate(){
        val datePicker = MaterialDatePicker.Builder
            .datePicker()
            .setCalendarConstraints(
                CalendarConstraints.Builder()
                    .setStart(MaterialDatePicker.thisMonthInUtcMilliseconds())
                    .setEnd(MaterialDatePicker.thisMonthInUtcMilliseconds() +  7776000000)
                    .build()
            )
            .build()
        datePicker.show(supportFragmentManager, "DATE_PICKER")
        datePicker.addOnPositiveButtonClickListener {
            val selection = datePicker.selection
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selection!!

            val dateFormat = SimpleDateFormat("MMM dd, yyyy")
            val formattedDate = dateFormat.format(calendar.time)
            var date1 = formattedDate

            var day1 = calendar.get(Calendar.DAY_OF_MONTH)
            var month1 = calendar.get(Calendar.MONTH) + 1 // Months are 0-based
            var year1 = calendar.get(Calendar.YEAR)

            pickStartTime(day1, month1, year1, date1)

//            activityAddTaskBinding.selectDate.text = formattedDate
        }
    }

    fun pickEndDate(){
        val datePicker = MaterialDatePicker.Builder
            .datePicker()
            .setCalendarConstraints(
                CalendarConstraints.Builder()
                    .setStart(MaterialDatePicker.thisMonthInUtcMilliseconds())
                    .setEnd(MaterialDatePicker.thisMonthInUtcMilliseconds() + 7776000000)
                    .build()
            )
            .build()
        datePicker.show(supportFragmentManager, "DATE_PICKER")
        datePicker.addOnPositiveButtonClickListener {
            val selection = datePicker.selection
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selection!!


            var day1 = calendar.get(Calendar.DAY_OF_MONTH)
            var month1 = calendar.get(Calendar.MONTH) + 1 // Months are 0-based
            var year1 = calendar.get(Calendar.YEAR)

            pickEndTime(day1, month1, year1 )

//            activityAddTaskBinding.selectDate.text = formattedDate
        }
    }

    fun clear(){
        activityAddTaskBinding.taskTitle.text = null
        activityAddTaskBinding.taskLocation.text = null
        activityAddTaskBinding.taskDesc.text = null
        activityAddTaskBinding.selectStartTime.text = "-SELECT-"
        activityAddTaskBinding.selectEndTime.text = "-SELECT-"
        name  = ""
        location = ""
        desc = ""
        date = ""
        day = Calendar.DAY_OF_MONTH
        month = Calendar.MONTH
        year = Calendar.YEAR
        startTime = Timestamp.now()
        endTime  = null


    }

    fun checkInfo() : Boolean {
        if (getData() && activityAddTaskBinding.selectStartTime.text != "-SELECT-" ) {
            return true
        }
        return false
    }

    fun pickStartTime(day1 : Int, month1 : Int, year1 : Int , date1 : String) {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(CLOCK_12H)
            .build()
        timePicker.show(supportFragmentManager, "timePicker")
        timePicker.addOnPositiveButtonClickListener {

            day = day1
            month = month1
            year = year1
            date = date1

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            calendar.set(Calendar.MINUTE, timePicker.minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            calendar.set(Calendar.MONTH, month-1)
            calendar.set(Calendar.YEAR, year)
            startTime = com.google.firebase.Timestamp(calendar.time)

            val amPm = if (timePicker.hour < 12) "AM" else "PM"
            val displayHour = if (timePicker.hour == 0) 12 else if (timePicker.hour > 12) timePicker.hour - 12 else timePicker.hour
            activityAddTaskBinding.selectStartTime.text = "$displayHour:${timePicker.minute} $amPm"
        }
    }

    fun pickEndTime(day1: Int, month1: Int, year1: Int) {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(CLOCK_12H)
            .build()
        timePicker.show(supportFragmentManager, "timePicker")
        timePicker.addOnPositiveButtonClickListener {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            calendar.set(Calendar.MINUTE, timePicker.minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.set(Calendar.DAY_OF_MONTH, day1)
            calendar.set(Calendar.MONTH, month1-1)
            calendar.set(Calendar.YEAR, year1)
            endTime = com.google.firebase.Timestamp(calendar.time)

            val amPm = if (timePicker.hour < 12) "AM" else "PM"
            val displayHour = if (timePicker.hour == 0) 12 else if (timePicker.hour > 12) timePicker.hour - 12 else timePicker.hour
            activityAddTaskBinding.selectEndTime.text = "$displayHour:${timePicker.minute} $amPm"
        }
    }

    fun saveData(){
        val database = Firebase.database
        val myRef = database.getReference("users")
            .child(uid)
            .child("tasks")

        val taskId = myRef.push().key.toString()
        val userTask = UserTasks(taskId, name, desc, location, date, startTime.seconds.toLong(), endTime?.seconds?.toLong(), false, admin)
        val myId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        myRef.child(taskId).setValue(userTask)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Toast.makeText(applicationContext, "Task added", Toast.LENGTH_SHORT).show()
                    loadInterstitialAd()
                    finish()
                }else{
                    Toast.makeText(applicationContext, "ERROR : Task could  not be added", Toast.LENGTH_SHORT).show()
                    activityAddTaskBinding.save.isEnabled = true
                    activityAddTaskBinding.save.text = "Assign"
                    activityAddTaskBinding.save.backgroundTintList = getColorStateList(android.R.color.holo_green_dark)
                }
            }
    }

    fun showAlertDialog() {
        val alertDialog = AlertDialog.Builder(this@AddTaskActivity)
        alertDialog.setTitle("Confirm")
            .setMessage("Are you sure you want to assign this task? \nYou cannot change any details once saved. Ensure given time slot is free.")
            .setPositiveButton("Confirm", DialogInterface.OnClickListener { dialogInterface, l ->
                getData()
                activityAddTaskBinding.save.isEnabled = false
                activityAddTaskBinding.save.text = "Saving..."
                activityAddTaskBinding.save.backgroundTintList = getColorStateList(android.R.color.darker_gray)

                CoroutineScope(Dispatchers.IO).launch{
                    saveData()
                }
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialogInterface, l ->
                dialogInterface.cancel()
            })
            .create()
            .show()
    }

    fun firebaseTimestampToCalendar(timestamp: com.google.firebase.Timestamp): Calendar {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp.toDate().time
        return calendar
    }

//    fun checkNotOverlap(callback: (Boolean) -> Unit) {
//        // First validate time sequence
//        val timeS = firebaseTimestampToCalendar(startTime)
//        val timeE = endTime?.let { firebaseTimestampToCalendar(it) }
//
//        if (timeE != null && timeE.before(timeS)) {
//            Toast.makeText(
//                applicationContext,
//                "End time cannot be before start time",
//                Toast.LENGTH_SHORT
//            ).show()
//            callback(false)
//            return
//        }
//
//        val database = Firebase.database
//        val myRef = database.getReference("users")
//            .child(uid)
//            .child("tasks")
//
//        val myStart = startTime.seconds
//        val myEnd = endTime?.seconds
//
//        myRef.get().addOnSuccessListener { snapshot ->
//            var hasConflict = false
//
//            snapshot.children.forEach { eachTask ->
//                if (hasConflict) return@forEach // Skip if conflict already found
//
//                val startStr = eachTask.child("startTime").child("seconds").value?.toString()
//                val endStr = eachTask.child("endTime").child("seconds").value?.toString()
//
//                val existingStart = startStr?.toLongOrNull() ?: 0L
//                val existingEnd = endStr?.toLongOrNull()
//
//                // Check all possible conflict scenarios
//                hasConflict = when {
//                    // Both tasks have end times
//                    myEnd != null && existingEnd != null -> {
//                        (myStart < existingEnd && myEnd > existingStart)
//                    }
//                    // Current task has end time, existing doesn't
//                    myEnd != null && existingEnd == null -> {
//                        (myStart <= existingStart && myEnd >= existingStart)
//                    }
//                    // Current task has no end time, existing does
//                    myEnd == null && existingEnd != null -> {
//                        (myStart >= existingStart && myStart <= existingEnd)
//                    }
//                    // Neither has end time - compare just start times
//                    else -> {
//                        (myStart == existingStart)
//                    }
//                }
//
//                if (hasConflict) {
//                    Toast.makeText(
//                        applicationContext,
//                        "Time conflict with existing task",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//            }
//
//            callback(!hasConflict) // Final result after checking all tasks
//        }.addOnFailureListener {
//            Toast.makeText(
//                applicationContext,
//                "Failed to check availability",
//                Toast.LENGTH_SHORT
//            ).show()
//            callback(false)
//        }
//    }


    fun checkNotOverlap(callback : (Boolean) -> Unit) { //false -> overlap, true -> no overlap
        val timeS = firebaseTimestampToCalendar(startTime)
//        val timeE = if(endTime != null) firebaseTimestampToCalendar(endTime!!) else null

        val timeE = endTime?.let{
            firebaseTimestampToCalendar(it)
        }

        if(timeE != null) {
            if (timeE!!.before(timeS)) {
                Toast.makeText(
                    applicationContext,
                    "End time cannot be before start time",
                    Toast.LENGTH_SHORT
                ).show()
                callback(false)
                return
            }else{
                callback(true)
                return
            }
        }else{
            callback(true)
            return
        }


//        val database = Firebase.database
//        val myRef = database.getReference("users")
//            .child(uid)
//            .child("tasks")
//
//
//        val myStart = startTime.seconds
//        val myEnd = endTime?.let{ endTime?.seconds}
//
//        myRef.get().addOnSuccessListener { snapshot ->
//            var hasConflict = false
//
//            for (eachTask in snapshot.children) {
//                if(hasConflict) continue
//
//                val y = eachTask.child("startTime").child("seconds").value.toString()
//                val start = if(y=="null")  0L else y.toLong()
//                val x = eachTask.child("endTime").child("seconds").value.toString()
//                val end = if(x=="null")  null else x.toLong()
//
//                if(myStart == start) hasConflict=true
//
//                if(end != null) {
//                    if(myEnd != null){
//                        if((myStart in start..end) || (myEnd in start..end)) {
//                            hasConflict = true
//
//                        }
//                    }
//                    else{
//                        if(myStart in start..end){
//                            hasConflict = true
//
//                        }
//                    }
//                }else{
//                    if(myEnd != null){
//                        if(start in myStart..myEnd){
//                            hasConflict = true
//                        }
//                    }else{
//                        if(myStart == start){
//                            hasConflict = true
//
//                        }
//                    }
//                }
//
//                if(hasConflict){
//                    Toast.makeText(applicationContext,
//                        "Given schedule is already engaged. Try different timings.",
//                        Toast.LENGTH_SHORT).show()
//                }
//
//            }
//
//            callback(!hasConflict)

    }

}
