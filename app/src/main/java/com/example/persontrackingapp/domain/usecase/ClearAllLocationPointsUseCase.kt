package com.example.persontrackingapp.domain.usecase

import com.example.persontrackingapp.domain.repository.LocationRepository
import javax.inject.Inject

class ClearAllLocationPointsUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    suspend operator fun invoke() {
        repository.clearAllLocationPoints()
    }
} 