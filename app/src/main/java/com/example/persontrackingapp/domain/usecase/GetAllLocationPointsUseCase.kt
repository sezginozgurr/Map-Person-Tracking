package com.example.persontrackingapp.domain.usecase

import com.example.persontrackingapp.domain.model.LocationPoint
import com.example.persontrackingapp.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllLocationPointsUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    operator fun invoke(): Flow<List<LocationPoint>> {
        return repository.getAllLocationPoints()
    }
} 