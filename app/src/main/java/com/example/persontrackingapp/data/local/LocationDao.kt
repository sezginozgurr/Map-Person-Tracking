package com.example.persontrackingapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationPoint(locationEntity: LocationEntity): Long
    
    @Query("SELECT * FROM location_points ORDER BY timestamp DESC")
    fun getAllLocationPoints(): Flow<List<LocationEntity>>
    
    @Query("SELECT * FROM location_points WHERE id = :id")
    suspend fun getLocationPointById(id: Long): LocationEntity?
    
    @Query("DELETE FROM location_points")
    suspend fun clearAllLocationPoints()
} 