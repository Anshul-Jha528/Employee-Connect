package com.anshul.employeeconnect

data class Teams(
    val name : String = "",
    val desc : String = "",
    val code : String = "",
    val admin : String = "",
    val noOfMembers : Int = 0,
    val members : HashMap<String, String> = hashMapOf(),
    val requests : HashMap<String, String> = hashMapOf()
) {
}