package com.example.offlinefirstnotesapp.features.notes.domain.usecase

import android.util.Log
import com.example.offlinefirstnotesapp.core.utils.SyncScheduler
import com.example.offlinefirstnotesapp.features.notes.domain.model.Note
import com.example.offlinefirstnotesapp.features.notes.domain.repository.NotesLocalRepository
import com.example.offlinefirstnotesapp.features.notes.domain.repository.NotesRemoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class NotesUseCase(
    private val localRepository: NotesLocalRepository, // Interface for local data operations
    private val remoteRepository: NotesRemoteRepository, // Interface for remote API operations
    private val syncScheduler: SyncScheduler // Schedules background sync tasks
) {

    fun getNotes(): Flow<List<Note>> = localRepository.getNotes()

    suspend fun addNote(title: String, content: String = "") {
        val now = System.currentTimeMillis()
        val note = Note(
            title = title.trim(),
            content = content.trim(),
            titleUpdatedAt = now,
            contentUpdatedAt = now,
            updatedAt = now,
            isSynced = false,
            isDeleted = false
        )
        localRepository.addNote(note)
        syncScheduler.scheduleOneTimeSync() // Schedule sync immediately
        sync()
    }

    suspend fun updateNote(note: Note) {
        val local = localRepository.getNoteById(note.id) ?: return
        val now = System.currentTimeMillis()
        
        val updatedNote = note.copy(
            title = note.title.trim(),
            content = note.content.trim(),
            titleUpdatedAt = if (local.title != note.title.trim()) now else local.titleUpdatedAt,
            contentUpdatedAt = if (local.content != note.content.trim()) now else local.contentUpdatedAt,
            updatedAt = now,
            isSynced = false
        )
        
        localRepository.updateNote(updatedNote)
        syncScheduler.scheduleOneTimeSync() // Schedule sync immediately
        sync()
    }

    suspend fun deleteNote(note: Note) {
        val deletedNote = note.copy(
            isDeleted = true,
            updatedAt = System.currentTimeMillis(),
            isSynced = false
        )
        localRepository.updateNote(deletedNote)
        syncScheduler.scheduleOneTimeSync() // Schedule sync immediately
        sync()
    }

    suspend fun sync() {
        Log.d("Sync", "Sync started...")

        try {
            val remoteNotes = remoteRepository.getNotes(since = null)
            val remoteIds = remoteNotes.map { it.id }.toSet()

            remoteNotes.forEach { remote ->
                val local = localRepository.getNoteById(remote.id)
                
                when {
                    remote.isDeleted -> {
                        if (local == null || local.isSynced || remote.updatedAt >= local.updatedAt) {
                            localRepository.hardDeleteNote(remote.id)
                        }
                    }
                    local == null -> {
                        localRepository.addNote(remote.copy(isSynced = true))
                    }
                    else -> {
                        var mergedNote = local.copy()
                        var hasRemoteChanges = false

                        if (remote.titleUpdatedAt > local.titleUpdatedAt) {
                            mergedNote = mergedNote.copy(
                                title = remote.title,
                                titleUpdatedAt = remote.titleUpdatedAt
                            )
                            hasRemoteChanges = true
                        }
                        if (remote.contentUpdatedAt > local.contentUpdatedAt) {
                            mergedNote = mergedNote.copy(
                                content = remote.content,
                                contentUpdatedAt = remote.contentUpdatedAt
                            )
                            hasRemoteChanges = true
                        }

                        if (hasRemoteChanges || local.isSynced) {
                            localRepository.updateNote(mergedNote.copy(
                                isSynced = local.isSynced,
                                updatedAt = maxOf(remote.updatedAt, local.updatedAt)
                            ))
                        }
                    }
                }
            }

            val localNotes = localRepository.getNotes().first()
            localNotes.filter { it.isSynced && !it.isDeleted }.forEach { localNote ->
                if (!remoteIds.contains(localNote.id)) {
                    Log.d("Sync", "Cleaning up orphaned local note: ${localNote.id}")
                    localRepository.hardDeleteNote(localNote.id)
                }
            }
            
            Log.d("Sync", "Pull/Merge successful")
        } catch (e: Exception) {
            Log.e("Sync", "Pull/Merge failed: ${e.message}")
        }

        val unsynced = localRepository.getUnsyncedNotes()
        if (unsynced.isNotEmpty()) {
            try {
                remoteRepository.syncNotes(unsynced)
                unsynced.forEach { localRepository.markAsSynced(it.id, it.updatedAt) }
            } catch (e: Exception) {
                Log.e("Sync", "Push failed: ${e.message}")
            }
        }
        Log.d("Sync", "Sync finished")
    }
}
