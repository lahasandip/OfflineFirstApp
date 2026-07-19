package com.example.offlinefirstnotesapp.features.notes.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.offlinefirstnotesapp.features.notes.data.local.entity.NotesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {

    @Upsert
    suspend fun upsertNote(note: NotesEntity)

    @Upsert
    suspend fun upsertNotes(notes: List<NotesEntity>)

    @Delete
    suspend fun deleteNote(note: NotesEntity)

    @Query("SELECT * from notes WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun getNotes(): Flow<List<NotesEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteByIdSync(id: String): NotesEntity?

    @Query("SELECT * FROM notes WHERE isSynced = 0")
    suspend fun getUnsyncedNotes(): List<NotesEntity>

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun hardDeleteNote(id: String)

    @Query("DELETE FROM notes WHERE id IN (:ids)")
    suspend fun hardDeleteNotes(ids: List<String>)

    @Query("UPDATE notes SET isSynced = 1 WHERE id = :id AND updatedAt = :updatedAt")
    suspend fun markAsSynced(id: String, updatedAt: Long)
    
    @Query("UPDATE notes SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSyncedBulk(ids: List<String>)
}
