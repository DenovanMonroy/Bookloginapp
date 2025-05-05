package com.example.bookloginapp.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.bookloginapp.model.Book

class SharedBookViewModel : ViewModel() {
    private val _selectedBook = mutableStateOf<Book?>(null)
    val selectedBook: State<Book?> = _selectedBook

    fun setSelectedBook(book: Book) {
        _selectedBook.value = book
    }
}