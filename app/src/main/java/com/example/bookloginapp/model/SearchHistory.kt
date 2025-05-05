package com.example.bookloginapp.model

import com.google.firebase.database.IgnoreExtraProperties
import java.util.Date

@IgnoreExtraProperties
data class SearchHistory(
    val query: String = "",
    val timestamp: Long = 0,
    val id: String = ""
) {
    // Constructor vacío necesario para Firebase
    constructor() : this("", 0, "")

    fun getFormattedDate(): String {
        val date = Date(timestamp)
        val format = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        return format.format(date)
    }
}