package com.anshul.employeeconnect

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Adapter
import androidx.recyclerview.widget.RecyclerView
import com.anshul.employeeconnect.databinding.RequestMemberItemBinding
import com.google.firebase.Firebase
import com.google.firebase.database.database

class TeamRequestAdapter(
    var requests : ArrayList<String>,
    var context : Context
) : RecyclerView.Adapter<TeamRequestAdapter.viewHolder>() {

    val database = Firebase.database
    val myRef = database.getReference("users")

    inner class viewHolder(val binding : RequestMemberItemBinding) : RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): viewHolder {
        val binding = RequestMemberItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return viewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: viewHolder,
        position: Int
    ) {
        val uid = requests[position].toString()
        myRef.child(uid).get().addOnSuccessListener { task->
            val snapshot = task.getValue(Users::class.java)
            holder.binding.name.text = snapshot!!.name
            holder.binding.email.text = snapshot.email
            holder.binding.btnAdd.setOnClickListener {
                (context as MyTeamActivity).add(uid)
            }
            holder.binding.btnReject.setOnClickListener {
                (context as MyTeamActivity).reject(uid)
            }
        }

    }

    override fun getItemCount(): Int {
        return requests.size
    }


}