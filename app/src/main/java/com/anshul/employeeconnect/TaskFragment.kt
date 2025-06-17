package com.anshul.employeeconnect

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anshul.employeeconnect.MainActivity.Companion.CHANNEL_ID
import com.anshul.employeeconnect.MainActivity.Companion.REQUEST_CODE
import com.anshul.employeeconnect.databinding.FragmentTaskBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.ktx.ChildEvent
import java.util.Calendar


class TaskFragment : Fragment() {

    lateinit var fragmentTaskBinding: FragmentTaskBinding
    var uid : String = ""
    var myAdapter : MyTasksAdapter? = null
    var teamId : String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentTaskBinding = FragmentTaskBinding.inflate(
            inflater,
            container,
            false
        )
        return fragmentTaskBinding.root
    }

    @SuppressLint("ServiceCast")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentTaskBinding.loadingTasks.isVisible = true
        fragmentTaskBinding.noTasks.isVisible = false

        val bundle = arguments
        if (bundle != null) {
            uid = bundle.getString("uid").toString()
            teamId = bundle.getString("teamId").toString()
        }else{
            uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
            teamId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        }
        if(uid != FirebaseAuth.getInstance().currentUser?.uid.toString()) {
            fragmentTaskBinding.taskFragmentLayout.setBackgroundResource(com.anshul.employeeconnect.R.color.white)
        }

        retrieveAndDisplayData()

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                TODO("Not yet implemented")
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
                removeTask(myAdapter!!.getId(viewHolder.adapterPosition))
            }

        }).attachToRecyclerView(fragmentTaskBinding.taskRecyclerView)




        fragmentTaskBinding.addTask.setOnClickListener {
            val intent = Intent(requireActivity(), AddTaskActivity::class.java)
            intent.putExtra("uid", uid)
            intent.putExtra("teamId", teamId)
            startActivity(intent)
        }

        fragmentTaskBinding.refresh.setOnClickListener {
            retrieveAndDisplayData()
        }


    }

    fun removeTask(taskId : String){
        if(uid == FirebaseAuth.getInstance().currentUser?.uid.toString()) {
            val dialog = AlertDialog.Builder(requireActivity())
            dialog.setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes", DialogInterface.OnClickListener {  interfaced , l->
                    Firebase.database.getReference("users").child(uid)
                        .child("tasks")
                        .child(taskId)
                        .removeValue()
                        .addOnSuccessListener {
                            cancelNotification(requireContext(), taskId)
                            Toast.makeText(requireContext(), "Task Deleted", Toast.LENGTH_SHORT).show()
                            retrieveAndDisplayData()
                        }
                })
                .setNegativeButton("No", DialogInterface.OnClickListener {  interfaced , l->
                    retrieveAndDisplayData()
                    interfaced.cancel()
                })
            dialog.setCancelable(false)
            dialog.create()
            dialog.show()
        }else{
            Toast.makeText(requireContext(), "You are not authorized to delete this task", Toast.LENGTH_SHORT).show()
            retrieveAndDisplayData()
        }
    }

    fun removeTaskAuto(taskId : String){
        Firebase.database.getReference("users").child(uid)
            .child("tasks")
            .child(taskId)
            .removeValue()
            .addOnSuccessListener {
                cancelNotification(requireContext(), taskId)
            }
    }


    fun showInfo(taskId : String?){
        val taskInfo = FragmentTaskInformation()
        val bundle = Bundle()
        bundle.putString("taskId", taskId)
        bundle.putString("uid", uid)
        taskInfo.arguments = bundle
        activity?.let{
            taskInfo.show(it.supportFragmentManager, "taskInfo")
        }

    }


    fun retrieveAndDisplayData(){
        fragmentTaskBinding.loadingTasks.isVisible = true
        fragmentTaskBinding.noTasks.isVisible = false

        val database = Firebase.database
        val myRef = database.getReference("users")
            .child(uid)
            .child("tasks")

        var myTasks = mutableListOf<UserTasks>()
        myTasks.clear()

        myRef.get().addOnSuccessListener { snapshot ->

            myTasks.clear()
            val currentDate = System.currentTimeMillis()

            snapshot.children.forEach { eachTask ->

                val task = eachTask.getValue(UserTasks::class.java)
                if (task != null) {
                    myTasks.add(task)

                    if(uid == FirebaseAuth.getInstance().currentUser?.uid.toString()) {

                        if (task.status == true) {
                            removeTaskAuto(task.taskId)
                        }
                        if (task.taskEndTime != null) {
                            if (task.taskEndTime*1000 + 3600000 < currentDate) {
                                removeTaskAuto(task.taskId)
                            }
                        } else {
                            if (task.taskStartTime*1000 +7200000 < currentDate) {
                                removeTaskAuto(task.taskId)
                            }
                        }
                        val taskTime = task.taskStartTime.toLong()*1000

                        Log.d("TIME", "${taskTime} : ${currentDate}")

                    }
                }
            }
                if (myTasks.isEmpty()) {
                    fragmentTaskBinding.loadingTasks.isVisible = false
                    fragmentTaskBinding.noTasks.isVisible = true
                } else {


                    fragmentTaskBinding.taskRecyclerView.layoutManager =
                        LinearLayoutManager(requireContext())
                    myAdapter =
                        MyTasksAdapter(requireContext(), myTasks as ArrayList<UserTasks>, this)
                    myAdapter!!.notifyDataSetChanged()
                    fragmentTaskBinding.taskRecyclerView.adapter = myAdapter
                    fragmentTaskBinding.loadingTasks.isVisible = false
                }
        }
    }

    override fun onResume() {
        super.onResume()
        retrieveAndDisplayData()
    }

    @SuppressLint("ServiceCast", "ScheduleExactAlarm")
    fun scheduleNotification(context: Context, triggerTime: Long, id : String) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("TASK_ID", id)
        val requestCode = id.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // For exact timing (even in Doze mode on API 23+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }

    }
    fun cancelNotification(context: Context, taskId: String) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val requestCode = taskId.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )

        pendingIntent?.let {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(it)
        }
    }

}

