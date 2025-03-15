package com.example.persontrackingapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [LocationEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class LocationDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
} 