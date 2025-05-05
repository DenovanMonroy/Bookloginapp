package com.example.bookloginapp.model

import com.google.gson.annotations.SerializedName

data class OpenLibraryResponse(
    val numFound: Int,
    val start: Int,
    val docs: List<BookDoc>
)

data class BookDoc(
    val key: String,
    val title: String,
    @SerializedName("author_name") val authorNames: List<String>? = null,
    @SerializedName("cover_i") val coverId: Int? = null,
    @SerializedName("first_sentence") val firstSentence: List<String>? = null,
    @SerializedName("isbn") val isbn: List<String>? = null
)