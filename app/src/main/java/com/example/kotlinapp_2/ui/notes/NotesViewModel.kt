package com.example.kotlinapp_2.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinapp_2.data.local.Note
import com.example.kotlinapp_2.data.repository.NoteRepository
import com.example.kotlinapp_2.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState(isLoading = true))
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<NotesEvent>()
    val events: SharedFlow<NotesEvent> = _events.asSharedFlow()

    private val searchQuery = MutableStateFlow("")
    private var currentSourceNotes: List<Note> = emptyList()

    private var lastDeletedNote: Note? = null

    init {
        observeNotes()
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun onDeleteNote(note: Note) {
        viewModelScope.launch {
            try {
                lastDeletedNote = note
                noteRepository.delete(note)
                _events.emit(NotesEvent.ShowUndoDelete(message = "Note deleted"))
            } catch (_: Exception) {
                _events.emit(NotesEvent.ShowMessage("Failed to delete note"))
            }
        }
    }

    fun onNoteCheckedChanged(note: Note, isChecked: Boolean) {
        if (note.isChecked == isChecked) return

        viewModelScope.launch {
            try {
                noteRepository.update(note.copy(isChecked = isChecked))
            } catch (_: Exception) {
                _events.emit(NotesEvent.ShowMessage("Failed to update note"))
            }
        }
    }

    fun onSortOptionChanged(sortOption: NoteSortOption) {
        _uiState.value = _uiState.value.copy(
            sortOption = sortOption,
            notes = sortNotes(currentSourceNotes, sortOption),
        )
    }

    fun onUndoDelete() {
        viewModelScope.launch {
            lastDeletedNote?.let { note ->
                noteRepository.insert(note.copy(id = 0))
                lastDeletedNote = null
            }
        }
    }

    private fun observeNotes() {
        searchQuery
            .debounce(Constants.SEARCH_DEBOUNCE_MS)
            .distinctUntilChanged()
            .onEach { query ->
                _uiState.value = _uiState.value.copy(
                    searchQuery = query,
                    isLoading = true,
                )
            }
            .flatMapLatest { query ->
                noteRepository.getAllNotes(query)
            }
            .onEach { notes ->
                currentSourceNotes = notes
                _uiState.value = _uiState.value.copy(
                    notes = sortNotes(notes, _uiState.value.sortOption),
                    isLoading = false,
                )
            }
            .catch {
                _events.emit(NotesEvent.ShowMessage("Failed to load notes"))
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
            .launchIn(viewModelScope)
    }

    private fun sortNotes(notes: List<Note>, sortOption: NoteSortOption): List<Note> {
        val activeNotes = notes.filterNot { it.isChecked }
        val checkedNotes = notes.filter { it.isChecked }

        return sortByOption(activeNotes, sortOption) + sortByOption(checkedNotes, sortOption)
    }

    private fun sortByOption(notes: List<Note>, sortOption: NoteSortOption): List<Note> {
        return when (sortOption) {
            NoteSortOption.NEWEST -> notes.sortedByDescending { it.timestamp }
            NoteSortOption.OLDEST -> notes.sortedBy { it.timestamp }
            NoteSortOption.TITLE -> notes.sortedBy { it.title.lowercase() }
        }
    }
}
