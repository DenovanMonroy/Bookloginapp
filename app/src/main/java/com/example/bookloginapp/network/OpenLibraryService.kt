package com.example.bookloginapp.network

import com.example.bookloginapp.model.OpenLibraryResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenLibraryService {

    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): Response<OpenLibraryResponse>
}