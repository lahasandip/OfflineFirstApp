package com.example.offlinefirstnotesapp.features.notes.data.local.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Room entity representing a note in the local database.
 */
@Parcelize
@Entity(
    tableName = "notes",
    indices = [
        Index(value = ["updatedAt"]),
        Index(value = ["isSynced"]),
        Index(value = ["isDeleted"])
    ]
)
data class NotesEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "content") val content: String = "",
    @ColumnInfo(name = "titleUpdatedAt") val titleUpdatedAt: Long,
    @ColumnInfo(name = "contentUpdatedAt") val contentUpdatedAt: Long,
    @ColumnInfo(name = "updatedAt") val updatedAt: Long,
    @ColumnInfo(name = "isSynced") val isSynced: Boolean = false,
    @ColumnInfo(name = "isDeleted") val isDeleted: Boolean = false,
    @ColumnInfo(name = "userId") val userId: String = "default_user"
): Parcelable
