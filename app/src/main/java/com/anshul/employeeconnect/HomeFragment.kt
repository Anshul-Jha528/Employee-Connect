package com.anshul.employeeconnect

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    lateinit var homeBinding: FragmentHomeBinding
    var adapter : TeamsAdapter? = null
    var rewardedAd : RewardedAd? = null
    var rewardAdId = "ca-app-pub-9376656451768331/1217381690"
    var maxTeams = 10

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        homeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return homeBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLimit()
        homeBinding.progressBar.isVisible = true
        setUpAdapter()


        homeBinding.createTeam.setOnClickListener {
            createTeam()
        }

        homeBinding.joinTeam.setOnClickListener {
            joinTeam()
        }

        MobileAds.initialize(requireActivity()){
            loadReward()
        }

        val adRequest = AdRequest.Builder().build()
        homeBinding.adView.loadAd(adRequest)


    }

    fun setLimit(){
        val database = Firebase.database
        database.getReference("users").child("maxTeams").get().addOnSuccessListener {
            if(it.exists()){
                maxTeams = it.value.toString().toInt()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setUpAdapter()
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
//        showInterstitialAd()
        startActivity(Intent(requireContext(), ActivityCreateTeam::class.java))
    }

    fun intentJoin(){
//        showInterstitialAd()
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
//                    if (!snapshot.child("team1").exists()) {
//                        intentCreate()
//                    } else if (!snapshot.child("team2").exists()) {
//                        intentCreate()
//                    } else {
//                        Toast.makeText(
//                            requireContext(),
//                            "You cannot have more than 2 teams",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
                    var count =0
                    for(i in snapshot.child("myTeams").children){
                        count++
                    }
                    Log.d("count", "TeamCount" + count)
                    if(count <=2){
                        intentCreate()
                    }else{
                        val dialog = AlertDialog.Builder(requireContext())
                            .setTitle("Limit reached !")
                            .setMessage("Your team joining / creating limit is reached. To add more teams, watch an ad. ")
                            .setPositiveButton ("Watch Ad") { interfaced, l ->
                                showRewardAd(1)
                            }
                            .setNegativeButton("Cancel") { interfaced, l ->
                                interfaced.dismiss()
                            }
                            .create()
                        dialog.show()
                    }

                }
            }
        }
    }

    fun loadReward(){
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(requireActivity(), rewardAdId, adRequest, object : RewardedAdLoadCallback() {

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                    Log.d("error", "Ad failed to load: ${adError.message}")
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    Log.d("success","Ad ready to show!")
                }

        })

    }

    fun showRewardAd(case : Int){
        if (rewardedAd != null) {
            rewardedAd?.let{ad ->
                ad.show(requireActivity()) { reward ->

                    if(case == 1){
                        intentCreate()
                    }else{
                        intentJoin()
                    }

                    loadReward()

                }
            }
        }else{
            loadReward()
            Toast.makeText(requireActivity(),
                "Ad could not be loaded. Please try again later.",
                Toast.LENGTH_SHORT).show()
            Log.d("error","The rewarded ad wasn't ready yet.")
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
//                    if (!snapshot.child("team1").exists()) {
//                        intentJoin()
//                    } else if (snapshot.child("team2").exists()) {
//                        intentJoin()
//                    } else {
//                        Toast.makeText(
//                            requireContext(),
//                            "You cannot have more than 2 teams",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
                    var count =0
                    for(i in snapshot.child("myTeams").children){
                        count++
                    }
                    Log.d("count", "TeamCount" + count)
                    if(count <=2){
                        intentJoin()
                    }else{
                        val dialog = AlertDialog.Builder(requireContext())
                            .setTitle("Limit reached !")
                            .setMessage("Your team joining / creating limit is reached. To add more teams, watch an ad. ")
                            .setPositiveButton ("Watch Ad") { interfaced, l ->
                                showRewardAd(2)
                            }
                            .setNegativeButton("Cancel") { interfaced, l ->
                                interfaced.dismiss()
                            }
                            .create()

                        dialog.show()

                    }

                }
            }
        }
    }

    fun setUpAdapter(){
        val database = Firebase.database
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid.toString()
        val myRef = database.getReference("users").child(uid).child("myTeams")
        var teamList = ArrayList<String>()

        myRef.get().addOnSuccessListener {
            if (it.exists()) {
                val snapshot = it

                for(i in snapshot.children){
                    teamList.add(i.value.toString())
                }

                if(teamList.size>=5){
                    homeBinding.adView2.isVisible = true
                }else{
                    homeBinding.adView2.isVisible = false
                }

                if(teamList.size >= maxTeams){
                    homeBinding.createTeam.isVisible = false
                    homeBinding.joinTeam.isVisible = false
                }

                if(teamList.size == 0){
                    homeBinding.progressBar.isVisible = false
                }else{
                    adapter = TeamsAdapter(requireContext(), teamList)
                    homeBinding.recyclerViewTeams .layoutManager = LinearLayoutManager(requireContext())
                    homeBinding.recyclerViewTeams.adapter = adapter
                    homeBinding.progressBar.isVisible = false
                    adapter!!.notifyDataSetChanged()
                    homeBinding.recyclerViewTeams.isVisible = true
                    homeBinding.progressBar.isVisible = false
                }

            }else{
                homeBinding.adView2.isVisible = false
                homeBinding.progressBar.isVisible = false
            }
        }
    }


}