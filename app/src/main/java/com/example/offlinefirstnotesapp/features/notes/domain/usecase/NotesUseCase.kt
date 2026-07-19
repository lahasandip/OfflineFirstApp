package com.example.offlinefirstnotesapp.features.notes.domain.usecase

import android.util.Log
import com.example.offlinefirstnotesapp.features.notes.data.worker.SyncScheduler
import com.example.offlinefirstnotesapp.features.notes.domain.model.Note
import com.example.offlinefirstnotesapp.features.notes.domain.repository.NotesLocalRepository
import com.example.offlinefirstnotesapp.features.notes.domain.repository.NotesRemoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class NotesUseCase(
    private val localRepository: NotesLocalRepository,
    private val remoteRepository: NotesRemoteRepository,
    private val syncScheduler: SyncScheduler
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
        syncScheduler.scheduleOneTimeSync()
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
        syncScheduler.scheduleOneTimeSync()
    }

    suspend fun deleteNote(note: Note) {
        val deletedNote = note.copy(
            isDeleted = true,
            updatedAt = System.currentTimeMillis(),
            isSynced = false
        )
        localRepository.updateNote(deletedNote)
        syncScheduler.scheduleOneTimeSync()
    }

    /**
     * Core synchronization logic: Pulls remote changes, merges them, and then pushes local changes.
     */
    suspend fun sync() {
        Log.d("Sync", "Sync started...")
        pullAndMerge()
        pushLocalChanges()
        Log.d("Sync", "Sync finished")
    }

    /**
     * Fetches remote notes and handles additions, updates, and deletions in the local database.
     */
    private suspend fun pullAndMerge() {
        try {
            val remoteNotes = remoteRepository.getNotes(since = null)
            
            // 1. Handle remote Additions and Updates
            remoteNotes.filter { !it.isDeleted }.forEach { remote ->
                reconcileWithLocal(remote)
            }

            // 2. Unified Deletion: Handles both soft-deleted and physically removed notes from server
            syncDeletions(remoteNotes)
            
            Log.d("Sync", "Pull/Merge successful")
        } catch (e: Exception) {
            Log.e("Sync", "Pull/Merge failed: ${e.message}")
        }
    }

    /**
     * Reconciles an active remote note with its local counterpart using field-level timestamps.
     */
    private suspend fun reconcileWithLocal(remote: Note) {
        val local = localRepository.getNoteById(remote.id)

        if (local == null) {
            localRepository.addNote(remote.copy(isSynced = true))
        } else {
            var mergedNote = local.copy()
            var hasRemoteChanges = false

            if (remote.titleUpdatedAt > local.titleUpdatedAt) {
                mergedNote = mergedNote.copy(title = remote.title, titleUpdatedAt = remote.titleUpdatedAt)
                hasRemoteChanges = true
            }
            if (remote.contentUpdatedAt > local.contentUpdatedAt) {
                mergedNote = mergedNote.copy(content = remote.content, contentUpdatedAt = remote.contentUpdatedAt)
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

    /**
     * Removes local notes that were either marked as deleted on the server or physically removed.
     */
    private suspend fun syncDeletions(remoteNotes: List<Note>) {
        val remoteMap = remoteNotes.associateBy { it.id }
        val localNotes = localRepository.getNotes().first()

        val idsToDelete = localNotes.filter { local ->
            if (!local.isSynced) return@filter false
            val remote = remoteMap[local.id]
            // Delete if missing from server OR marked deleted on server (and remote is not older than local)
            remote == null || (remote.isDeleted && (remote.updatedAt >= local.updatedAt || local.isDeleted))
        }.map { it.id }

        if (idsToDelete.isNotEmpty()) {
            Log.d("Sync", "Bulk hard deleting local notes: ${idsToDelete.size}")
            localRepository.hardDeleteNotes(idsToDelete)
        }
    }

    /**
     * Pushes all locally modified notes to the remote server.
     */
    private suspend fun pushLocalChanges() {
        val unsynced = localRepository.getUnsyncedNotes()
        if (unsynced.isNotEmpty()) {
            try {
                remoteRepository.syncNotes(unsynced)
                // Optimized: Bulk mark as synced
                localRepository.markAsSyncedBulk(unsynced.map { it.id })
                Log.d("Sync", "Push successful")
            } catch (e: Exception) {
                Log.e("Sync", "Push failed: ${e.message}")
            }
        }
    }
}
