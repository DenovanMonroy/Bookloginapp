package com.example.bookloginapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bookloginapp.R
import com.example.bookloginapp.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToFavorites: () -> Unit, // Añadido parámetro para navegación a favoritos
    onNavigateToProfile: () -> Unit    // Añadido parámetro para navegación a perfil
) {
    val authViewModel: AuthViewModel = viewModel()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Book App")
                },
                actions = {
                    IconButton(onClick = {
                        authViewModel.signOut()
                        onNavigateToLogin()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Banner superior con imagen
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Puedes cambiar esto por una imagen que tengas en tus recursos
                    //Image(
                    //    painter = painterResource(id = R.drawable.book_banner), // Asegúrate de tener esta imagen en tus recursos
                    //    contentDescription = "Banner de libros",
                    //    contentScale = ContentScale.Crop,
                    //    modifier = Modifier.fillMaxSize()
                    //)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Text(
                            text = "Descubre tu próxima gran lectura",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "¿Qué deseas hacer hoy?",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botones de acciones principales
            HomeActionButton(
                icon = Icons.Default.Search,
                text = "Buscar Libros",
                onClick = onNavigateToSearch
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Asegúrate de tener los handlers de navegación correctos
            HomeActionButton(
                icon = Icons.Default.Favorite,
                text = "Mis Favoritos",
                onClick = onNavigateToFavorites // Usa el parámetro de navegación a favoritos
            )

            Spacer(modifier = Modifier.height(16.dp))

            HomeActionButton(
                icon = Icons.Default.Person,
                text = "Mi Perfil",
                onClick = onNavigateToProfile // Usa el parámetro de navegación al perfil
            )
        }
    }
}

@Composable
fun HomeActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}