package com.anshul.employeeconnect

import android.Manifest
import android.app.Notification
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.anshul.employeeconnect.AddTaskActivity
import com.anshul.employeeconnect.databinding.ActivityMyTeamBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.database
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL


class MyTeamActivity : AppCompatActivity() {

    lateinit var myTeamBinding: ActivityMyTeamBinding
    val database = Firebase.database
    var myRef = database.getReference("teams")
    var teamId : String = ""
    var teamName : String = ""
    var teamDesc : String = ""
    var teamCode : String = ""
    val auth = Firebase.auth
    var myAdapter2 : TeamMemberAdapter? = null
    var admin : String = ""
    var contacts = mutableListOf<String>()
    var myName : String = ""
    var token : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        myTeamBinding = ActivityMyTeamBinding.inflate(layoutInflater)
        setContentView(myTeamBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(myTeamBinding.root.id)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.title = "My Team"
        teamId = intent.getStringExtra("teamId")!!
        teamCode = intent.getStringExtra("teamCode")!!
        myTeamBinding.requestTextView.isVisible = false
        setDetails()
        getTeamDetails()

        val sharedPreferences = getSharedPreferences("my shared preferences", MODE_PRIVATE)
        myName = sharedPreferences.getString("userName", "")!!

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT){
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
                if(myAdapter2!!.getUid(viewHolder.adapterPosition)== admin){
                    Toast.makeText(
                        applicationContext,
                        "Admin cannot leave their team.",
                        Toast.LENGTH_SHORT).show()
                    getTeamDetails()
                }else {
                    if (auth.currentUser?.uid.toString() == admin) {
                        removeMember(myAdapter2!!.getUid(viewHolder.adapterPosition))
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "You are not the admin to remove someone",
                            Toast.LENGTH_SHORT
                        ).show()
                        getTeamDetails()
                    }
                }
            }

        }).attachToRecyclerView(myTeamBinding.memberRecyclerView)

        if(auth.currentUser?.uid?.toString()!! != admin){
            myTeamBinding.btnGroupCall.isVisible = false
        }

        myTeamBinding.btnGroupCall.isVisible = true

        myTeamBinding.btnGroupCall.setOnClickListener {

//            Toast.makeText(applicationContext, "Initialising call...", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, TeamChatActivity::class.java)
            intent.putExtra("teamName", teamName)
            intent.putExtra("teamCode", teamCode)
            startActivity(intent)


        }


    }

    fun setDetails(){

        database.getReference("teams").child(teamId).get().addOnSuccessListener { snapshot ->
            val team = snapshot.getValue(Teams::class.java)
            teamName = team!!.name
            teamDesc = team.desc
            myTeamBinding.teamName.text = teamName
            myTeamBinding.teamDesc.text = teamDesc
            myTeamBinding.teamCode.text = teamCode
        }


    }

    fun getTeamDetails(){
        myRef.child(teamId).get().addOnSuccessListener { snapshot->

                if (snapshot.child("admin").value.toString() == (auth.currentUser?.uid.toString())) {

                    getRequests()

                } else {
                    myTeamBinding.requestRecyclerView.isVisible = false
                    myTeamBinding.requestTextView.isVisible = false
                }

                admin = snapshot.child("admin").value.toString()

                getMembers(snapshot)
            }

        
    }

    fun getRequests(){
        var requests = mutableListOf<String>()
        requests.clear()
        val myRef2 = database.getReference("teams").child(teamId)
        myRef2.child("requests").get().addOnCompleteListener { task ->

            if(task.isSuccessful) {
                val snapshot = task.result

                if (snapshot.exists()) {

                    snapshot.children.forEach { req ->
                        requests.add(req.value.toString())
                    }
                    val myAdapter = TeamRequestAdapter(requests as ArrayList, this)
                    myTeamBinding.requestRecyclerView.layoutManager =
                        androidx.recyclerview.widget.LinearLayoutManager(this)
                    myTeamBinding.requestRecyclerView.adapter = myAdapter
                    myAdapter.notifyDataSetChanged()
                    val num = myAdapter.itemCount
                    myTeamBinding.requestTextView.setText("Pending Requests (${num})")
                    myTeamBinding.requestRecyclerView.isVisible = true
                    myTeamBinding.requestTextView.isVisible = true
                }
                else {
                    myTeamBinding.requestRecyclerView.isVisible = false
                    myTeamBinding.requestTextView.isVisible = false
                }
            }else{
                Toast.makeText(this, "Could not get requests", Toast.LENGTH_SHORT).show()
                myTeamBinding.requestRecyclerView.isVisible = false
                myTeamBinding.requestTextView.isVisible = false
            }
        }

    }



    fun getMembers(snapshot: DataSnapshot){
        val members = ArrayList<String>()
        members.clear()
        contacts.clear()
        members.add(snapshot.child("admin").value.toString())
        for(i in snapshot.child("members").children){
            members.add(i.value.toString())
            val x = i.child("contact").value.toString()
            contacts.add(x)
        }
        myAdapter2 = TeamMemberAdapter(this,members,snapshot.child("admin").value.toString())
        myTeamBinding.memberRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        myTeamBinding.memberRecyclerView.adapter = myAdapter2
        myAdapter2!!.notifyDataSetChanged()
        myTeamBinding.memberRecyclerView.isVisible = true

        val num = myAdapter2!!.getItemCount()
        myTeamBinding.membersTextView.setText("Members (${num})")

        val count = myAdapter2!!.itemCount
        if(count == 1){
            myTeamBinding.btnGroupCall.isVisible = false
        }else{
            myTeamBinding.btnGroupCall.isVisible = true
        }

    }

    fun add(uid : String){
        val userRef = database.getReference("users")
        userRef.child(uid).get().addOnSuccessListener { snapshot ->

//            if(snapshot.child("team1").value == null) {
                userRef.child(uid).child("myTeams").child(teamId).setValue(teamId)
                myRef.child(teamId).child("members").child(uid).setValue(uid)
                myRef.child(teamId).child("requests").child(uid).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Added", Toast.LENGTH_SHORT).show()
                        getTeamDetails()
                    }


                myRef.child(teamId).get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        var mem = snapshot.child("noOfMembers").value.toString().toInt()
                        mem = mem + 1
                        myRef.child(teamId).child("noOfMembers").setValue(mem)
                    }
                    getTeamDetails()
                }
