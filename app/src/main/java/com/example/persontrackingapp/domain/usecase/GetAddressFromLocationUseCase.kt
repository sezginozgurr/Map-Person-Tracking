package com.example.persontrackingapp.domain.usecase

import com.example.persontrackingapp.domain.repository.LocationRepository
import javax.inject.Inject

class GetAddressFromLocationUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    suspend operator fun invoke(latitude: Double, longitude: Double): String? {
        return repository.getAddressFromLocation(latitude, longitude)
    }
} 