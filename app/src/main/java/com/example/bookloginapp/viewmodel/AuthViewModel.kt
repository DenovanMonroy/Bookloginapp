package com.example.bookloginapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _loginState.value = LoginState.Success
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                _loginState.value = LoginState.Success
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _loginState.value = LoginState.Initial
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}

sealed class LoginState {
    object Initial : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}