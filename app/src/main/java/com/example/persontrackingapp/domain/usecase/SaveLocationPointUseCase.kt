package com.example.persontrackingapp.domain.usecase

import com.example.persontrackingapp.domain.model.LocationPoint
import com.example.persontrackingapp.domain.repository.LocationRepository
import javax.inject.Inject

class SaveLocationPointUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    suspend operator fun invoke(locationPoint: LocationPoint): Long {
        return repository.saveLocationPoint(locationPoint)
    }
} 