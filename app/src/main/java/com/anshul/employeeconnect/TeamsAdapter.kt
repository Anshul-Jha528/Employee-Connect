package com.anshul.employeeconnect

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anshul.employeeconnect.databinding.FragmentTaskBinding
import com.anshul.employeeconnect.databinding.TaskItemBinding
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.view.LayoutInflater
import com.anshul.employeeconnect.databinding.TeamItemBinding
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.database.database

class TeamsAdapter(
    var context: Context ,
    var teamList : ArrayList<String>
) : RecyclerView.Adapter<TeamsAdapter.viewHolder>() {

    val myTeams = teamList

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): viewHolder {

        val binding = TeamItemBinding.inflate(LayoutInflater.from( parent.context), parent , false)
        return viewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: viewHolder,
        position: Int
    ) {

        val database = Firebase.database
        val myRef = database.getReference("teams").child(myTeams[position])
        myRef.get().addOnSuccessListener { snapshot ->
            val team = snapshot.getValue(Teams::class.java)
            holder.adapterBinding.noOfMember.text = team!!.noOfMembers.toString() + " members"
            holder.adapterBinding.teamName.text = team.name
            holder.adapterBinding.teamDesc.text = team.desc
        }


        holder.adapterBinding.teamCardView.setOnClickListener {
            val intent = Intent(context, MyTeamActivity::class.java)
            intent.putExtra("teamId", myTeams[position])
            intent.putExtra("teamCode", myTeams[position])
            context.startActivity(intent)

        }


    }

    override fun getItemCount(): Int {
        return myTeams.size
    }

    inner class viewHolder(val adapterBinding : TeamItemBinding) : RecyclerView.ViewHolder(adapterBinding.root){

    }


}