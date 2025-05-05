package com.example.bookloginapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookloginapp.model.Book
import com.example.bookloginapp.model.ReadingState
import com.example.bookloginapp.model.SearchHistory
import com.example.bookloginapp.repository.BooksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BooksViewModel : ViewModel() {

    private val repository = BooksRepository()

    // Estado para la búsqueda de libros
    private val _searchState = MutableStateFlow<SearchState>(SearchState.Initial)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    // Estado para libros favoritos
    private val _favoriteBooksState = MutableStateFlow<FavoriteBooksState>(FavoriteBooksState.Loading)
    val favoriteBooksState: StateFlow<FavoriteBooksState> = _favoriteBooksState.asStateFlow()

    // Estado para notas de libros
    private val _bookNotes = MutableStateFlow<String>("")
    val bookNotes: StateFlow<String> = _bookNotes.asStateFlow()

    // Estado para la operación de guardar notas
    private val _saveNotesState = MutableStateFlow<SaveNotesState>(SaveNotesState.Initial)
    val saveNotesState: StateFlow<SaveNotesState> = _saveNotesState.asStateFlow()

    // Estado para historial de búsqueda
    private val _searchHistoryState = MutableStateFlow<SearchHistoryState>(SearchHistoryState.Initial)
    val searchHistoryState: StateFlow<SearchHistoryState> = _searchHistoryState.asStateFlow()

    // Estado para el estado de lectura
    private val _readingState = MutableStateFlow<ReadingState>(ReadingState.NotStarted)
    val readingState: StateFlow<ReadingState> = _readingState.asStateFlow()

    /**
     * Buscar libros usando la API de Open Library
     */
    fun searchBooks(query: String) {
        if (query.isBlank()) {
            _searchState.value = SearchState.Initial
            return
        }

        _searchState.value = SearchState.Loading
        viewModelScope.launch {
            try {
                val books = repository.searchBooks(query)
                _searchState.value = if (books.isEmpty()) {
                    SearchState.Empty
                } else {
                    SearchState.Success(books)
                }
            } catch (e: Exception) {
                _searchState.value = SearchState.Error("Error al buscar libros: ${e.message}")
            }
        }
    }

    /**
     * Cargar libros favoritos desde Firebase
     */
    fun loadFavoriteBooks() {
        _favoriteBooksState.value = FavoriteBooksState.Loading
        viewModelScope.launch {
            try {
                val favoriteBooks = repository.getFavoriteBooks()
                _favoriteBooksState.value = if (favoriteBooks.isEmpty()) {
                    FavoriteBooksState.Empty
                } else {
                    FavoriteBooksState.Success(favoriteBooks)
                }
            } catch (e: Exception) {
                _favoriteBooksState.value = FavoriteBooksState.Error("Error al cargar favoritos: ${e.message}")
            }
        }
    }

    /**
     * Cambiar el estado de favorito de un libro
     */
    fun toggleFavorite(book: Book) {
        viewModelScope.launch {
            try {
                val success = repository.toggleFavorite(book)
                if (success) {
                    // Actualizar la lista de búsqueda si está disponible
                    when (val currentState = _searchState.value) {
                        is SearchState.Success -> {
                            val updatedBooks = currentState.books.map {
                                if (it.key == book.key) it.copy(isFavorite = !it.isFavorite) else it
                            }
                            _searchState.value = SearchState.Success(updatedBooks)
                        }
                        else -> {}
                    }

                    // Recargar la lista de favoritos
                    loadFavoriteBooks()
                }
            } catch (e: Exception) {
                // Manejar el error
            }
        }
    }

    /**
     * Cargar las notas de un libro
     */
    fun loadBookNotes(bookKey: String) {
        viewModelScope.launch {
            try {
                val notes = repository.getBookNotes(bookKey)
                _bookNotes.value = notes
            } catch (e: Exception) {
                _bookNotes.value = ""
                // Opcionalmente manejar el error
            }
        }
    }

    /**
     * Guardar las notas de un libro
     */
    fun saveBookNotes(bookKey: String, notes: String) {
        _saveNotesState.value = SaveNotesState.Loading
        viewModelScope.launch {
            try {
                val success = repository.saveBookNotes(bookKey, notes)
                if (success) {
                    _bookNotes.value = notes
                    _saveNotesState.value = SaveNotesState.Success
                } else {
                    _saveNotesState.value = SaveNotesState.Error("No se pudieron guardar las notas")
                }
            } catch (e: Exception) {
                _saveNotesState.value = SaveNotesState.Error("Error al guardar las notas: ${e.message}")
            }
        }
    }

    /**
     * Cargar historial de búsqueda
     */
    fun loadSearchHistory() {
        viewModelScope.launch {
            println("Cargando historial de búsqueda...")
            _searchHistoryState.value = SearchHistoryState.Loading
            try {
                val history = repository.getSearchHistory()
                println("Historial cargado: ${history.size} elementos")
                _searchHistoryState.value = if (history.isEmpty()) {
                    println("Historial vacío")
                    SearchHistoryState.Empty
                } else {
                    println("Historial con datos")
                    SearchHistoryState.Success(history)
                }
            } catch (e: Exception) {
                println("Error al cargar historial: ${e.message}")
                _searchHistoryState.value = SearchHistoryState.Error("Error al cargar el historial: ${e.message}")
            }
        }
    }

    fun saveSearchQuery(query: String) {
        viewModelScope.launch {
            println("Guardando consulta: $query")
            try {
                val success = repository.saveSearchQuery(query)
                if (success) {
                    println("Consulta guardada exitosamente")
                    // Recargar el historial después de guardar
                    loadSearchHistory()
                } else {
                    println("No se pudo guardar la consulta")
                }
            } catch (e: Exception) {
                println("Error al guardar consulta: ${e.message}")
            }
        }
    }
    /**
     * Eliminar un elemento del historial de búsqueda
     */
    fun deleteSearchHistoryItem(searchId: String) {
        viewModelScope.launch {
            try {
                val success = repository.deleteSearchHistory(searchId)
                if (success) {
                    loadSearchHistory()
                }
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    /**
     * Borrar todo el historial de búsqueda
     */
    fun clearSearchHistory() {
        viewModelScope.launch {
            try {
                val success = repository.clearSearchHistory()
                if (success) {
                    _searchHistoryState.value = SearchHistoryState.Empty
                }
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    /**
     * Cargar el estado de lectura de un libro
     */
    fun loadReadingState(bookKey: String) {
        viewModelScope.launch {
            try {
                val state = repository.getReadingState(bookKey)
                _readingState.value = state ?: ReadingState.NotStarted
            } catch (e: Exception) {
                _readingState.value = ReadingState.NotStarted
            }
        }
    }

    /**
     * Actualizar el estado de lectura de un libro
     */
    fun updateReadingState(bookKey: String, state: ReadingState) {
        viewModelScope.launch {
            try {
                val success = repository.saveReadingState(bookKey, state)
                if (success) {
                    _readingState.value = state
                }
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
}

/**
 * Estados posibles para la búsqueda de libros
 */
sealed class SearchState {
    object Initial : SearchState()
    object Loading : SearchState()
    object Empty : SearchState()
    data class Success(val books: List<Book>) : SearchState()
    data class Error(val message: String) : SearchState()
}

/**
 * Estados posibles para la carga de libros favoritos
 */
sealed class FavoriteBooksState {
    object Loading : FavoriteBooksState()
    object Empty : FavoriteBooksState()
    data class Success(val books: List<Book>) : FavoriteBooksState()
    data class Error(val message: String) : FavoriteBooksState()
}

/**
 * Estados posibles para la operación de guardar notas
 */
sealed class SaveNotesState {
    object Initial : SaveNotesState()
    object Loading : SaveNotesState()
    object Success : SaveNotesState()
    data class Error(val message: String) : SaveNotesState()
}

/**
 * Estados posibles para el historial de búsqueda
 */
sealed class SearchHistoryState {
    object Initial : SearchHistoryState()
    object Loading : SearchHistoryState()
    object Empty : SearchHistoryState()
    data class Success(val history: List<SearchHistory>) : SearchHistoryState()
    data class Error(val message: String) : SearchHistoryState()
}