package com.example.bookloginapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.bookloginapp.model.Book
import com.example.bookloginapp.viewmodel.BooksViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    book: Book,
    onBackClick: () -> Unit,
    booksViewModel: BooksViewModel = viewModel()
) {
    var currentBook by remember { mutableStateOf(book) }
    var showNotes by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    val bookNotes by booksViewModel.bookNotes.collectAsState()
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val currentDate = dateFormat.format(Date())

    // Cargar notas al entrar a la pantalla
    LaunchedEffect(currentBook.key) {
        booksViewModel.loadBookNotes(currentBook.key)
    }

    // Actualizar notas cuando cambian en el ViewModel
    LaunchedEffect(bookNotes) {
        notes = bookNotes
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalle del libro",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        booksViewModel.toggleFavorite(currentBook)
                        currentBook = currentBook.copy(isFavorite = !currentBook.isFavorite)
                    }) {
                        Icon(
                            imageVector = if (currentBook.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (currentBook.isFavorite) "Quitar de favoritos" else "Añadir a favoritos",
                            tint = if (currentBook.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = {
                        // Compartir funcionalidad
                        val shareText = "Te recomiendo: ${currentBook.title} por ${currentBook.author}"
                        val sendIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(android.content.Intent.createChooser(sendIntent, "Compartir libro"))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir libro"
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
                .verticalScroll(rememberScrollState())
        ) {
            // Header con portada y detalles principales
            BookHeader(currentBook)

            Divider(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .fillMaxWidth()
            )

            // Detalles adicionales
            BookDetailSection(currentBook)

            Divider(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .fillMaxWidth()
            )

            // Descripción completa
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Descripción",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentBook.description.ifEmpty { "Sin descripción disponible" },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Estado de lectura
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Estado de lectura",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReadingStateChip(
                        text = "No leído",
                        selected = true,
                        onClick = { /* Cambiar estado */ }
                    )
                    ReadingStateChip(
                        text = "Leyendo",
                        selected = false,
                        onClick = { /* Cambiar estado */ }
                    )
                    ReadingStateChip(
                        text = "Leído",
                        selected = false,
                        onClick = { /* Cambiar estado */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notas personales
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mis Notas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = { showNotes = !showNotes }) {
                        Icon(
                            imageVector = if (showNotes) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Mostrar notas"
                        )
                    }
                }

                AnimatedVisibility(
                    visible = showNotes,
                    enter = expandVertically(
                        animationSpec = tween(durationMillis = 300)
                    ) + fadeIn(
                        animationSpec = tween(durationMillis = 300)
                    ),
                    exit = shrinkVertically(
                        animationSpec = tween(durationMillis = 300)
                    ) + fadeOut(
                        animationSpec = tween(durationMillis = 300)
                    )
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Última actualización: $currentDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = { Text("Añade tus notas sobre este libro...") },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { notes = bookNotes },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("Cancelar")
                            }

                            Button(
                                onClick = { booksViewModel.saveBookNotes(currentBook.key, notes) }
                            ) {
                                Text("Guardar")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Enlaces externos
            BookExternalLinks(currentBook) { url ->
                uriHandler.openUri(url)
            }

            // Recomendaciones similares
            SimilarBooksSection()

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun BookHeader(book: Book) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Portada del libro
            if (book.coverUrl.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .width(140.dp)
                        .height(210.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(book.coverUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = book.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Card(
                    modifier = Modifier
                        .width(140.dp)
                        .height(210.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Información básica
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "por ${book.author}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Rating estrellas (simulado)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < 4) Icons.Default.Star else Icons.Default.StarOutline,
                            contentDescription = null,
                            tint = if (index < 4) Color(0xFFFFC107) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "4.0",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Estado de favorito
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (book.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (book.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (book.isFavorite) "En favoritos" else "Añadir a favoritos",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón de acción principal
                Button(
                    onClick = { /* Alguna acción */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Leer muestra")
                }
            }
        }
    }
}

@Composable
fun BookDetailSection(book: Book) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Detalles",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Detalles en forma de tabla
        BookDetailItem("Editorial", "Open Library")
        BookDetailItem("Fecha de publicación", "2010") // Simulado
        BookDetailItem("Idioma", "Español") // Simulado
        BookDetailItem("ISBN", "9780123456789") // Simulado
        BookDetailItem("Número de páginas", "325") // Simulado
        BookDetailItem("Categorías", "Ficción, Novela") // Simulado
    }
}

@Composable
fun BookDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingStateChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null
    )
}

@Composable
fun BookExternalLinks(
    book: Book,
    onLinkClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Enlaces externos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tarjetas clicables para enlaces externos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val openLibraryUrl = "https://openlibrary.org${book.key}"

            LinkCard(
                icon = Icons.Default.Public,
                title = "Open Library",
                onClick = { onLinkClick(openLibraryUrl) }
            )

            LinkCard(
                icon = Icons.Default.Store,
                title = "Comprar",
                onClick = { onLinkClick("https://www.amazon.com/s?k=${book.title.replace(" ", "+")}+${book.author.replace(" ", "+")}") }
            )

            LinkCard(
                icon = Icons.Default.Info,
                title = "Goodreads",
                onClick = { onLinkClick("https://www.goodreads.com/search?q=${book.title.replace(" ", "+")}") }
            )
        }
    }
}

@Composable
fun LinkCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .height(80.dp)
            .width(80.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun SimilarBooksSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        Text(
            text = "Libros similares",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Lista horizontal de libros recomendados (simulados)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(5) {
                SimilarBookItem()
            }
        }
    }
}

@Composable
fun SimilarBookItem() {
    Column(
        modifier = Modifier.width(100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Portada del libro similar (simulada)
        Card(
            modifier = Modifier
                .size(100.dp, 150.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Libro similar",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Título del libro similar (simulado)
        Text(
            text = "Título del libro",
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}