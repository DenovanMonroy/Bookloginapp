package com.example.bookloginapp.repository

import android.net.Uri
import com.example.bookloginapp.model.UserProfile
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.util.Date

class UserRepository {

    private val database = Firebase.database.reference
    private val auth = Firebase.auth
    private val storage = Firebase.storage

    suspend fun getUserProfile(): UserProfile? {
        val currentUser = auth.currentUser ?: return null

        return try {
            val snapshot = database
                .child("users")
                .child(currentUser.uid)
                .child("profile")
                .get()
                .await()

            if (snapshot.exists()) {
                val profile = snapshot.getValue(UserProfile::class.java)
                profile?.copy(uid = currentUser.uid)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUserProfile(
        firstName: String,
        lastName: String,
        secondLastName: String,
        birthDate: Date?,
        profileImageUri: Uri? = null
    ): Boolean {
        val currentUser = auth.currentUser ?: return false

        return try {
            var profilePictureUrl = ""

            // Upload image if provided
            profileImageUri?.let { uri ->
                val storageRef = storage.reference
                    .child("profile_pictures")
                    .child("${currentUser.uid}.jpg")

                storageRef.putFile(uri).await()
                profilePictureUrl = storageRef.downloadUrl.await().toString()
            }

            // If no new image was uploaded, get the existing image URL
            if (profilePictureUrl.isEmpty()) {
                val existingProfile = getUserProfile()
                profilePictureUrl = existingProfile?.profilePictureUrl ?: ""
            }

            val userProfile = UserProfile(
                uid = currentUser.uid,
                firstName = firstName,
                lastName = lastName,
                secondLastName = secondLastName,
                birthDate = birthDate,
                profilePictureUrl = profilePictureUrl
            )

            database
                .child("users")
                .child(currentUser.uid)
                .child("profile")
                .setValue(userProfile)
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }
}