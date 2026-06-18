package com.example.daveai.data.db

import androidx.room.TypeConverter

class DataConverters {

    @TypeConverter
    fun fromTimestamp(value: Long?): java.util.Date? {
        return value?.let { java.util.Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: java.util.Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromStringList(value: String?): List<String>? {
        return value?.split(",")?.map { it.trim() }
    }

    @TypeConverter
    fun fromList(list: List<String>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun fromFloatArray(value: String?): FloatArray? {
        if (value.isNullOrBlank()) return null
        return value.split(",").map { it.toFloat() }.toFloatArray()
    }

    @TypeConverter
    fun toFloatArray(array: FloatArray?): String? {
        return array?.joinToString(",")
    }
}
