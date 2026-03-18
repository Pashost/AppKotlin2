package com.example.kotlinapp_2.ui.notes

import com.example.kotlinapp_2.data.local.Note

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
)
