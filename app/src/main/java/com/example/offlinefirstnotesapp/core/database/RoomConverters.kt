package  com.example.offlinefirstnotesapp.core.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RoomConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromMap(value: Map<String, Long>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toMap(value: String): Map<String, Long> {
        val mapType = object : TypeToken<Map<String, Long>>() {}.type
        return gson.fromJson(value, mapType)
    }
}
