package  com.example.offlinefirstnotesapp.features.notes.data.repository

import  com.example.offlinefirstnotesapp.features.notes.data.mapper.toDto
import  com.example.offlinefirstnotesapp.features.notes.data.mapper.toNote
import  com.example.offlinefirstnotesapp.features.notes.data.remote.api.NotesApi
import  com.example.offlinefirstnotesapp.features.notes.domain.model.Note
import  com.example.offlinefirstnotesapp.features.notes.domain.repository.NotesRemoteRepository

class NotesRemoteRepositoryImpl(
    private val notesApi: NotesApi
) : NotesRemoteRepository {

    override suspend fun getNotes(since: Long?): List<Note> {
        val filter = since?.let { "gt.$it" }
        return notesApi.getNotes(updatedAt = filter).map { it.toNote() }
    }

    override suspend fun syncNotes(notes: List<Note>): List<Note> {
        if (notes.isEmpty()) return emptyList()
        return notesApi.upsertNotes(notes = notes.map { it.toDto() }).map { it.toNote() }
    }
}