//            }else if(snapshot.child("team2").value == null){
//                userRef.child(uid).child("team2").setValue(teamId)
//                myRef.child(teamId).child("members").push().setValue(uid)
//                myRef.child(teamId).child("requests").child(uid).removeValue()
//                    .addOnSuccessListener {
//                        Toast.makeText(this, "Added", Toast.LENGTH_SHORT).show()
//                        getTeamDetails()
//                    }
//
//
//                myRef.child(teamId).get().addOnSuccessListener { snapshot ->
//                    if (snapshot.exists()) {
//                        var mem = snapshot.child("noOfMembers").value.toString().toInt()
//                        mem = mem + 1
//                        myRef.child(teamId).child("noOfMembers").setValue(mem)
//                    }
//                    getTeamDetails()
//                }
////            }else{
//                myRef.child(teamId).child("requests").child(uid).removeValue()
//                getTeamDetails()
//                Toast.makeText(applicationContext, "User has already joined 2 teams, cannot add.", Toast.LENGTH_SHORT).show()
////            }
        }

    }

    fun reject(uid : String){
        myRef.child(teamId).child("requests").child(uid).removeValue().addOnSuccessListener {
            Toast.makeText(this, "Rejected", Toast.LENGTH_SHORT).show()
            getTeamDetails()
        }
    }

    fun assignTask(uid : String){
        val intent = Intent(this, AddTaskActivity::class.java)
        intent.putExtra("teamId",teamId)
        intent.putExtra("uid", uid)
        startActivity(intent)
    }

    fun viewInfo(uid : String){
        val userInfo = FragmentViewInfo()
        val bundle = Bundle()
        bundle.putString("uid",uid)
        bundle.putBoolean("isAdmin" , auth.currentUser?.uid?.toString()==admin)
        bundle.putString("teamId", teamId)
        userInfo.arguments = bundle
        userInfo.isCancelable = false
        userInfo.show(supportFragmentManager,"viewInfo")
    }

    fun removeMember(uid : String){

        val alertDialog = AlertDialog.Builder(this@MyTeamActivity)
        alertDialog.setTitle("Remove Member")
            .setMessage("Are you sure you want to remove this member from your team.")
            .setPositiveButton("Remove", DialogInterface.OnClickListener { dialogInterface, l ->
                val userRef = database.getReference("users")
                userRef.child(uid).child("myTeams").child(teamId).removeValue()
                myRef.child(teamId).child("members").child(uid).removeValue().addOnSuccessListener {
                    Toast.makeText(this, "Removed", Toast.LENGTH_SHORT).show()
                    myRef.child(teamId).get().addOnSuccessListener { snapshot ->
                        if(snapshot.exists()){
                            var mem = snapshot.child("noOfMembers").value.toString().toInt()
                            mem = mem - 1
                            myRef.child(teamId).child("noOfMembers").setValue(mem)
                        }
                    }
                    getTeamDetails()
                }
                dialogInterface.cancel()
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialogInterface, l ->
                getTeamDetails()
                dialogInterface.cancel()
            })
            .create()
            .show()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.team_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){

            android.R.id.home ->{
                finish()
            }

            R.id.refresh ->{
                getTeamDetails()
            }

            R.id.delete ->{
                if(auth.currentUser?.uid?.toString()==admin){
                    deleteTeam()
                }else{
                    Toast.makeText(applicationContext, "Only admin can delete this team", Toast.LENGTH_SHORT).show()
                }
            }

            R.id.leave ->{
                if(auth.currentUser?.uid?.toString() == admin){
                    Toast.makeText(applicationContext, "Admin cannot leave their team. Prefer deleting the team otherwise.",
                        Toast.LENGTH_SHORT).show()
                }else{
                    val dialog = AlertDialog.Builder(this)
                    dialog.setTitle("Leave team")
                        .setMessage("Do you want to leave this team? You will have to request again to join back.")
                        .setPositiveButton ("Leave", DialogInterface.OnClickListener{dialogInterface, l ->
                            removeMember2(auth.currentUser?.uid?.toString()!!, teamId )
                            myRef.child(teamId).get().addOnSuccessListener { snapshot ->
                                if(snapshot.exists()){
                                    var mem = snapshot.child("noOfMembers").value.toString().toInt()
                                    mem = mem - 1
                                    myRef.child(teamId).child("noOfMembers").setValue(mem)
                                }
                            }
                            Toast.makeText(
                                applicationContext,
                                "Left the team",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        })
                        .setNegativeButton ("Cancel", DialogInterface.OnClickListener{dialogInterface, l ->
                            dialogInterface.cancel()
                        })
                        .create()
                        .show()
                }
            }
        }

        return true
    }

    fun deleteTeam(){
        val alertDialog = AlertDialog.Builder(this@MyTeamActivity)
        alertDialog.setTitle("Delete Team")
            .setMessage("Are you sure you want to delete this team? You cannot retrieve it again.")
            .setPositiveButton("Delete", DialogInterface.OnClickListener { dialogInterface, l ->
                myRef.child(teamId).get().addOnSuccessListener { snapshot ->
                    if(snapshot.exists()){
                        val admin = snapshot.child("admin").value
                        removeMember2(admin.toString(), teamId)
                        for(i in snapshot.child("members").children){
                            removeMember2(i.value.toString(), teamId)
                        }
                        myRef.child(teamId).removeValue().addOnSuccessListener {
                            Toast.makeText(
                                applicationContext,
                                "Team deleted successfully.",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }

                    }
                }
            })
            .setNegativeButton( "Cancel", { dialogInterface, l ->
                dialogInterface.cancel()
            })
            .create()
            .show()

    }

    fun removeMember2(id : String, teamId : String){
        val ref = database.getReference("users").child(id)
        ref.child("myTeams").child(teamId).removeValue()
        database.getReference("teams").child(teamId).child("members").child(id).removeValue()
    }


//    fun groupCall(){
//        if(contacts.size!=0){
//            val uri = Uri.parse("tel:" + contacts.joinToString(","))
//            val intent = Intent(Intent.ACTION_CALL, uri )
//            startActivity(intent)
//            Toast.makeText(applicationContext, "Calling...", Toast.LENGTH_SHORT).show()
//            myTeamBinding.btnGroupCall.isEnabled = true
//        }else{
//            myTeamBinding.btnGroupCall.isEnabled = true
//            Toast.makeText(applicationContext, "No contacts available", Toast.LENGTH_SHORT).show()
//        }
//
//    }



}