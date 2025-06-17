package com.anshul.employeeconnect

data class Messages(
    val senderId : String = "",
    val senderName : String = "",
    val message : String = "",
    val time : Long = 0L
) {
}