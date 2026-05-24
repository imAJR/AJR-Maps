package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LocationTrackingManager(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @SuppressLint("MissingPermission")
    fun getLocationFlow(): Flow<Location> = callbackFlow {
        val locationListener = LocationListener { location ->
            trySend(location)
        }
        
        try {
            // Register for location updates if provider is enabled
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    2000L,
                    5f,
                    locationListener
                )
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    2000L,
                    5f,
                    locationListener
                )
            }
            
            // Try get last known
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let { trySend(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            close(e)
        }

        awaitClose {
            locationManager.removeUpdates(locationListener)
        }
    }
}
