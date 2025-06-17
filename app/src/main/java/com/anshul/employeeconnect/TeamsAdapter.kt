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
import com.google.firebase.Timestamp

class TeamsAdapter(
    var context: Context ,
    var teams : ArrayList<Teams>
) : RecyclerView.Adapter<TeamsAdapter.viewHolder>() {

    val myTeams : ArrayList<Teams> = teams

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
        holder.adapterBinding.teamName.text = myTeams[position].name
        holder.adapterBinding.teamDesc.text = myTeams[position].desc
        holder.adapterBinding.noOfMember.text = myTeams[position].noOfMembers.toString() + " members"
        holder.adapterBinding.teamCardView.setOnClickListener {
            val intent = Intent(context, MyTeamActivity::class.java)
            intent.putExtra("teamId", myTeams[position].code)
            intent.putExtra("teamName", myTeams[position].name)
            intent.putExtra("teamDesc", myTeams[position].desc)
            intent.putExtra("teamCode", myTeams[position].code)
            context.startActivity(intent)

        }


    }

    override fun getItemCount(): Int {
        return myTeams.size
    }

    inner class viewHolder(val adapterBinding : TeamItemBinding) : RecyclerView.ViewHolder(adapterBinding.root){

    }


}