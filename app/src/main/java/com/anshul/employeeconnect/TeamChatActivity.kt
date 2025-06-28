package com.anshul.employeeconnect

import android.app.Notification
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.anshul.employeeconnect.databinding.ActivityTeamChatBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class TeamChatActivity : AppCompatActivity() {

    lateinit var binding : ActivityTeamChatBinding
    var message : String = ""
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    var senderName = ""
    var teamCode = ""
    var teamName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTeamChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(binding.root.id)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        teamName = intent.getStringExtra("teamName").toString()
        teamCode = intent.getStringExtra("teamCode").toString()
        supportActionBar?.title = teamName
        getName()
        reset()
        binding.messageRecycler.layoutManager = LinearLayoutManager(this)

        binding.btnSend.setOnClickListener {
            if(binding.messageInput.text!!.isEmpty()){
                Toast.makeText(applicationContext, "Message cannot be empty", Toast.LENGTH_SHORT).show()
            }else{
                sendMessage()
            }
        }

        retrieveMessages()


    }

    fun getName(){
        val database = Firebase.database
        val myref = database.getReference("users").child(uid)
        myref.get().addOnSuccessListener {
            senderName = it.child("name").value.toString()
        }
    }

    fun reset() {
        binding.btnSend.isEnabled = true
        binding.messageInput.text?.clear()
        message = binding.messageInput.text.toString()
    }

    fun sendMessage(){
        binding.btnSend.isEnabled = false
        message = binding.messageInput.text.toString()
        val database = Firebase.database
        val myRef = database.getReference("teams").child(teamCode).child("messages")

        val myMessage = Messages(uid, senderName, message , System.currentTimeMillis())

        CoroutineScope(Dispatchers.IO).launch {
            myRef.push().setValue(myMessage.toMap()).addOnSuccessListener {
                runOnUiThread {
                    reset()
                    Log.d("success", "Message sent")
                    notifyMessage()
                }
            }.addOnFailureListener {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Could not send message", Toast.LENGTH_SHORT)
                        .show()
                    Log.d("error", it.message.toString())
                }
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

    fun retrieveMessages(){
        val database = Firebase.database
        val myRef = database.getReference("teams").child(teamCode).child("messages")

        val messages = ArrayList<Messages>()
        val milli = System.currentTimeMillis().toLong()

        myRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                messages.clear()
                for(data in snapshot.children){
                    val diff = milli - data.getValue(Messages::class.java)!!.time.toLong()
                    if(diff > 86400000){
                        data.ref.removeValue()
                    }else {
                        messages.add(data.getValue(Messages::class.java)!!)
                    }
                }
                setAdapter(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Could not retrieve messages", Toast.LENGTH_SHORT).show()
                Log.d("error", error.message)
            }
        })

    }

    fun setAdapter(messages : ArrayList<Messages>){

        if(messages.size == 0){
            binding.emptyMessage.isVisible = true
        }else{
            binding.emptyMessage.isVisible = false
        }

        val adapter = MessageAdapter(this, messages)
        binding.messageRecycler.adapter = adapter
        binding.messageRecycler.scrollToPosition(messages.size-1)
        adapter.notifyDataSetChanged()
    }


    fun notifyMessage(){
        var token = ""
        FirebaseMessaging.getInstance().token.addOnCompleteListener {task ->
            if(!task.isSuccessful){
                Log.d("FirebaseMessaging", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }else{
                token = task.result
                Log.d("FirebaseMessaging", "Fetching FCM registration token success")
            }
        }
        val title = "New Message"
        val message = "You may have new messages. Tap to view"
        val notification = RemoteMessage.Builder("topic/team_chat")
            .addData("title", title)
            .addData("body", message)
            .addData("teamCode", teamCode)
            .addData("type", "chat")
            .build()

        FirebaseMessaging.getInstance().send(notification)
        Log.d("notification", "Notification sent")

    }

    override fun onBackPressed() {
        super.onBackPressed()
        loadInterstitialAd()
        finish()
    }


}