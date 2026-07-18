package com.example.offlinefirstnotesapp.features.notes.domain.model

import java.util.UUID

data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String = "",
    val titleUpdatedAt: Long = System.currentTimeMillis(),
    val contentUpdatedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val userId: String = "default_user"
)
