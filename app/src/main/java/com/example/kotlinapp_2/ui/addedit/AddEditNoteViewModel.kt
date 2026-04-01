package com.example.kotlinapp_2.ui.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinapp_2.data.local.Note
import com.example.kotlinapp_2.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditNoteUiState(isLoading = true))
    val uiState: StateFlow<AddEditNoteUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddEditNoteEvent>()
    val events: SharedFlow<AddEditNoteEvent> = _events.asSharedFlow()

    private val noteId: Int = savedStateHandle["noteId"] ?: -1
    private var currentIsChecked: Boolean = false

    init {
        loadNoteIfNeeded()
    }

    fun onSaveClicked(title: String, content: String) {
        if (title.isBlank() || content.isBlank()) {
            viewModelScope.launch {
                _events.emit(AddEditNoteEvent.ShowMessage("Title and content cannot be empty"))
            }
            return
        }

        viewModelScope.launch {
            try {
                if (_uiState.value.isEditMode) {
                    noteRepository.update(
                        Note(
                            id = noteId,
                            title = title.trim(),
                            content = content.trim(),
                            timestamp = System.currentTimeMillis(),
                            isChecked = currentIsChecked,
                        )
                    )
                } else {
                    noteRepository.insert(
                        Note(
                            title = title.trim(),
                            content = content.trim(),
                            timestamp = System.currentTimeMillis(),
                        )
                    )
                }
                _events.emit(AddEditNoteEvent.ShowMessage("Note saved"))
                _events.emit(AddEditNoteEvent.NavigateBack)
            } catch (_: Exception) {
                _events.emit(AddEditNoteEvent.ShowMessage("Failed to save note"))
            }
        }
    }

    private fun loadNoteIfNeeded() {
        if (noteId == -1) {
            _uiState.value = AddEditNoteUiState(isLoading = false)
            return
        }

        viewModelScope.launch {
            try {
                val note = noteRepository.getNoteById(noteId)
                if (note != null) {
                    currentIsChecked = note.isChecked
                    _uiState.value = AddEditNoteUiState(
                        noteId = note.id,
                        isEditMode = true,
                        title = note.title,
                        content = note.content,
                        isLoading = false,
                    )
                } else {
                    _uiState.value = AddEditNoteUiState(isLoading = false)
                    _events.emit(AddEditNoteEvent.ShowMessage("Note not found"))
                }
            } catch (_: Exception) {
                _uiState.value = AddEditNoteUiState(isLoading = false)
                _events.emit(AddEditNoteEvent.ShowMessage("Failed to load note"))
            }
        }
    }
}
