package com.example.bookloginapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bookloginapp.model.Book
import com.example.bookloginapp.ui.components.BookItem
import com.example.bookloginapp.viewmodel.BooksViewModel
import com.example.bookloginapp.viewmodel.FavoriteBooksState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    booksViewModel: BooksViewModel,
    onBookClick: (Book) -> Unit
) {
    val favoriteBooksState by booksViewModel.favoriteBooksState.collectAsState()

    // Cargar favoritos al entrar a la pantalla
    LaunchedEffect(key1 = true) {
        booksViewModel.loadFavoriteBooks()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Favoritos") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = favoriteBooksState) {
                is FavoriteBooksState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is FavoriteBooksState.Empty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Aún no tienes libros favoritos",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Los libros que marques como favoritos aparecerán aquí",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is FavoriteBooksState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.books) { book ->
                            BookItem(
                                book = book,
                                onClick = { onBookClick(book) },
                                onFavoriteClick = {
                                    booksViewModel.toggleFavorite(book)
                                }
                            )
                        }
                    }
                }
                is FavoriteBooksState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error: ${(state as FavoriteBooksState.Error).message}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}