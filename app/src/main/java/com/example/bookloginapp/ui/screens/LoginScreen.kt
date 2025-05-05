package com.example.bookloginapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bookloginapp.viewmodel.AuthViewModel
import com.example.bookloginapp.viewmodel.LoginState

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }

    val loginState by authViewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            onNavigateToHome()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isSignUp) "Crear Cuenta" else "Iniciar Sesión",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo Electrónico") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            if (loginState is LoginState.Error) {
                Text(
                    text = (loginState as LoginState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Button(
                onClick = {
                    if (isSignUp) {
                        authViewModel.signUp(email, password)
                    } else {
                        authViewModel.signIn(email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                enabled = email.isNotEmpty() && password.isNotEmpty() && loginState !is LoginState.Loading
            ) {
                if (loginState is LoginState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isSignUp) "Registrarse" else "Iniciar Sesión")
                }
            }

            TextButton(
                onClick = { isSignUp = !isSignUp },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    if (isSignUp) "¿Ya tienes cuenta? Inicia sesión"
                    else "¿No tienes cuenta? Regístrate"
                )
            }
        }
    }
}