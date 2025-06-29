package com.anshul.employeeconnect

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.audiofx.BassBoost.Settings
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager.widget.ViewPager
import com.anshul.employeeconnect.databinding.ActivityMainBinding
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.jar.Manifest


class MainActivity : AppCompatActivity() {

    lateinit var mainBinding: ActivityMainBinding
    lateinit var sharedPreferences: SharedPreferences
    val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(mainBinding.root.id)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPreferences = this.getSharedPreferences("my shared preferences", MODE_PRIVATE)
        val email = sharedPreferences.getString("userEmail", "")
        val name = sharedPreferences.getString("userName", "")

        val intent = intent
        val emailLink = intent.data.toString()

        if (auth.isSignInWithEmailLink(emailLink)){
            auth.signInWithEmailLink(email!!, emailLink)
                .addOnCompleteListener{task ->
                    if(task.isSuccessful){
                        Toast.makeText(applicationContext, "Signed in successfully !", Toast.LENGTH_SHORT).show()
                        val result = task.result
                    }else{
                        Toast.makeText(applicationContext, task.exception?.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
        }

        checkNew()

        createNotificationChannel()

        val pagerAdapter = MainPagerAdapter(supportFragmentManager)
        mainBinding.viewPagerMain.adapter = pagerAdapter

        mainBinding.btmNavBarMain.setOnItemSelectedListener {
            when(it.itemId){
                R.id.navHome -> mainBinding.viewPagerMain.currentItem = 0
                R.id.navTask -> mainBinding.viewPagerMain.currentItem = 1
                R.id.navProfile -> mainBinding.viewPagerMain.currentItem = 2
                else -> mainBinding.viewPagerMain.currentItem = 0
            }
            return@setOnItemSelectedListener true
        }

        mainBinding.viewPagerMain.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {}
            override fun onPageSelected(position: Int) {
                mainBinding.btmNavBarMain.selectedItemId = when (position) {
                    0 -> R.id.navHome
                    1 -> R.id.navTask
                    2 -> R.id.navProfile
                    else -> R.id.navHome

                }
            }

            override fun onPageScrollStateChanged(state: Int) {}

        })

        FirebaseMessaging.getInstance().subscribeToTopic("team_chat")


        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(
                this@MainActivity,
                OnInitializationCompleteListener { initializationStatus: InitializationStatus? -> })
        }

        initialiseAds()
        getNotificationPermissions()

    }

    fun initialiseAds(){
        MobileAds.initialize(this){}
    }

    fun getNotificationPermissions(){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // Launch special settings activity for exact alarm permission
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }

    }




    fun signOut(){
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        if (mainBinding.viewPagerMain.currentItem == 0) {
            super.onBackPressed()
        } else {
            mainBinding.viewPagerMain.currentItem = 0
            mainBinding.btmNavBarMain.selectedItemId = R.id.navHome
        }
    }

    fun checkNew(){
        val database = Firebase.database
        val myRef = database.getReference("users")
        myRef.get().addOnSuccessListener { snapshot ->
            if(!snapshot.child(auth.currentUser?.uid?.toString()!!).exists()){
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle("Welcome")
                    .setMessage("Please complete your profile to continue")
                    .setPositiveButton("Complete") { interfaced, l ->
                        val intent = Intent(this, UpdateProfile::class.java)
                        startActivity(intent)
                    }
                    .setNegativeButton("Later") { interfaced, l ->
                        finish()
                    }
                    .create()
                    .show()
            }
        }

    }


     fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "My Notification Channel"
            val descriptionText = "Channel for scheduled notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "my_channel_id"
        const val NOTIFICATION_ID = 1
        const val REQUEST_CODE = 100
    }

}