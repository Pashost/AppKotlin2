package com.example.kotlinapp_2.data.repository

import com.example.kotlinapp_2.data.local.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(query: String): Flow<List<Note>>
    suspend fun getNoteById(id: Int): Note?
    suspend fun insert(note: Note)
    suspend fun update(note: Note)
    suspend fun delete(note: Note)
}
