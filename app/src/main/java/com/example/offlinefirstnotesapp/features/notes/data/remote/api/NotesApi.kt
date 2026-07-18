package  com.example.offlinefirstnotesapp.features.notes.data.remote.api

import  com.example.offlinefirstnotesapp.features.notes.data.remote.dto.NotesDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface NotesApi {

    @GET("notes")
    suspend fun getNotes(
        @Query("updated_at") updatedAt: String? = null,
        @Query("select") select: String = "*"
    ): List<NotesDto>

    @POST("notes")
    suspend fun upsertNotes(
        @Header("Prefer") prefer: String = "resolution=merge-duplicates,return=representation",
        @Body notes: List<NotesDto>
    ): List<NotesDto>
}