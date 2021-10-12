package de.colognecode.musicorganizer.repository.database.entities

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.colognecode.musicorganizer.repository.network.model.TrackItem

class DataConverter {

    @TypeConverter
    fun fromTrackItemList(value: List<TrackItem>): String {
        val gson = Gson()
        val type = object : TypeToken<List<TrackItem>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toTrackItemList(value: String): List<TrackItem> {
        val gson = Gson()
        val type = object : TypeToken<List<TrackItem>>() {}.type
        return gson.fromJson(value, type)
    }
}