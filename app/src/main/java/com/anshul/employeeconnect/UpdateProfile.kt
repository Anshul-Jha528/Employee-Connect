package com.anshul.employeeconnect

import android.content.Intent
import android.icu.text.UnicodeSetSpanner
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.anshul.employeeconnect.databinding.ActivityUpdateProfileBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.database.ktx.database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UpdateProfile : AppCompatActivity() {

    lateinit var updateProfileBinding: ActivityUpdateProfileBinding
    val auth = FirebaseAuth.getInstance()
    val database : FirebaseDatabase = Firebase.database
    val myRef = database.getReference("users")

    var name: String = ""
    var contact: String = ""
    var age: Int = -1
    var email: String = ""
    var gender: String = ""
    var job: String = ""
    var empID: String = ""
    var jobDesc: String = ""
    var address: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        updateProfileBinding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(updateProfileBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(updateProfileBinding.root.id)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.title = "Complete Profile"

        Toast.makeText(applicationContext, "Loading...", Toast.LENGTH_LONG).show()

        initialiseAds()
        CoroutineScope(Dispatchers.IO).launch{
            createDb()
            getData()

        }

        updateProfileBinding.save.setOnClickListener {

            if(!checkRequiredFields()){
                Toast.makeText(applicationContext, "Please fill all required* fields", Toast.LENGTH_SHORT).show()
            }else{
                updateProfileBinding.save.isEnabled = false
                updateProfileBinding.save.text = "Saving..."
                updateProfileBinding.save.backgroundTintList = getColorStateList(android.R.color.darker_gray)
                CoroutineScope(Dispatchers.IO).launch {
                    save()
                }
            }

        }

    }

    fun checkRequiredFields(): Boolean {
        if(updateProfileBinding.inputName.text.isEmpty() ||
            updateProfileBinding.contact.text.isEmpty() ||
            updateProfileBinding.job.text.isEmpty() ||
            updateProfileBinding.address.text.isEmpty() ||
            (updateProfileBinding.gender.checkedRadioButtonId != updateProfileBinding.male.id &&
             updateProfileBinding.gender.checkedRadioButtonId != updateProfileBinding.female.id)) {

            return false

        }else{
            return true
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

    fun save(){

        if(!updateProfileBinding.inputName.text.isEmpty()){
            name = updateProfileBinding.inputName.text.toString()
        }

        if(!updateProfileBinding.contact.text.isEmpty()){
            contact = updateProfileBinding.contact.text.toString()
        }
        if(!updateProfileBinding.age.text.isEmpty()){
            age = updateProfileBinding.age.text.toString().toInt()
        }
        if(updateProfileBinding.gender.checkedRadioButtonId == updateProfileBinding.male.id){
            gender = "M"
        }else if(updateProfileBinding.gender.checkedRadioButtonId == updateProfileBinding.female.id){
            gender = "F"
        }
        if(!updateProfileBinding.job.text.isEmpty()){
            job = updateProfileBinding.job.text.toString()
        }
        if(!updateProfileBinding.empID.text.isEmpty()){
            empID = updateProfileBinding.empID.text.toString()
        }
        if(!updateProfileBinding.jobDesc.text.isEmpty()){
            jobDesc = updateProfileBinding.jobDesc.text.toString()
        }
        if(!updateProfileBinding.address.text.isEmpty()){
            address = updateProfileBinding.address.text.toString()
        }

        val sharedPreferences = getSharedPreferences("my shared preferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("userName", name)
        editor.apply()
        editor.commit()

        email = auth.currentUser?.email.toString()

        val user = Users(name, contact, email, age, gender, job, empID, jobDesc, address)

        myRef.child(auth.currentUser?.uid.toString()).updateChildren(user.toMap()).addOnCompleteListener { task ->
            if(task.isSuccessful){
                Toast.makeText(applicationContext, "Profile updated", Toast.LENGTH_SHORT).show()
                val sharedPreference = this.getSharedPreferences("my shared preferences", MODE_PRIVATE)
                val editor = sharedPreference.edit()
                editor.putString("userName", name)
                editor.apply()
                loadInterstitialAd()
                finish()
            }else{
                Toast.makeText(applicationContext, "Could not update profile", Toast.LENGTH_SHORT).show()
            }
        }


    }

    fun initialiseAds(){
        MobileAds.initialize(this){}
    }

    fun getData() {

        val sharedPreferences = getSharedPreferences("my shared preferences", MODE_PRIVATE)
        name = sharedPreferences.getString("userName", "").toString()
        if(name != "") updateProfileBinding.inputName.setText(name)

        if (myRef.child(auth.currentUser?.uid.toString()).get() != null) {
            myRef.child(auth.currentUser?.uid.toString()).get().addOnSuccessListener {
                name = it.child("name").value.toString()
                contact = it.child("contact").value.toString()
                if(it.child("age").value != null) {
                    age = it.child("age").value.toString().toInt()
                }
                email = it.child("email").value.toString()
                gender = it.child("gender").value.toString()
                job = it.child("job").value.toString()
                empID = it.child("empID").value.toString()
                jobDesc = it.child("jobDesc").value.toString()
                address = it.child("address").value.toString()

                CoroutineScope(Dispatchers.Main).launch {
                    updateUI()
                }
            }



        }
    }

    fun createDb(){
        if(myRef.child(auth.currentUser?.uid.toString()).get() == null){
            val newUser = Users("", "", "", -1, "", "", "", "", "")
            myRef.child(auth.currentUser?.uid.toString()).setValue(newUser)
        }
    }

    fun updateUI(){
        if(name != "null"){
            updateProfileBinding.inputName.setText(name)
        }
        if(contact != "null"){
            updateProfileBinding.contact.setText(contact)
        }
        if(age != -1){
            updateProfileBinding.age.setText(age.toString())
        }
        if(gender != "null"){
            if(gender == "M"){
                updateProfileBinding.male.isChecked = true
            }else if(gender == "F"){
                updateProfileBinding.female.isChecked = true
            }
        }
        if(job != "null"){
            updateProfileBinding.job.setText(job)
        }
        if(empID != "null"){
            updateProfileBinding.empID.setText(empID)
        }
        if(jobDesc != "null"){
            updateProfileBinding.jobDesc.setText(jobDesc)
        }
        if(address != "null") {
            updateProfileBinding.address.setText(address)
        }

        Toast.makeText(applicationContext, "Fields updated", Toast.LENGTH_SHORT).show()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        val database = com.google.firebase.ktx.Firebase.database
        val myRef = database.getReference("users")
        myRef.get().addOnSuccessListener { snapshot ->
            if(!snapshot.child(auth.currentUser?.uid?.toString()!!).exists()){
                Toast.makeText(applicationContext, "Please complete your profile.", Toast.LENGTH_SHORT).show()
            }else{
                finish()
            }
        }

    }

}