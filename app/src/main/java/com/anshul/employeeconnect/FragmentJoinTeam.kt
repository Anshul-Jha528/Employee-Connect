package com.anshul.employeeconnect

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.anshul.employeeconnect.databinding.FragmentJoinTeamBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FragmentJoinTeam : DialogFragment() {

    lateinit var joinTeamBinding : FragmentJoinTeamBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        joinTeamBinding = FragmentJoinTeamBinding.inflate(inflater, container, false)
        return joinTeamBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        joinTeamBinding.requestBtn.setOnClickListener {
            if(joinTeamBinding.teamCodee.text != null){
                requestJoin()
            }else{
                Toast.makeText(requireContext(), "Enter team code", Toast.LENGTH_SHORT).show()
            }

        }

    }

    fun requestJoin(){
        joinTeamBinding.requestBtn.isEnabled = false
        joinTeamBinding.requestBtn.text = "Requesting..."
        val auth = FirebaseAuth.getInstance()
        val teamCode = joinTeamBinding.teamCodee.text.toString()
        val database = Firebase.database
        val myRef = database.getReference("teams")
        myRef.get().addOnCompleteListener {
            if (it.isSuccessful) {
                val snapshot = it.result
                if (!snapshot.child(teamCode).exists()) {
                    Toast.makeText(requireContext(), "Team not found", Toast.LENGTH_SHORT).show()
                    joinTeamBinding.requestBtn.isEnabled = true
                    joinTeamBinding.requestBtn.text = "Request"
                } else {

                    if(snapshot.child(teamCode).child("members").hasChild(auth.currentUser?.uid.toString())
                        || snapshot.child(teamCode).child("admin").value.toString() == auth.currentUser?.uid.toString()){
                        Toast.makeText(requireContext(), "You are already a member of this team", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                    else if(!snapshot.child(teamCode).child("requests").hasChild(auth.currentUser?.uid?.toString()!!)) {
                        myRef.child(teamCode).child("requests").child(auth.currentUser?.uid.toString())
                            .setValue(auth.currentUser?.uid.toString())
                        Toast.makeText(requireContext(), "Request sent", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }else{
                        Toast.makeText(
                            requireActivity(),
                            "Request is already pending...",
                            Toast.LENGTH_SHORT
                        ).show()
                        dismiss()
                    }
                }
            }
        }

    }

}