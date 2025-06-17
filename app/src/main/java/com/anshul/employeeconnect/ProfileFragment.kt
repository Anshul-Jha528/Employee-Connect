package com.anshul.employeeconnect

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.anshul.employeeconnect.databinding.FragmentProfileBinding
import com.google.android.gms.ads.AdRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class ProfileFragment : Fragment() {

    lateinit var profileBinding: FragmentProfileBinding
    val auth = FirebaseAuth.getInstance()
    val database : FirebaseDatabase = Firebase.database
    val myRef = database.getReference("users")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        profileBinding = FragmentProfileBinding.inflate(
            inflater,
            container,
            false
        )
        return profileBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileBinding.btnUpdateProfile.setOnClickListener {
            startActivity(Intent(requireContext(), UpdateProfile::class.java))
        }

        myRef.child(auth.currentUser?.uid.toString()).get().addOnSuccessListener {
            profileBinding.profileName.text = it.child("name").value.toString()
        }

        profileBinding.profileEmail.text = "Email ID : ${auth.currentUser?.email}"



        profileBinding.profileSignOut.setOnClickListener {
            val dialog = AlertDialog.Builder(requireActivity())
            dialog.setTitle("Sign Out")
                .setMessage("Do you really want to sign out? You can login again.")
                .setPositiveButton ("Sign out", DialogInterface.OnClickListener{dialogInterface, l ->
                    (activity as MainActivity ).signOut()
                })
                .setNegativeButton("Stay signed in", DialogInterface.OnClickListener{dialogInterface, l ->
                    dialogInterface.dismiss()
                })
                .create()
                .show()
        }

        profileBinding.viewInfo.setOnClickListener {
            val viewInfo = FragmentViewInfo()
            val bundle = Bundle()
            bundle.putString("uid", auth.currentUser?.uid.toString())
            viewInfo.arguments = bundle
            viewInfo.isCancelable = false
            viewInfo.show(parentFragmentManager, "viewInfo")
        }

        val adRequest = AdRequest.Builder().build()
        profileBinding.adView.loadAd(adRequest)



    }



}