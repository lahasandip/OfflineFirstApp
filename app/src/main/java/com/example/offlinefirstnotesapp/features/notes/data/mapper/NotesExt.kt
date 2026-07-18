package  com.example.offlinefirstnotesapp.features.notes.data.mapper

import  com.example.offlinefirstnotesapp.features.notes.data.local.entity.NotesEntity
import  com.example.offlinefirstnotesapp.features.notes.data.remote.dto.NotesDto
import  com.example.offlinefirstnotesapp.features.notes.domain.model.Note

fun Note.toEntity(): NotesEntity = NotesEntity(
    id = id,
    title = title,
    content = content,
    titleUpdatedAt = titleUpdatedAt,
    contentUpdatedAt = contentUpdatedAt,
    updatedAt = updatedAt,
    isSynced = isSynced,
    isDeleted = isDeleted,
    userId = userId
)

fun NotesEntity.toNote(): Note = Note(
    id = id,
    title = title,
    content = content,
    titleUpdatedAt = titleUpdatedAt,
    contentUpdatedAt = contentUpdatedAt,
    updatedAt = updatedAt,
    isSynced = isSynced,
    isDeleted = isDeleted,
    userId = userId
)

fun NotesDto.toNote(): Note = Note(
    id = id ?: "",
    title = title,
    content = content,
    titleUpdatedAt = titleUpdatedAt,
    contentUpdatedAt = contentUpdatedAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    userId = userId,
    isSynced = true
)

fun Note.toDto(): NotesDto = NotesDto(
    id = id,
    title = title,
    content = content,
    titleUpdatedAt = titleUpdatedAt,
    contentUpdatedAt = contentUpdatedAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    userId = userId
)

fun List<NotesDto>.toNotes(): List<Note> = this.map { it.toNote() }
