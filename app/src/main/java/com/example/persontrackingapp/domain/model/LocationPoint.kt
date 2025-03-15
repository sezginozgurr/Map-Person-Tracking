package com.example.persontrackingapp.domain.model

import com.google.android.gms.maps.model.LatLng
import java.util.Date

data class LocationPoint(
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val timestamp: Date = Date()
) {
    fun toLatLng(): LatLng = LatLng(latitude, longitude)
    
    companion object {
        fun fromLatLng(latLng: LatLng, address: String? = null): LocationPoint {
            return LocationPoint(
                latitude = latLng.latitude,
                longitude = latLng.longitude,
                address = address
            )
        }
    }
} 