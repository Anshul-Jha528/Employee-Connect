package com.anshul.employeeconnect

import com.google.firebase.Timestamp

data class UserTasks(
    val taskId: String,
    val taskTitle: String,
    val taskDesc: String,
    val taskLocation: String,
    val taskDate: String,
    val taskStartTime: Long,
    val taskEndTime: Long? = null,
    val status : Boolean = false,
    val admin : String
) {
     ;constructor() : this("", "", "", "", "", 0, null, false, "")

}