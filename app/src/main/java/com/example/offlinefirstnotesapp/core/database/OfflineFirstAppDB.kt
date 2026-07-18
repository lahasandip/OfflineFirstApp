package  com.example.offlinefirstnotesapp.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import  com.example.offlinefirstnotesapp.features.notes.data.local.dao.NotesDao
import  com.example.offlinefirstnotesapp.features.notes.data.local.entity.NotesEntity

@Database(
    entities = [NotesEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class OfflineFirstNotesAppDB : RoomDatabase() {
    abstract fun notesDao(): NotesDao

    companion object {
        @Volatile
        private var instance: OfflineFirstNotesAppDB? = null

        fun getInstance(context: Context): OfflineFirstNotesAppDB {
            return instance ?: synchronized(this) {
                instance ?: createDatabase(context).also { instance = it }
            }
        }

        private fun createDatabase(context: Context): OfflineFirstNotesAppDB {
            return Room.databaseBuilder(
                context,
                OfflineFirstNotesAppDB::class.java,
                "notes_app"
            )
            .build()
        }
    }
}
