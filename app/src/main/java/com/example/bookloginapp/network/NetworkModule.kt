package com.example.bookloginapp.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private const val OPEN_LIBRARY_BASE_URL = "https://openlibrary.org/"

    val openLibraryService: OpenLibraryService by lazy {
        createOpenLibraryService()
    }

    private fun createOpenLibraryService(): OpenLibraryService {
        val retrofit = Retrofit.Builder()
            .baseUrl(OPEN_LIBRARY_BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(OpenLibraryService::class.java)
    }

    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}