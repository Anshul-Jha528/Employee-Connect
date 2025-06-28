package com.anshul.employeeconnect

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import androidx.fragment.app.Fragment
import com.anshul.employeeconnect.databinding.FragmentLogin1Binding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.*

class FragmentSignin : Fragment() {

    lateinit var signInBinding: FragmentLogin1Binding

    val auth = FirebaseAuth.getInstance()
    lateinit var name : String
    lateinit var email : String

    lateinit var sharedPreferences : SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        signInBinding = FragmentLogin1Binding.inflate(inflater, container, false)
        return signInBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        signInBinding.btnSignin.setOnClickListener {
            if(signInBinding.name.text!!.isEmpty() || signInBinding.email.text!!.isEmpty()){
                Toast.makeText(context, "Please fill the details", Toast.LENGTH_SHORT).show()
            }else {
                name = signInBinding.name.text.toString()
                email = signInBinding.email.text.toString()

                saveName(name, email)
                sendSignInLink(email)
            }


        }


        signInBinding.btnSigninwithGoogle.setOnClickListener{

//            if(signInBinding.name.text!!.isEmpty()){
//                Toast.makeText(context, "Please write your name", Toast.LENGTH_SHORT).show()
//            }else {
//                name = signInBinding.name.text.toString()

                if (Build.VERSION.SDK_INT >= 34) {
                    signInWithGoogle()
                } else {
                    signInWithTraditionalMethod()
                }
//            }

        }




    }

    fun saveName(name : String, email: String){
        sharedPreferences = requireActivity().getSharedPreferences("my shared preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("userName", name)
        editor.putString("userEmail",email)
        editor.apply()

    }

    fun sendSignInLink(email : String){

        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setUrl("https://employee-connect-bce80.firebaseapp.com/finishSignUp?cartId=1234")
            .setHandleCodeInApp(true)
            .setAndroidPackageName(
                "com.anshul.employeeconnect",
                true,
                "9"
            )
            .build()

        auth.sendSignInLinkToEmail(email, actionCodeSettings)
            .addOnCompleteListener{ task ->

                if(task.isSuccessful){
                    Toast.makeText(context, "Sign in link sent to your email.", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(context, task.exception?.message.toString(), Toast.LENGTH_SHORT).show()
                }

            }

    }

    fun signInWithGoogle(){
        val googleOption = GetGoogleIdOption.Builder()
            .setServerClientId(getString(R.string.web_client_id))
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()


        val request = androidx.credentials.GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()

        var response : GetCredentialResponse

        try{

            val credentialManager = CredentialManager.create(requireContext())

            CoroutineScope(Dispatchers.IO).async {
                response = credentialManager.getCredential(
                    request = request,
                    context = requireContext())

                response.credential?.let{ it ->
                    handleSignin(it)
                }
            }
        }catch(e : Exception){

            Toast.makeText(context, e.message.toString(), Toast.LENGTH_SHORT).show()

        }






    }

    fun handleSignin(credential : androidx.credentials.Credential){
        if(credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL){
            val googleIdTokemCredential = GoogleIdTokenCredential.createFrom(credential.data)

            firebaseAuthWithGoogle(googleIdTokemCredential.idToken)
        }else{
            Toast.makeText(context, "Invalid credential", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    Toast.makeText(context, "Signed in successfully !", Toast.LENGTH_SHORT).show()

//                    email = user?.email.toString()
//                    saveName(name, email)

                    (activity as LoginActivity)?.signInSuccess()

                } else {
                    // If sign in fails, display a message to the user

                    Toast.makeText(context, "Cannot sign in", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun signInWithTraditionalMethod(){
        val gso = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        val mauth = FirebaseAuth.getInstance()

        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, 1)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 1 ){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            }catch(e : ApiException){
                Toast.makeText(context, "Google sign in failed", Toast.LENGTH_SHORT).show()
            }
        }

    }


}

