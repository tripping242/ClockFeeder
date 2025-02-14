package com.codingblocks.clock.core.local

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import timber.log.Timber
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ZonedDateTimeConverter {
    private val formatter = DateTimeFormatter.ISO_DATE_TIME
    @TypeConverter
    fun toString(entity: ZonedDateTime): String {
        return formatter.format(entity)
    }
    @TypeConverter
    fun fromString(serialized: String): ZonedDateTime {
        return ZonedDateTime.from(formatter.parse(serialized))
    }
}