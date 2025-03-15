package com.example.persontrackingapp.domain.usecase

import com.example.persontrackingapp.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TrackingStatusUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    fun getTrackingStatus(): Flow<Boolean> {
        return repository.isTrackingEnabled()
    }
    
    suspend fun setTrackingStatus(enabled: Boolean) {
        repository.setTrackingEnabled(enabled)
    }
} 