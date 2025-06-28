package com.anshul.employeeconnect

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.anshul.employeeconnect.databinding.ActivityLoginBinding
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        supportActionBar?.title = "Login"

        supportFragmentManager.beginTransaction()
            .replace(binding.frameLogin.id, FragmentSignin())
            .commit()
    }

    override fun onStart() {
        super.onStart()
        initializeAds()
//        FirebaseApp.initializeApp(this)
        val mauth = FirebaseAuth.getInstance()
        if(mauth.currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }

    fun signInSuccess(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun initializeAds(){
        MobileAds.initialize(this) {}

    }

}