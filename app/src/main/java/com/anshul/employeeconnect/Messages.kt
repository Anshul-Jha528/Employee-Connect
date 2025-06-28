package com.anshul.employeeconnect

data class Messages(
    val senderId : String = "",
    val senderName : String = "",
    val message : String = "",
    val time : Long = 0L
) {
    constructor() : this("", "", "", 0L)

    fun toMap() : Map<String, Any>{
        return mapOf(
            "senderId" to senderId,
            "senderName" to senderName,
            "message" to message,
            "time" to time)
    }

}