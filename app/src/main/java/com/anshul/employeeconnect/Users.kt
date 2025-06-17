package com.anshul.employeeconnect

data class Users(
    val name: String = "",
    val contact: String = "",
    val email: String = "",
    val age: Int = -1,
    val gender: String = "",
    val job: String = "",
    val empID: String = "",
    val jobDesc: String = "",
    val address: String = ""
) {

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "contact" to contact,
            "email" to email,
            "age" to age,
            "gender" to gender,
            "job" to job,
            "empID" to empID,
            "jobDesc" to jobDesc,
            "address" to address
        )
    }

}