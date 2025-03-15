package com.example.persontrackingapp.data.repository

import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.example.persontrackingapp.data.local.LocationDao
import com.example.persontrackingapp.data.local.LocationEntity
import com.example.persontrackingapp.data.local.PreferencesManager
import com.example.persontrackingapp.domain.model.LocationPoint
import com.example.persontrackingapp.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume

class LocationRepositoryImpl @Inject constructor(
    private val locationDao: LocationDao,
    private val preferencesManager: PreferencesManager,
    private val context: Context
) : LocationRepository {
    
    override suspend fun saveLocationPoint(locationPoint: LocationPoint): Long {
        val locationEntity = LocationEntity.fromLocationPoint(locationPoint)
        return locationDao.insertLocationPoint(locationEntity)
    }
    
    override fun getAllLocationPoints(): Flow<List<LocationPoint>> {
        return locationDao.getAllLocationPoints().map { entities ->
            entities.map { it.toLocationPoint() }
        }
    }
    
    override suspend fun getLocationPointById(id: Long): LocationPoint? {
        return locationDao.getLocationPointById(id)?.toLocationPoint()
    }
    
    override suspend fun clearAllLocationPoints() {
        locationDao.clearAllLocationPoints()
    }
    
    override suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        if (addresses.isEmpty()) {
                            continuation.resume(null)
                        } else {
                            val address = addresses[0]
                            val addressParts = buildAddressParts(address)
                            continuation.resume(addressParts.joinToString(", "))
                        }
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                
                if (addresses.isNullOrEmpty()) {
                    null
                } else {
                    val address = addresses[0]
                    val addressParts = buildAddressParts(address)
                    addressParts.joinToString(", ")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun buildAddressParts(address: android.location.Address): MutableList<String> {
        val addressParts = mutableListOf<String>()
        
        if (address.thoroughfare != null) {
            addressParts.add(address.thoroughfare)
        }
        
        if (address.subThoroughfare != null) {
            addressParts.add(address.subThoroughfare)
        }
        
        if (address.locality != null) {
            addressParts.add(address.locality)
        }
        
        if (address.adminArea != null) {
            addressParts.add(address.adminArea)
        }
        
        if (address.countryName != null) {
            addressParts.add(address.countryName)
        }
        
        return addressParts
    }
    
    override fun isTrackingEnabled(): Flow<Boolean> {
        return preferencesManager.isTrackingEnabled
    }
    
    override suspend fun setTrackingEnabled(enabled: Boolean) {
        preferencesManager.setTrackingEnabled(enabled)
    }
} 