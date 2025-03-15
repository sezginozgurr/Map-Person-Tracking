package com.example.persontrackingapp.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persontrackingapp.domain.model.LocationPoint
import com.example.persontrackingapp.domain.usecase.ClearAllLocationPointsUseCase
import com.example.persontrackingapp.domain.usecase.GetAddressFromLocationUseCase
import com.example.persontrackingapp.domain.usecase.GetAllLocationPointsUseCase
import com.example.persontrackingapp.domain.usecase.TrackingStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getAllLocationPointsUseCase: GetAllLocationPointsUseCase,
    private val clearAllLocationPointsUseCase: ClearAllLocationPointsUseCase,
    private val getAddressFromLocationUseCase: GetAddressFromLocationUseCase,
    private val trackingStatusUseCase: TrackingStatusUseCase
) : ViewModel() {
    
    private val _selectedLocationAddress = MutableStateFlow<String?>(null)
    val selectedLocationAddress: StateFlow<String?> = _selectedLocationAddress.asStateFlow()
    
    val locationPoints = getAllLocationPointsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val isTrackingEnabled = trackingStatusUseCase.getTrackingStatus()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    
    fun setTrackingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            trackingStatusUseCase.setTrackingStatus(enabled)
        }
    }
    
    fun clearAllLocationPoints() {
        viewModelScope.launch {
            clearAllLocationPointsUseCase()
        }
    }
    
    fun getAddressForLocation(locationPoint: LocationPoint) {
        viewModelScope.launch {
            val address = getAddressFromLocationUseCase(locationPoint.latitude, locationPoint.longitude)
            _selectedLocationAddress.value = address
        }
    }
} 