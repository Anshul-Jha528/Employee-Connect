package com.anshul.employeeconnect

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.anshul.employeeconnect.databinding.FragmentViewInfoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class FragmentViewInfo : DialogFragment() {

    lateinit var viewInfoBinding: FragmentViewInfoBinding

    var uid : String = ""
    val database : FirebaseDatabase = Firebase.database
    val myRef = database.getReference("users")
    val auth = FirebaseAuth.getInstance()
    var name : String = ""
    var userNumber : String = ""
    

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewInfoBinding = FragmentViewInfoBinding.inflate(
            inflater,
            container,
            false
        )
        return viewInfoBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uid = arguments?.getString("uid").toString()
        val isAdmin = arguments?.getBoolean("isAdmin")
        val teamId = arguments?.getString("teamId")
        viewInfoBinding.btnShowTasks.isVisible = isAdmin!!
        
        if(uid == auth.currentUser?.uid.toString()){
            setMyInfo()
        }else{
            setOtherInfo()
        }

        viewInfoBinding.btnOk.setOnClickListener {
            dismiss()
        }

        viewInfoBinding.btnShowTasks.setOnClickListener {
            val intent = Intent(requireActivity(), ActivityTeamMemberTasks::class.java)
            intent.putExtra("uid", uid)
            intent.putExtra("name", name)
            intent.putExtra("teamId", teamId)
            startActivity(intent)
            dismiss()
        }

        viewInfoBinding.call.setOnClickListener {
            startCall(userNumber)
        }

    }

    fun startCall(userNumber : String){

        if(ContextCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.CALL_PHONE )
            != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(requireActivity(),  arrayOf(android.Manifest.permission.CALL_PHONE), 2)

        }else {

            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:$userNumber") //case sensitive tel
            startActivity(intent)
        }

    }
    override fun onRequestPermissionsResult(requestCode : Int, permissions : Array<out String>, grantResults : IntArray){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)



        if(requestCode==2 && grantResults.size>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:$userNumber") //case sensitive tel
            startActivity(intent)
        }
    }



    fun setMyInfo(){
        viewInfoBinding.call.isVisible = false
        viewInfoBinding.btnShowTasks.isVisible = false
        myRef.child(uid).get().addOnSuccessListener {
            name = it.child("name").value.toString()
            val contact = it.child("contact").value.toString()
            val email = it.child("email").value.toString()
            val age = it.child("age").value.toString()
            val gender = it.child("gender").value.toString()
            val job = it.child("job").value.toString()
            val empID = it.child("empID").value.toString()
            val jobDesc = it.child("jobDesc").value.toString()
            val address = it.child("address").value.toString()
            
            viewInfoBinding.textInfo.text = "Name : ${name} \nEmail ID : ${email} \nContact No. : ${contact} \nAge : ${age} \nGender : ${gender} \nJob Title : ${job} \n" +
                    "Employee ID : ${empID} \nJob Description : ${jobDesc} \nAddress : ${address}"
                        
        }
        
    }
    
    fun setOtherInfo(){
        myRef.child(uid).get().addOnSuccessListener {
            name = it.child("name").value.toString()
            val gender = it.child("gender").value.toString()
            val job = it.child("job").value.toString()
            val jobDesc = it.child("jobDesc").value.toString()
            val address = it.child("address").value.toString()
            userNumber = it.child("contact").value.toString()

            viewInfoBinding.textInfo.text =
                "Name : ${name} \nGender : ${gender} \nJob Title : ${job} \nJob Description : ${jobDesc} \nAddress : ${address}"
        }
    }

}