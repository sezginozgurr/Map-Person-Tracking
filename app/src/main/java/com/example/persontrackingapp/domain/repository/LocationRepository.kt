package com.example.persontrackingapp.domain.repository

import com.example.persontrackingapp.domain.model.LocationPoint
import kotlinx.coroutines.flow.Flow

interface LocationRepository {

    suspend fun saveLocationPoint(locationPoint: LocationPoint): Long

    fun getAllLocationPoints(): Flow<List<LocationPoint>>

    suspend fun getLocationPointById(id: Long): LocationPoint?

    suspend fun clearAllLocationPoints()

    suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String?

    fun isTrackingEnabled(): Flow<Boolean>

    suspend fun setTrackingEnabled(enabled: Boolean)

} 