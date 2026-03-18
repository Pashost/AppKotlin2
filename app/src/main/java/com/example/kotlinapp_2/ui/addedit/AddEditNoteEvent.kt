package com.example.kotlinapp_2.ui.addedit

sealed class AddEditNoteEvent {
    data class ShowMessage(val message: String) : AddEditNoteEvent()
    data object NavigateBack : AddEditNoteEvent()
}
