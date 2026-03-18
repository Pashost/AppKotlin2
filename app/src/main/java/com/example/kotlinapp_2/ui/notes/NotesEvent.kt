package com.example.kotlinapp_2.ui.notes

sealed class NotesEvent {
    data class ShowMessage(val message: String) : NotesEvent()
    data class ShowUndoDelete(val message: String) : NotesEvent()
}
