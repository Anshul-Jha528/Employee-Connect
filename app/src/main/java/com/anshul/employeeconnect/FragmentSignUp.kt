package com.anshul.employeeconnect

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.anshul.employeeconnect.databinding.FragmentSignUpBinding


class FragmentSignUp : Fragment() {

    lateinit var signUpBinding: FragmentSignUpBinding

    override fun onCreateView(



        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        signUpBinding = FragmentSignUpBinding.inflate(layoutInflater)
        return signUpBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        signUpBinding.btnSignin2.setOnClickListener{
            val email = signUpBinding.email.text.toString()
            val password = signUpBinding.password.text.toString()
        }




    }


}