package  com.example.offlinefirstnotesapp.features.notes.domain.repository

import  com.example.offlinefirstnotesapp.features.notes.domain.model.Note

interface NotesRemoteRepository {

    suspend fun getNotes(since: Long? = null): List<Note>

    suspend fun syncNotes(notes: List<Note>): List<Note>

}