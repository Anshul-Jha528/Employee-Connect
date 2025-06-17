package com.anshul.employeeconnect

import android.R
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.anshul.employeeconnect.databinding.ActivityCreateTeamBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ActivityCreateTeam : AppCompatActivity() {

    lateinit var createTeamBinding: ActivityCreateTeamBinding
    val auth = FirebaseAuth.getInstance()
    val database = Firebase.database
    val myRef = database.getReference("teams")
    val myRef2 = database.getReference("users")
    val uid = auth.currentUser?.uid


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createTeamBinding = ActivityCreateTeamBinding.inflate(layoutInflater)
        setContentView(createTeamBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(createTeamBinding.root.id)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        createTeamBinding.creatingTeam.isVisible = false
        createTeamBinding.newTeamLayout.isVisible = false

        createTeamBinding.createBtn.setOnClickListener {
            if (check()==true) {
                createTeam()
            }
        }
        createTeamBinding.okBtn.setOnClickListener {
            finish()
        }

        createTeamBinding.adView.isVisible = true


    }
    fun check() : Boolean{
        if(!createTeamBinding.teamName.text.isEmpty()){
            if(createTeamBinding.teamDesc.text.isEmpty()){
                createTeamBinding.teamDesc.setText("--")
            }
            return true
        }else{
            Toast.makeText(applicationContext, "Team name is necessary", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    fun createTeam() {

        createTeamBinding.creatingTeam.isVisible = true
        createTeamBinding.createBtn.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            val teamName = createTeamBinding.teamName.text.toString()
            val teamDesc = createTeamBinding.teamDesc.text.toString()
            val code = getUniqueCode()

            val team = Teams(teamName, teamDesc, code, uid.toString(), 1, hashMapOf(), hashMapOf())

            myRef.child(code).setValue(team).addOnCompleteListener { task ->
                if(task.isSuccessful){

                    myRef2.child(uid.toString()).get().addOnSuccessListener { snapshot ->
                        if(snapshot.exists()){
                            if(snapshot.child("team1").value == null){
                                myRef2.child(uid.toString()).child("team1").setValue(code)
                            }else if(snapshot.child("team2").value == null){
                                myRef2.child(uid.toString()).child("team2").setValue(code)
                            }
                            teamCreated(code)
                        }
                    }

//                    if(myRef2.child(uid.toString()).child("team1").get() == null){
//                        myRef2.child(uid.toString()).child("team1").setValue(code)
//                    }else if(myRef2.child(uid.toString()).child("team2").get() == null){
//                        myRef2.child(uid.toString()).child("team2").setValue(code)
//                    }

                }else{
                    Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_SHORT).show()
                }

            }



        }

    }

    fun teamCreated(code : String){

        createTeamBinding.adView.isVisible = false
        createTeamBinding.creatingTeam.isVisible = false
        createTeamBinding.newTeamLayout.isVisible = true
        createTeamBinding.newTeamName.text = createTeamBinding.teamName.text
        createTeamBinding.newTeamDesc.text = createTeamBinding.teamDesc.text
        createTeamBinding.newTeamCode.text = code

    }

    suspend fun getUniqueCode(): String {
        while (true) {
            val code = generateRandomCode()
            try {
                val snapshot = myRef.child(code).get().await()
                if (!snapshot.exists()) {
                    return code
                }
            } catch (e: Exception) {
                // Handle error or retry
                throw e // or generate a new code
            }
        }
    }

    fun generateRandomCode(): String {
        val chars = ('a'..'z') + ('0'..'9')
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }



}