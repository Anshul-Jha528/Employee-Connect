package com.anshul.employeeconnect

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.anshul.employeeconnect.MainActivity.Companion.CHANNEL_ID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class HandleFirebaseNotification : FirebaseMessagingService()  {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("notification", "notification received")

        message.data?.let {
            val type = it["type"]
            val title = it["title"]
            val body = it["body"]
            val teamCode = it["teamCode"]

            createNotificationChannel()

//            createNotification(title!!, body!!, teamCode!!)




        }

    }

//    @SuppressLint("ServiceCast")
//    fun createNotification(title : String, body : String, teamCode : String){
//        val intent = Intent(this, ActivityMeetings::class.java)
//        intent.putExtra("teamCode", teamCode)
//
//        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
//
//        val notification = NotificationCompat.Builder(this, "1")
//            .setSmallIcon(R.drawable.baseline_call_24)
//            .setContentTitle(title)
//            .setContentText(body)
//            .addAction(R.drawable.pick_call, "Andwer", pendingIntent )
//            .addAction(R.drawable.decline_call, "Decline", null)
//            .setTimeoutAfter(60000L)
//            .build()
//
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.notify(1, notification)
//
//    }

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

}