package com.example.offlinefirstnotesapp.features.notes.data.repository

import com.example.offlinefirstnotesapp.features.notes.data.local.dao.NotesDao
import com.example.offlinefirstnotesapp.features.notes.data.mapper.toEntity
import com.example.offlinefirstnotesapp.features.notes.data.mapper.toNote
import com.example.offlinefirstnotesapp.features.notes.domain.model.Note
import com.example.offlinefirstnotesapp.features.notes.domain.repository.NotesLocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotesLocalRepositoryImpl(
    private val notesDao: NotesDao,
) : NotesLocalRepository {
    override fun getNotes(): Flow<List<Note>> {
        return notesDao.getNotes().map { entities ->
            entities.map { it.toNote() }
        }
    }

    override suspend fun addNote(note: Note) {
        notesDao.upsertNote(note.toEntity())
    }

    override suspend fun addNotes(notes: List<Note>) {
        notesDao.upsertNotes(notes.map { it.toEntity() })
    }

    override suspend fun deleteNote(note: Note) {
        notesDao.deleteNote(note.toEntity())
    }

    override suspend fun updateNote(note: Note) {
        notesDao.upsertNote(note.toEntity())
    }

    override suspend fun getUnsyncedNotes(): List<Note> {
        return notesDao.getUnsyncedNotes().map { it.toNote() }
    }

    override suspend fun getNoteById(id: String): Note? {
        return notesDao.getNoteByIdSync(id)?.toNote()
    }

    override suspend fun hardDeleteNote(id: String) {
        notesDao.hardDeleteNote(id)
    }

    override suspend fun hardDeleteNotes(ids: List<String>) {
        notesDao.hardDeleteNotes(ids)
    }

    override suspend fun markAsSynced(id: String, updatedAt: Long) {
        notesDao.markAsSynced(id, updatedAt)
    }

    override suspend fun markAsSyncedBulk(ids: List<String>) {
        notesDao.markAsSyncedBulk(ids)
    }
}
