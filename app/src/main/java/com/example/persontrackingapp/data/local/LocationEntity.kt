package com.example.persontrackingapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.persontrackingapp.domain.model.LocationPoint
import java.util.Date

@Entity(tableName = "location_points")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val timestamp: Date
) {
    fun toLocationPoint(): LocationPoint {
        return LocationPoint(
            id = id,
            latitude = latitude,
            longitude = longitude,
            address = address,
            timestamp = timestamp
        )
    }
    
    companion object {
        fun fromLocationPoint(locationPoint: LocationPoint): LocationEntity {
            return LocationEntity(
                id = locationPoint.id,
                latitude = locationPoint.latitude,
                longitude = locationPoint.longitude,
                address = locationPoint.address,
                timestamp = locationPoint.timestamp
            )
        }
    }
} 