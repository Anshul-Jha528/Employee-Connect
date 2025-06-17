package com.anshul.employeeconnect

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.anshul.employeeconnect.databinding.ActivityTeamMemberTasksBinding

class ActivityTeamMemberTasks : AppCompatActivity() {

    lateinit var binding: ActivityTeamMemberTasksBinding
    var uid : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTeamMemberTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(binding.root.id)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.title = "Member Tasks"

        binding.name.text = intent.getStringExtra("name")
        uid = intent.getStringExtra("uid").toString()

        val fragmentManager= supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val fragmentTask = TaskFragment()
        val bundle = Bundle()
        bundle.putString("uid", uid)
        bundle.putString("name", intent.getStringExtra("name"))
        bundle.putString("teamId", intent.getStringExtra("teamId"))
        fragmentTask.arguments = bundle
        fragmentTransaction.add(binding.frameLayout.id, fragmentTask)
        fragmentTransaction
        fragmentTransaction.commit()


    }




}