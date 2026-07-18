package  com.example.offlinefirstnotesapp.features.notes.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.offlinefirstnotesapp.features.notes.data.local.entity.NotesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNote(note: NotesEntity): Long

    @Update
    suspend fun updateNote(note: NotesEntity)

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

    @Query("UPDATE notes SET isSynced = 1 WHERE id = :id AND updatedAt = :updatedAt")
    suspend fun markAsSynced(id: String, updatedAt: Long)
}
