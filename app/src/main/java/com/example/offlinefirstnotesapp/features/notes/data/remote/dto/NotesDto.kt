package  com.example.offlinefirstnotesapp.features.notes.data.remote.dto

import com.google.gson.annotations.SerializedName

data class NotesDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("title_updated_at") val titleUpdatedAt: Long,
    @SerializedName("content_updated_at") val contentUpdatedAt: Long,
    @SerializedName("updated_at") val updatedAt: Long,
    @SerializedName("is_deleted") val isDeleted: Boolean,
    @SerializedName("user_id") val userId: String
)
