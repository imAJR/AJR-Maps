package com.example.network

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// NOMINATIM SEARCH

@JsonClass(generateAdapter = true)
data class NominatimResult(
    val place_id: Long?,
    val lat: String,
    val lon: String,
    val display_name: String,
    val type: String?,
    val name: String?
)

interface NominatimApi {
    @GET("search")
    suspend fun searchPlace(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 10
    ): List<NominatimResult>

    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "json"
    ): NominatimResult
}

// OSRM ROUTING (Free and public!)

@JsonClass(generateAdapter = true)
data class OsrmResponse(
    val code: String,
    val routes: List<OsrmRoute>
)

@JsonClass(generateAdapter = true)
data class OsrmRoute(
    val geometry: OsrmGeometry,
    val distance: Double,
    val duration: Double
)

@JsonClass(generateAdapter = true)
data class OsrmGeometry(
    val coordinates: List<List<Double>>, // [lon, lat]
    val type: String
)

interface OsrmRoutingApi {
    @GET("route/v1/{profile}/{coordinates}")
    suspend fun getRoute(
        @Path("profile") profile: String, // "driving", "walking", "cycling"
        @Path("coordinates") coordinates: String, // lon,lat;lon,lat
        @Query("overview") overview: String = "full",
        @Query("geometries") geometries: String = "geojson"
    ): OsrmResponse
}
