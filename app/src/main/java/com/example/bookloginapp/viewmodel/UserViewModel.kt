package com.example.bookloginapp.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookloginapp.model.UserProfile
import com.example.bookloginapp.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class UserViewModel : ViewModel() {

    private val repository = UserRepository()

    private val _userProfileState = MutableStateFlow<UserProfileState>(UserProfileState.Loading)
    val userProfileState: StateFlow<UserProfileState> = _userProfileState.asStateFlow()

    private val _updateProfileState = MutableStateFlow<UpdateProfileState>(UpdateProfileState.Initial)
    val updateProfileState: StateFlow<UpdateProfileState> = _updateProfileState.asStateFlow()

    private var selectedImageUri: Uri? = null

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        _userProfileState.value = UserProfileState.Loading
        viewModelScope.launch {
            try {
                val userProfile = repository.getUserProfile()
                if (userProfile != null) {
                    _userProfileState.value = UserProfileState.Success(userProfile)
                } else {
                    _userProfileState.value = UserProfileState.NotFound
                }
            } catch (e: Exception) {
                _userProfileState.value = UserProfileState.Error("Error al cargar perfil: ${e.message}")
            }
        }
    }

    fun setSelectedImage(uri: Uri) {
        selectedImageUri = uri
    }

    fun updateProfile(
        firstName: String,
        lastName: String,
        secondLastName: String,
        birthDate: Date?
    ) {
        if (firstName.isBlank() || lastName.isBlank()) {
            _updateProfileState.value = UpdateProfileState.Error("Nombre y apellido paterno son obligatorios")
            return
        }

        _updateProfileState.value = UpdateProfileState.Loading
        viewModelScope.launch {
            try {
                val success = repository.updateUserProfile(
                    firstName = firstName,
                    lastName = lastName,
                    secondLastName = secondLastName,
                    birthDate = birthDate,
                    profileImageUri = selectedImageUri
                )

                if (success) {
                    _updateProfileState.value = UpdateProfileState.Success
                    // Reload profile
                    loadUserProfile()
                    // Clear selected image
                    selectedImageUri = null
                } else {
                    _updateProfileState.value = UpdateProfileState.Error("No se pudo actualizar el perfil")
                }
            } catch (e: Exception) {
                _updateProfileState.value = UpdateProfileState.Error("Error: ${e.message}")
            }
        }
    }
}

sealed class UserProfileState {
    object Loading : UserProfileState()
    object NotFound : UserProfileState()
    data class Success(val profile: UserProfile) : UserProfileState()
    data class Error(val message: String) : UserProfileState()
}

sealed class UpdateProfileState {
    object Initial : UpdateProfileState()
    object Loading : UpdateProfileState()
    object Success : UpdateProfileState()
    data class Error(val message: String) : UpdateProfileState()
}