package com.anshul.employeeconnect

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.anshul.employeeconnect.databinding.TeamMemberItemBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database

class TeamMemberAdapter(
    val context : Context,
    val members : ArrayList<String>,
    val admin : String
) : RecyclerView.Adapter<TeamMemberAdapter.viewHolder>() {

    val auth = Firebase.auth
    val myUid = auth.currentUser?.uid?.toString()
    val database = Firebase.database
    val myRef = database.getReference("users")

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): viewHolder {
        val binding = TeamMemberItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return viewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: viewHolder,
        position: Int
    ) {
        if(members[position] == admin && members[position] == myUid!!) {
            holder.adapterBinding.assignTask.isVisible = false
            holder.adapterBinding.info.isVisible = false
            holder.adapterBinding.admin.text = "Admin/You"
        }
        else if(members[position]==admin) {
            holder.adapterBinding.assignTask.isVisible = false
            holder.adapterBinding.info.isVisible = true
            holder.adapterBinding.admin.text = "Admin"
        }
        else if(members[position]==myUid!!) {
            holder.adapterBinding.assignTask.isVisible = false
            holder.adapterBinding.info.isVisible = false
            holder.adapterBinding.admin.text = "You"
        }else{
            holder.adapterBinding.admin.isVisible = false
        }
        myRef.child(members[position]) .get().addOnCompleteListener { task ->
            val snapshot = task.result
            holder.adapterBinding.name.text = snapshot.child("name").value.toString()
            holder.adapterBinding.email.text = snapshot.child("email").value.toString()
        }
        if(myUid!! != admin){
            holder.adapterBinding.assignTask.isVisible = false
        }

        holder.adapterBinding.assignTask.setOnClickListener {
            (context as MyTeamActivity).assignTask(members[position])
        }
        holder.adapterBinding.info.setOnClickListener {
            (context as MyTeamActivity).viewInfo(members[position])
        }


    }

    override fun getItemCount(): Int {
        return members.size
    }
    fun getUid(position : Int) : String {
        return members[position]
    }
    inner class viewHolder(val adapterBinding : TeamMemberItemBinding) : RecyclerView.ViewHolder(adapterBinding.root){}




}