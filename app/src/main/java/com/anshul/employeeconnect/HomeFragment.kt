package com.anshul.employeeconnect

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anshul.employeeconnect.databinding.FragmentHomeBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    lateinit var homeBinding: FragmentHomeBinding
    var adapter : TeamsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        homeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return homeBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeBinding.progressBar.isVisible = true
        setUpAdapter()


        homeBinding.createTeam.setOnClickListener {
            createTeam()
        }

        homeBinding.joinTeam.setOnClickListener {
            val joinTeam = FragmentJoinTeam()
            joinTeam.isCancelable = true
            joinTeam.show(parentFragmentManager, "joinTeam")
        }

        val adRequest = AdRequest.Builder().build()
        homeBinding.adView.loadAd(adRequest)
    }

    override fun onResume() {
        super.onResume()
        setRecyclerView()
    }

    fun setRecyclerView(){
        val database = Firebase.database
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid.toString()
        val myRef = database.getReference("users").child(uid)
        val myRef2 = database.getReference("teams")
        myRef.get().addOnCompleteListener { task ->
            if(task.isSuccessful){
                val teams = ArrayList<Teams>()
                val snapshot = task.result
                var t1 : String? = null
                var t2 : String? = null
                if(snapshot.child("team1").exists()){
                    t1 = (snapshot.child("team1").value.toString())

                }
                if(snapshot.child("team2").exists()){
                    t2 = (snapshot.child("team2").value.toString())
                }
                if(t1 != null || t2 != null){
                    myRef2.get().addOnCompleteListener { task2 ->
                        if(task2.isSuccessful){
                            val snapshot2 = task2.result
                            if(t1 != null){
                                teams.add(snapshot2.child(t1).getValue(Teams::class.java)!!)
                            }
                            if(t2 != null){
                                teams.add(snapshot2.child(t2).getValue(Teams::class.java)!!)
                            }
                            homeBinding.progressBar.isVisible = false
                            adapter = TeamsAdapter(requireContext(), teams)
                            adapter!!.notifyDataSetChanged()
                            homeBinding.recyclerViewTeams.adapter = adapter
                        }
                    }
                }else{
                    homeBinding.progressBar.isVisible = false
                }
            }
        }

    }

    private var interstitialAd: InterstitialAd? = null

    fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(requireActivity(), "ca-app-pub-9376656451768331/5119343542", adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            })
    }

    fun showInterstitialAd() {
        if (interstitialAd != null) {
            interstitialAd?.show(requireActivity())
        } else {
            loadInterstitialAd()
        }
    }

    fun intentCreate(){
        showInterstitialAd()
        startActivity(Intent(requireContext(), ActivityCreateTeam::class.java))
    }

    fun intentJoin(){
        showInterstitialAd()
        val joinTeam = FragmentJoinTeam()
        joinTeam.isCancelable = true
        joinTeam.show(parentFragmentManager, "joinTeam")
    }

    fun createTeam() {
        val database = Firebase.database
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid.toString()
        val myRef = database.getReference("users").child(uid)

        CoroutineScope(Dispatchers.IO).launch {
            myRef.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val snapshot = it.result
                    if (!snapshot.child("team1").exists()) {
                        intentCreate()
                    } else if (!snapshot.child("team2").exists()) {
                        intentCreate()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "You cannot have more than 2 teams",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    fun joinTeam() {
        val database = Firebase.database
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid.toString()
        val myRef = database.getReference("users").child(uid)

        CoroutineScope(Dispatchers.IO).launch {
            myRef.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val snapshot = it.result
                    if (!snapshot.child("team1").exists()) {
                        intentJoin()
                    } else if (snapshot.child("team2").exists()) {
                        intentJoin()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "You cannot have more than 2 teams",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    fun setUpAdapter(){
        val database = Firebase.database
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid.toString()
        val myRef = database.getReference("users").child(uid)
        var team1 : String? = null
        var team2 : String? = null

        myRef.get().addOnCompleteListener {
            if (it.isSuccessful) {
                val snapshot = it.result
                if (snapshot.child("team1").exists()) {
                    team1 = snapshot.child("team1").value.toString()
                }
                if (snapshot.child("team2").exists()) {
                    team2 = snapshot.child("team2").value.toString()
                }
                if(team1 != null || team2 != null){
                    val myRef2 = database.getReference("teams")
                    val teams = ArrayList<Teams>()
                    myRef2.get().addOnCompleteListener {
                        if (it.isSuccessful) {
                            val snapshot = it.result
                            if(team1 != null){
                                val team = snapshot.child(team1).getValue(Teams::class.java)!!
                                teams.add(team!!)
                            }
                            if(team2 != null){
                                val team = snapshot!!.child(team2).getValue(Teams::class.java)!!
                                teams.add(team)
                            }
                            homeBinding.recyclerViewTeams.layoutManager = LinearLayoutManager(requireContext())
                            val adapter = TeamsAdapter(requireContext(), teams)
                            adapter.notifyDataSetChanged()
                            homeBinding.recyclerViewTeams.adapter = adapter
                        }
                    }

                }
            }
        }
    }


}