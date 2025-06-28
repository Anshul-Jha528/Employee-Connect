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

    constructor() : this("", "", "", "", 0, hashMapOf(), hashMapOf())

    fun toMap() : Map<String, Any>{
        return mapOf(
            "name" to name,
            "desc" to desc,
            "code" to code,
            "admin" to admin,
            "noOfMembers" to noOfMembers,
            "members" to members,
            "requests" to requests
        )
    }

}