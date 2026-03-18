package com.example.kotlinapp_2.ui.addedit

data class AddEditNoteUiState(
    val noteId: Int = -1,
    val isEditMode: Boolean = false,
    val title: String = "",
    val content: String = "",
    val isLoading: Boolean = false,
)
