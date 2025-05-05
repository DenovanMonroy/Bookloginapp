package com.example.bookloginapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.bookloginapp.model.Book
import com.example.bookloginapp.model.SearchHistory
import com.example.bookloginapp.ui.components.BookItem
import com.example.bookloginapp.viewmodel.BooksViewModel
import com.example.bookloginapp.viewmodel.SearchHistoryState
import com.example.bookloginapp.viewmodel.SearchState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchScreen(
    booksViewModel: BooksViewModel,
    onBookClick: (Book) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showSearchHistory by remember { mutableStateOf(false) }
    val searchState by booksViewModel.searchState.collectAsState()
    val searchHistoryState by booksViewModel.searchHistoryState.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Cargar historial al entrar a la pantalla
    LaunchedEffect(key1 = true) {
        booksViewModel.loadSearchHistory()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Barra de búsqueda
        TopAppBar(
            title = {
                TextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        showSearchHistory = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            showSearchHistory = focusState.isFocused
                        },
                    placeholder = { Text("Buscar libros...") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    trailingIcon = {
                        IconButton(onClick = {
                            if (searchQuery.isNotEmpty()) {
                                booksViewModel.searchBooks(searchQuery)
                                booksViewModel.saveSearchQuery(searchQuery)
                                showSearchHistory = false
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (searchQuery.isNotEmpty()) {
                                booksViewModel.searchBooks(searchQuery)
                                booksViewModel.saveSearchQuery(searchQuery)
                                showSearchHistory = false
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }
                        }
                    )
                )
            }
        )

        // Panel de historial DEBAJO DE LA BARRA pero por encima del contenido
        AnimatedVisibility(
            visible = showSearchHistory,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            SearchHistoryOverlay(
                historyState = searchHistoryState,
                onHistoryItemClick = { query ->
                    searchQuery = query
                    booksViewModel.searchBooks(query)
                    booksViewModel.saveSearchQuery(query)
                    showSearchHistory = false
                    focusManager.clearFocus()
                    keyboardController?.hide()
                },
                onDeleteHistoryItem = { searchId ->
                    booksViewModel.deleteSearchHistoryItem(searchId)
                },
                onClearHistory = {
                    booksViewModel.clearSearchHistory()
                },
                onDismiss = {
                    showSearchHistory = false
                }
            )
        }

        // Contenido principal (resultados de búsqueda)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f) // Para que ocupe el resto del espacio disponible
        ) {
            when (val state = searchState) {
                is SearchState.Initial -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Busca tu próxima lectura",
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                is SearchState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is SearchState.Success -> {
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
                is SearchState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No se encontraron resultados para \"$searchQuery\"",
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                is SearchState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // Solicitar foco automáticamente cuando se muestra la pantalla
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
fun SearchHistoryOverlay(
    historyState: SearchHistoryState,
    onHistoryItemClick: (String) -> Unit,
    onDeleteHistoryItem: (String) -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Búsquedas recientes",
                    style = MaterialTheme.typography.titleMedium
                )

                Row {
                    if (historyState is SearchHistoryState.Success && historyState.history.isNotEmpty()) {
                        TextButton(onClick = onClearHistory) {
                            Text("Borrar todo")
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (historyState) {
                is SearchHistoryState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                }
                is SearchHistoryState.Empty, is SearchHistoryState.Initial -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No hay búsquedas recientes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                is SearchHistoryState.Success -> {
                    if (historyState.history.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No hay búsquedas recientes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        // Limitamos la altura máxima del panel de historial
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                        ) {
                            historyState.history.forEach { history ->
                                SearchHistoryItem(
                                    history = history,
                                    onClick = { onHistoryItemClick(history.query) },
                                    onDelete = { onDeleteHistoryItem(history.id) }
                                )
                            }
                        }
                    }
                }
                is SearchHistoryState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Error: ${historyState.message}",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchHistoryItem(
    history: SearchHistory,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = history.query,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = history.getFormattedDate(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Eliminar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}