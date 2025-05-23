package com.anshul.employeeconnect

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.anshul.employeeconnect.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

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

//        getIntentSignIn()

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

        mainBinding.textView2.text = "Name : ${name} \nEmail : ${email} "

        mainBinding.signout.setOnClickListener{
            signOut()
        }


    }

    fun getIntentSignIn(){

        val intent = intent
        val emailLink = intent.data.toString()
        val email = sharedPreferences.getString("userEmail", "")

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
    }

    fun signOut(){
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

}