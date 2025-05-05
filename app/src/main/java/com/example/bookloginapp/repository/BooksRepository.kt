package com.example.bookloginapp.repository

import com.example.bookloginapp.model.Book
import com.example.bookloginapp.model.BookDoc
import com.example.bookloginapp.network.NetworkModule
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.Date
import com.example.bookloginapp.model.SearchHistory
import com.example.bookloginapp.model.ReadingState

class BooksRepository {

    private val openLibraryService = NetworkModule.openLibraryService
    private val database = Firebase.database.reference
    private val auth = Firebase.auth

    suspend fun searchBooks(query: String): List<Book> {
        val response = openLibraryService.searchBooks(query)
        if (!response.isSuccessful || response.body() == null) {
            return emptyList()
        }

        val bookDocs = response.body()!!.docs
        val favoriteBooks = getFavoriteBooks()

        return bookDocs.map { it.toBook(favoriteBooks) }
    }

    suspend fun getFavoriteBooks(): List<Book> {
        val currentUser = auth.currentUser ?: return emptyList()

        return try {
            val snapshot = database
                .child("users")
                .child(currentUser.uid)
                .child("favorites")
                .get()
                .await()

            if (snapshot.exists()) {
                val books = mutableListOf<Book>()
                for (bookSnapshot in snapshot.children) {
                    // Extraer valores directamente de cada nodo hijo
                    val id = bookSnapshot.child("id").getValue(String::class.java) ?: ""
                    val key = bookSnapshot.child("key").getValue(String::class.java) ?: ""
                    val title = bookSnapshot.child("title").getValue(String::class.java) ?: "Sin título"
                    val author = bookSnapshot.child("author").getValue(String::class.java) ?: "Desconocido"
                    val coverUrl = bookSnapshot.child("coverUrl").getValue(String::class.java) ?: ""
                    val description = bookSnapshot.child("description").getValue(String::class.java) ?: ""

                    // Crear el objeto Book con los valores extraídos y añadirlo a la lista
                    val book = Book(
                        id = id,
                        key = key,
                        title = title,
                        author = author,
                        coverUrl = coverUrl,
                        description = description,
                        isFavorite = true
                    )
                    books.add(book)
                }
                books
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("Error al obtener favoritos: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
    suspend fun toggleFavorite(book: Book): Boolean {
        val currentUser = auth.currentUser ?: return false
        val bookKey = book.key.replace("/", "_") // Asegurar que la clave no contenga caracteres prohibidos

        val favoritesRef = database
            .child("users")
            .child(currentUser.uid)
            .child("favorites")
            .child(bookKey)

        return try {
            if (book.isFavorite) {
                // Eliminar de favoritos - SOLO el libro específico
                favoritesRef.removeValue().await()
            } else {
                // Añadir a favoritos con toda la información necesaria
                val bookMap = mapOf(
                    "id" to book.id,
                    "key" to book.key,
                    "title" to book.title,
                    "author" to book.author,
                    "coverUrl" to book.coverUrl,
                    "description" to book.description,
                    "isFavorite" to true,
                    "addedAt" to Date().time
                )

                favoritesRef.setValue(bookMap).await()
            }
            true
        } catch (e: Exception) {
            println("Error al modificar favorito: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    private fun BookDoc.toBook(favoriteBooks: List<Book>): Book {
        val isFav = favoriteBooks.any { it.key == this.key }
        return Book(
            key = this.key,
            title = this.title,
            author = this.authorNames?.joinToString(", ") ?: "Autor desconocido",
            coverUrl = this.coverId?.let { "https://covers.openlibrary.org/b/id/$it-L.jpg" } ?: "",
            description = this.firstSentence?.firstOrNull() ?: "Sin descripción disponible",
            isFavorite = isFav
        )
    }
    suspend fun saveBookNotes(bookKey: String, notes: String): Boolean {
        val currentUser = auth.currentUser ?: return false

        return try {
            database
                .child("users")
                .child(currentUser.uid)
                .child("book_notes")
                .child(bookKey)
                .setValue(notes)
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getBookNotes(bookKey: String): String {
        val currentUser = auth.currentUser ?: return ""

        return try {
            val snapshot = database
                .child("users")
                .child(currentUser.uid)
                .child("book_notes")
                .child(bookKey)
                .get()
                .await()

            snapshot.getValue(String::class.java) ?: ""
        } catch (e: Exception) {
            ""
        }
    }
    // Añadir estos métodos a tu BooksRepository existente

    /**
     * Guardar una consulta de búsqueda en el historial
     */
    suspend fun saveSearchQuery(query: String): Boolean {
        if (query.isBlank()) return false
        val currentUser = auth.currentUser ?: return false

        return try {
            // Generar una referencia con clave única
            val historyRef = database
                .child("users")
                .child(currentUser.uid)
                .child("search_history")
                .push()

            val searchId = historyRef.key ?: return false
            val timestamp = System.currentTimeMillis()

            // Crear mapa de datos
            val searchData = mapOf(
                "query" to query,
                "timestamp" to timestamp,
                "id" to searchId
            )

            // Guardar en Firebase
            historyRef.setValue(searchData).await()

            true
        } catch (e: Exception) {
            println("Error al guardar búsqueda: ${e.message}")
            false
        }
    }

    /**
     * Obtener el historial de búsqueda del usuario
     */
    suspend fun getSearchHistory(): List<SearchHistory> {
        val currentUser = auth.currentUser ?: return emptyList()

        return try {
            val snapshot = database
                .child("users")
                .child(currentUser.uid)
                .child("search_history")
                .get()
                .await()

            val result = mutableListOf<SearchHistory>()

            if (snapshot.exists()) {
                for (childSnapshot in snapshot.children) {
                    // Convertir el valor a un Map
                    val map = childSnapshot.value as? Map<*, *>
                    if (map != null) {
                        val query = map["query"] as? String ?: ""
                        val timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0L
                        val id = map["id"] as? String ?: childSnapshot.key ?: ""

                        if (query.isNotEmpty()) {
                            result.add(SearchHistory(query, timestamp, id))
                        }
                    }
                }
            }

            // ¡IMPORTANTE! Devolver la lista ordenada
            result.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            println("Error al obtener historial: ${e.message}")
            emptyList()
        }
    }
    suspend fun deleteSearchHistory(searchId: String): Boolean {
        val currentUser = auth.currentUser ?: return false

        return try {
            database
                .child("users")
                .child(currentUser.uid)
                .child("search_history")
                .child(searchId)
                .removeValue()
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun clearSearchHistory(): Boolean {
        val currentUser = auth.currentUser ?: return false

        return try {
            database
                .child("users")
                .child(currentUser.uid)
                .child("search_history")
                .removeValue()
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }
    /**
     * Guardar el estado de lectura de un libro
     */
    suspend fun saveReadingState(bookKey: String, state: ReadingState): Boolean {
        val currentUser = auth.currentUser ?: return false
        val safeBookKey = bookKey.replace("/", "_")

        return try {
            database
                .child("users")
                .child(currentUser.uid)
                .child("reading_states")
                .child(safeBookKey)
                .setValue(state.name)
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtener el estado de lectura de un libro
     */
    suspend fun getReadingState(bookKey: String): ReadingState? {
        val currentUser = auth.currentUser ?: return null
        val safeBookKey = bookKey.replace("/", "_")

        return try {
            val snapshot = database
                .child("users")
                .child(currentUser.uid)
                .child("reading_states")
                .child(safeBookKey)
                .get()
                .await()

            val stateString = snapshot.getValue(String::class.java)
            stateString?.let { ReadingState.valueOf(it) } ?: ReadingState.NotStarted
        } catch (e: Exception) {
            ReadingState.NotStarted
        }
    }


}
