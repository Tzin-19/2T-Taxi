package com.example.taxibookingproject.controller

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionsApiService {
    @GET("maps/api/directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") apiKey: String
    ): DirectionsResponse
}

data class DirectionsResponse(
    @SerializedName("routes") val routes: List<Route>,
    @SerializedName("status") val status: String
)

data class Route(
    @SerializedName("overview_polyline") val overviewPolyline: OverviewPolyline,
    @SerializedName("legs") val legs: List<Leg>
)

data class Leg(
    @SerializedName("distance") val distance: Distance,
    @SerializedName("duration") val duration: Duration?
)

data class Distance(
    @SerializedName("text") val text: String,
    @SerializedName("value") val value: Int // meters
)

data class Duration(
    @SerializedName("text") val text: String,
    @SerializedName("value") val value: Int
)

data class OverviewPolyline(
    @SerializedName("points") val points: String
)

data class RouteData(
    val points: List<LatLng>,
    val distanceKm: Double,
    val distanceText: String,
    val durationText: String
)

class DirectionsController {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(DirectionsApiService::class.java)

    suspend fun getRouteData(origin: LatLng, dest: LatLng, apiKey: String): RouteData? {
        return try {
            val response = apiService.getDirections(
                "${origin.latitude},${origin.longitude}",
                "${dest.latitude},${dest.longitude}",
                apiKey
            )
            if (response.status == "OK" && response.routes.isNotEmpty()) {
                val route = response.routes[0]
                val points = decodePolyline(route.overviewPolyline.points)
                val leg = route.legs.firstOrNull()
                val distanceValue = leg?.distance?.value ?: 0
                val distanceText = leg?.distance?.text ?: "0 km"
                val durationText = leg?.duration?.text ?: ""
                
                RouteData(points, distanceValue / 1000.0, distanceText, durationText)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getRoutePoints(origin: LatLng, dest: LatLng, apiKey: String): List<LatLng> {
        return getRouteData(origin, dest, apiKey)?.points ?: emptyList()
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
            poly.add(p)
        }
        return poly
    }
}
