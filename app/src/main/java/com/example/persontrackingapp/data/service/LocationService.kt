package com.example.persontrackingapp.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.example.persontrackingapp.R
import com.example.persontrackingapp.domain.model.LocationPoint
import com.example.persontrackingapp.domain.usecase.GetAddressFromLocationUseCase
import com.example.persontrackingapp.domain.usecase.SaveLocationPointUseCase
import com.example.persontrackingapp.presentation.map.MapActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {
    
    @Inject
    lateinit var saveLocationPointUseCase: SaveLocationPointUseCase
    
    @Inject
    lateinit var getAddressFromLocationUseCase: GetAddressFromLocationUseCase
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    
    private var lastLocation: Location? = null
    private val minDistanceThreshold = 10f // 100 metre
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "location_channel"
        
        fun startService(context: Context) {
            val intent = Intent(context, LocationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, LocationService::class.java)
            context.stopService(intent)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationCallback()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        startLocationUpdates()
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }
    
    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    processNewLocation(location)
                }
            }
        }
    }
    
    private fun processNewLocation(location: Location) {
        val currentLocation = location
        val previousLocation = lastLocation
        
        if (previousLocation == null || 
            calculateDistance(previousLocation, currentLocation) >= minDistanceThreshold) {
            
            lastLocation = currentLocation
            
            serviceScope.launch {
                val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
                val address = getAddressFromLocationUseCase(currentLocation.latitude, currentLocation.longitude)
                val locationPoint = LocationPoint.fromLatLng(latLng, address)
                saveLocationPointUseCase(locationPoint)
            }
        }
    }
    
    private fun calculateDistance(previous: Location, current: Location): Float {
        return previous.distanceTo(current)
    }
    
    private fun startLocationUpdates() {
        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(3000)
                .setMaxUpdateDelayMillis(10000)
                .build()
                
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
    
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.location_channel_name)
            val descriptionText = getString(R.string.location_channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle(getString(R.string.location_notification_title))
        .setContentText(getString(R.string.location_notification_text))
        .setSmallIcon(R.drawable.ic_location)
        .setContentIntent(createPendingIntent())
        .build()
    
    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(this, MapActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
} 