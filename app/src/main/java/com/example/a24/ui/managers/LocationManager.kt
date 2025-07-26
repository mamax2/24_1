package com.example.a24.ui.managers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.coroutines.resume

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)

class LocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val geocoder = Geocoder(context, Locale.getDefault())

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getCurrentLocation(): LocationData? {
        if (!hasLocationPermission()) {
            return null
        }

        return try {
            val location = fusedLocationClient.lastLocation.await()
            location?.let {
                val address = getAddressFromLocation(it.latitude, it.longitude)
                LocationData(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    address = address
                )
            }
        } catch (e: SecurityException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getLocationFromAddress(address: String): LocationData? {
        return try {
            suspendCancellableCoroutine { continuation ->
                try {
                    val addresses = geocoder.getFromLocationName(address, 1)
                    if (addresses?.isNotEmpty() == true) {
                        val location = addresses[0]
                        continuation.resume(
                            LocationData(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                address = getFormattedAddress(location)
                            )
                        )
                    } else {
                        continuation.resume(null)
                    }
                } catch (e: Exception) {
                    continuation.resume(null)
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String? {
        return try {
            suspendCancellableCoroutine { continuation ->
                try {
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (addresses?.isNotEmpty() == true) {
                        continuation.resume(getFormattedAddress(addresses[0]))
                    } else {
                        continuation.resume(null)
                    }
                } catch (e: Exception) {
                    continuation.resume(null)
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getFormattedAddress(address: Address): String {
        val parts = mutableListOf<String>()

        address.thoroughfare?.let { parts.add(it) }
        address.locality?.let { parts.add(it) }
        address.adminArea?.let { parts.add(it) }
        address.countryName?.let { parts.add(it) }

        return if (parts.isNotEmpty()) {
            parts.joinToString(", ")
        } else {
            "${address.latitude}, ${address.longitude}"
        }
    }

    fun openInMaps(latitude: Double, longitude: Double, label: String? = null) {
        try {
            val uri = if (label != null) {
                "geo:$latitude,$longitude?q=$latitude,$longitude($label)"
            } else {
                "geo:$latitude,$longitude"
            }

            val intent = android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse(uri)
            )
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to Google Maps web
            val url = "https://www.google.com/maps?q=$latitude,$longitude"
            val intent = android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse(url)
            )
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    fun calculateDistance(
        lat1: Double, lng1: Double,
        lat2: Double, lng2: Double
    ): Double {
        val earthRadius = 6371.0 // Raggio della Terra in km

        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadius * c // Distanza in km
    }

    fun formatDistance(distanceKm: Double): String {
        return when {
            distanceKm < 1.0 -> "${(distanceKm * 1000).toInt()} m"
            distanceKm < 10.0 -> "${"%.1f".format(distanceKm)} km"
            else -> "${distanceKm.toInt()} km"
        }
    }

    fun getDirections(
        fromLat: Double,
        fromLng: Double,
        toLat: Double,
        toLng: Double
    ) {
        try {
            val directionsUrl = "https://www.google.com/maps/dir/?api=1" +
                    "&origin=$fromLat,$fromLng" +
                    "&destination=$toLat,$toLng" +
                    "&travelmode=driving"

            val intent = android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse(directionsUrl)
            )
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK

            intent.setPackage("com.google.android.apps.maps")

            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                intent.setPackage(null)
                context.startActivity(intent)
            }

        } catch (e: Exception) {
            try {
                val uri = "geo:$fromLat,$fromLng?q=$toLat,$toLng"
                val intent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse(uri)
                )
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun getDirectionsToAddress(
        fromLat: Double,
        fromLng: Double,
        destinationAddress: String
    ) {
        try {
            val encodedDestination = java.net.URLEncoder.encode(destinationAddress, "UTF-8")
            val directionsUrl = "https://www.google.com/maps/dir/?api=1" +
                    "&origin=$fromLat,$fromLng" +
                    "&destination=$encodedDestination" +
                    "&travelmode=driving"

            val intent = android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse(directionsUrl)
            )
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK

            // Prova ad aprire Google Maps
            intent.setPackage("com.google.android.apps.maps")

            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback al browser
                intent.setPackage(null)
                context.startActivity(intent)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getLocationFromAddressWithDistance(
        address: String,
        currentLocation: LocationData
    ): Pair<LocationData?, Double?> {
        val addressLocation = getLocationFromAddress(address)

        return if (addressLocation != null) {
            val distance = calculateDistance(
                currentLocation.latitude, currentLocation.longitude,
                addressLocation.latitude, addressLocation.longitude
            )
            Pair(addressLocation, distance)
        } else {
            Pair(null, null)
        }
    }
}