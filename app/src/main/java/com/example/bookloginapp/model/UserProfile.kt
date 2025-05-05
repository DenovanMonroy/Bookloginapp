package com.example.bookloginapp.model

import java.util.Date

data class UserProfile(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val secondLastName: String = "",
    val birthDate: Date? = null,
    val profilePictureUrl: String = ""
)