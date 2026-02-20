package com.example.speedometerpro.data.repository

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.os.Looper
import com.example.speedometerpro.domain.repository.LocationRepository
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LocationRepositoryImpl(application: Application) : LocationRepository {

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(application)

    // Request settings for high-accuracy speedometer tracking
    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        1000L // update every 1 second
    )
        .setMinUpdateIntervalMillis(500L)
        .setWaitForAccurateLocation(true)
        .build()

    private var lastLocation: Location? = null
    private val minDistanceMeters = 0.5f

    // Smoothing variables (Low-pass filter)
    private var smoothedLat = 0.0
    private var smoothedLng = 0.0
    private var smoothedSpeed = 0f
    private val alpha = 0.2f // smoothing factor (lower = smoother but slower to react)

    /**
     * Applies a low-pass filter to the GPS coordinates and speed to
     * prevent the needle from jumping around due to GPS "noise".
     */
    private fun smoothLocation(location: Location): Location {
        if (smoothedLat == 0.0 && smoothedLng == 0.0) {
            smoothedLat = location.latitude
            smoothedLng = location.longitude
        } else {
            smoothedLat = alpha * location.latitude + (1 - alpha) * smoothedLat
            smoothedLng = alpha * location.longitude + (1 - alpha) * smoothedLng
        }

        // Smooth speed: Convert to km/h, smooth it, then convert back to m/s for the Location object
        val currentSpeedKmh = location.speed * 3.6f
        smoothedSpeed = alpha * currentSpeedKmh + (1 - alpha) * smoothedSpeed

        return Location(location).apply {
            latitude = smoothedLat
            longitude = smoothedLng
            speed = smoothedSpeed / 3.6f // Store as m/s
        }
    }

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(): Flow<Location> = callbackFlow {
        // Reset smoothing every time we start a new flow session
        resetSmoothing()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { location ->
                    // Filter out tiny jumps caused by GPS inaccuracy while standing still
                    val distance = lastLocation?.distanceTo(location) ?: Float.MAX_VALUE

                    if (distance >= minDistanceMeters) {
                        lastLocation = location
                        val finalLocation = smoothLocation(location)
                        trySend(finalLocation).isSuccess
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        )

        awaitClose {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }

    private fun resetSmoothing() {
        smoothedLat = 0.0
        smoothedLng = 0.0
        smoothedSpeed = 0f
        lastLocation = null
    }
}