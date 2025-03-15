package com.example.persontrackingapp.presentation.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.persontrackingapp.R
import com.example.persontrackingapp.data.service.LocationService
import com.example.persontrackingapp.databinding.ActivityMapBinding
import com.example.persontrackingapp.databinding.DialogAddressInfoBinding
import com.example.persontrackingapp.domain.model.LocationPoint
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    
    private lateinit var binding: ActivityMapBinding
    private val viewModel: MapViewModel by viewModels()
    
    private var googleMap: GoogleMap? = null
    private var locationPoints = listOf<LocationPoint>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupClickListeners() {
        binding.btnToggleTracking.setOnClickListener {
            if (viewModel.isTrackingEnabled.value) {
                stopLocationTracking()
            } else {
                startLocationTracking()
            }
        }
        
        binding.btnClearRoute.setOnClickListener {
            viewModel.clearAllLocationPoints()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.locationPoints.collect { points ->
                        locationPoints = points
                        updateMapMarkers()
                    }
                }
                
                launch {
                    viewModel.isTrackingEnabled.collect { isEnabled ->
                        updateTrackingButton(isEnabled)
                    }
                }
                
                launch {
                    viewModel.selectedLocationAddress.collect { address ->
                        address?.let { showAddressDialog(it) }
                    }
                }
            }
        }
    }
    
    private fun updateTrackingButton(isTracking: Boolean) {
        binding.btnToggleTracking.text = if (isTracking) {
            getString(R.string.stop_tracking)
        } else {
            getString(R.string.start_tracking)
        }
    }
    
    private fun startLocationTracking() {
        if (checkLocationPermission()) {
            viewModel.setTrackingEnabled(true)
            LocationService.startService(this)
        }
    }
    
    private fun stopLocationTracking() {
        viewModel.setTrackingEnabled(false)
        LocationService.stopService(this)
    }
    
    private fun checkLocationPermission(): Boolean {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val backgroundLocationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        
        val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        
        if (!fineLocationPermission || !coarseLocationPermission || !notificationPermission) {
            requestLocationPermissions()
            return false
        }
        
        if (!backgroundLocationPermission) {
            requestBackgroundLocationPermission()
            return false
        }
        
        return true
    }
    
    private fun requestLocationPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        ActivityCompat.requestPermissions(
            this,
            permissions.toTypedArray(),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }
    
    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.background_location_permission_title)
                .setMessage(R.string.background_location_permission_message)
                .setPositiveButton(R.string.settings) { _, _ ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        requestBackgroundLocationPermission()
                    } else {
                        startLocationTracking()
                    }
                } else {
                    showPermissionDeniedDialog()
                }
            }
            BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationTracking()
                }
            }
        }
    }
    
    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.permission_required)
            .setMessage(R.string.permission_rationale)
            .setPositiveButton(R.string.settings) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true
        
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        }
        
        map.setOnMarkerClickListener { marker ->
            val locationPoint = locationPoints.find {
                it.latitude == marker.position.latitude && it.longitude == marker.position.longitude
            }
            
            locationPoint?.let {
                viewModel.getAddressForLocation(it)
            }
            
            true
        }
        
        updateMapMarkers()
    }
    
    private fun updateMapMarkers() {
        val map = googleMap ?: return
        
        map.clear()
        
        if (locationPoints.isEmpty()) return
        
        val boundsBuilder = LatLngBounds.Builder()
        
        locationPoints.forEach { point ->
            val latLng = point.toLatLng()
            map.addMarker(MarkerOptions().position(latLng))
            boundsBuilder.include(latLng)
        }
        
        try {
            val bounds = boundsBuilder.build()
            val padding = resources.getDimensionPixelSize(R.dimen.map_padding)
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
        } catch (e: Exception) {
            // Tek bir nokta varsa veya başka bir hata durumunda
            val lastPoint = locationPoints.last().toLatLng()
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(lastPoint, 15f))
        }
    }
    
    private fun showAddressDialog(address: String) {
        val dialogBinding = DialogAddressInfoBinding.inflate(layoutInflater)
        
        // Logo ekleme
        dialogBinding.ivLogo.setImageResource(R.drawable.logo_second)
        
        // Adres başlığı ve içeriği
        dialogBinding.tvAddressTitle.text = getString(R.string.address_title)
        dialogBinding.tvAddressContent.text = address.ifEmpty { getString(R.string.address_not_found) }
        
        // Dialog oluşturma
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(true) // Arka plana tıklayınca kapanabilir
            .create()
        
        // Kapat butonu işlevselliği
        dialogBinding.btnClose.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 1002
    }
} 