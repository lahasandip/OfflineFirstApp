package  com.example.offlinefirstnotesapp.features.notes.domain.repository

import  com.example.offlinefirstnotesapp.features.notes.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NotesLocalRepository {

    fun getNotes(): Flow<List<Note>>

    suspend fun addNote(note: Note)

    suspend fun deleteNote(note: Note)

    suspend fun updateNote(note: Note)

    suspend fun getUnsyncedNotes(): List<Note>

    suspend fun getNoteById(id: String): Note?

    suspend fun hardDeleteNote(id: String)

    suspend fun markAsSynced(id: String, updatedAt: Long)
}