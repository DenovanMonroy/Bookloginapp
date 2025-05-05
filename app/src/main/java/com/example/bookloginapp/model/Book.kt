package com.example.bookloginapp.model

data class Book(
    val id: String = "",
    val key: String = "",
    val title: String = "",
    val author: String = "",
    val coverUrl: String = "",
    val description: String = "",
    val isFavorite: Boolean = false
) {
    // Constructor vacío necesario para Firebase
    constructor() : this("", "", "", "", "", "", false)
}