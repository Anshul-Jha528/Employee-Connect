//package com.anshul.employeeconnect
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.graphics.Bitmap
//import android.graphics.Canvas
//import android.graphics.Color
//import android.graphics.Paint
//import android.os.Build
//import android.os.Bundle
//import android.util.Log
//import android.widget.Toast
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import com.anshul.employeeconnect.databinding.ActivityMeetingsBinding
//import com.google.firebase.Firebase
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.digest.Crypt
//import com.google.firebase.messaging.FirebaseMessaging
//import com.google.firebase.messaging.RemoteMessage
//import io.agora.rtc2.Constants
//import io.agora.rtc2.IRtcEngineEventHandler
//import io.agora.rtc2.RtcEngine
//import io.agora.rtc2.RtcEngineConfig
//import io.agora.rtc2.ChannelMediaOptions
//
//class ActivityMeetings : AppCompatActivity() {
//
//    lateinit var binding: ActivityMeetingsBinding
//
//    var teamName : String = ""
//    var teamCode : String = ""
//    var admin : String = ""
//    var myName : String = ""
//    var token : String = ""
//    var isAdmin = false
//    var isMicOn = true
//    var isAudOn = true
//
//    private val myAppId = "15dc6de6b1e947c5b70042af07144e0a"
//    var mRtcEngine : RtcEngine? = null
//    var channelName = ""
//    val callToken = "007eJxTYFi6y0DudseG6mees5ZXFkU1K855UbPDSzrVvjlW545Y6WcFBkPTlGSzlFSzJMNUSxPzZNMkcwMDE6PENANzQxOTVIPEiTO8MhoCGRnqHctZGBkgEMTnYXDLLCoucc5IzMtLzWFgAABkIiI5"
//
//    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
//        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
//            super.onJoinChannelSuccess(channel, uid, elapsed)
//            runOnUiThread {
//
//            }
//        }
//        override fun onUserJoined(uid: Int, elapsed: Int) {
//            runOnUiThread {
//                Toast.makeText(applicationContext, "New user joined", Toast.LENGTH_SHORT).show()
//            }
//        }
//        override fun onUserOffline(uid: Int, reason: Int) {
//            super.onUserOffline(uid, reason)
//            runOnUiThread {
//
//            }
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        binding = ActivityMeetingsBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(binding.root.id)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        val sharedPreferences = getSharedPreferences("my shared preferences", MODE_PRIVATE)
//        myName = sharedPreferences.getString("userName", "")!!
//
//        teamName = intent.getStringExtra("teamName")!!
//        teamCode = intent.getStringExtra("teamCode")!!
//        isAdmin = intent.getBooleanExtra("isAdmin", false)
//        channelName = teamCode
//
//        initializeAgora()
//        checkPermissions()
//
//        binding.leave.setOnClickListener {
//            mRtcEngine?.leaveChannel()
//            finish()
//        }
//        binding.mic.setOnClickListener {
//            if(isMicOn){
//                mRtcEngine?.muteLocalAudioStream(true)
//                binding.mic.backgroundTintList = ContextCompat.getColorStateList(this, R.color.gray)
//                isMicOn = false
//            }else{
//                mRtcEngine?.muteLocalAudioStream(false)
//                binding.mic.backgroundTintList = ContextCompat.getColorStateList(this, R.color.whiteVariant)
//                isMicOn = true
//            }
//        }
//        binding.aud.setOnClickListener {
//            if(isAudOn){
//                mRtcEngine?.setEnableSpeakerphone(false)
//                binding.aud.backgroundTintList = ContextCompat.getColorStateList(this, R.color.gray)
//                isAudOn = false
//            }else{
//                mRtcEngine?.setEnableSpeakerphone(true)
//                binding.aud.backgroundTintList = ContextCompat.getColorStateList(this, R.color.whiteVariant)
//                isAudOn = true
//            }
//        }
//
//    }
//
//    fun initializeAgora(){
//        try {
//            val config = RtcEngineConfig().apply {
//                mContext = baseContext
//                mAppId = myAppId
//                mEventHandler = mRtcEventHandler
//
//
//            }
//            mRtcEngine = RtcEngine.create(config)
//            mRtcEngine?.setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY, Constants.AUDIO_SCENARIO_GAME_STREAMING)
//        } catch (e: Exception) {
//            throw RuntimeException("Error initializing RTC engine: ${e.message}")
//            finish()
//        }
//    }
//
//    fun joinChannel(){
//        val options = ChannelMediaOptions().apply {
//            channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
//            clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
//        }
//
//        mRtcEngine?.joinChannel(callToken, channelName,"",0)
//        mRtcEngine?.disableVideo()
//        mRtcEngine?.enableAudio()
//        mRtcEngine?.setEnableSpeakerphone(true)
//
//
//    }
//
//    fun generateInitialsAvatar(name: String): Bitmap {
//        val initials = name.split(" ").map { it.first() }.joinToString("")
//        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(bitmap)
//        val paint = Paint().apply {
//            color = Color.BLUE
//            textSize = 40f
//            textAlign = Paint.Align.CENTER
//        }
//        canvas.drawText(initials, 50f, 50f, paint)
//        return bitmap
//    }
//
//
//    fun getToken(){
//
//        FirebaseMessaging.getInstance().token.addOnCompleteListener {task ->
//            if(!task.isSuccessful){
//                Log.d("FirebaseMessaging", "Fetching FCM registration token failed", task.exception)
//                return@addOnCompleteListener
//            }else{
//                token = task.result
//                Log.d("FirebaseMessaging", "Fetching FCM registration token success")
//                notifyCall()
//            }
//        }
//    }
//
//    fun notifyCall(){
//        val notification = RemoteMessage.Builder(token)
//            .addData("title", "Call from $teamName")
//            .addData("body", "Join the call now")
//            .addData("teamCode", teamCode)
//            .addData("type", "call")
//            .addData("admin", admin)
//            .build()
//        FirebaseMessaging.getInstance().send(notification)
//    }
//
//    private fun checkPermissions() {
//        val permissions = mutableListOf(
//            Manifest.permission.CAMERA,
//            Manifest.permission.RECORD_AUDIO
//        )
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            permissions.add(android.Manifest.permission.BLUETOOTH_CONNECT)
//        }
//
//        if (permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
//            joinChannel()
//        } else {
//            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 100)
//        }
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == 100) {
//            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
//                joinChannel()
//            } else {
//                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
//                finish()
//            }
//        }
//    }
//
//
//}