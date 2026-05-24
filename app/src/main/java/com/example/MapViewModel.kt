package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.AiOrchestrator
import com.example.ai.AiResponse
import com.example.location.LocationTrackingManager
import com.example.network.NetworkProvider
import com.example.network.NominatimResult
import com.example.network.OsrmRoute
import com.example.privacy.PrivacyUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.common.GeoPoint

data class MapState(
    val userLocation: GeoPoint? = null,
    val selectedDestination: GeoPoint? = null,
    val destinationName: String = "",
    val routingMode: RoutingMode = RoutingMode.SMART,
    val isTracking: Boolean = false,
    val aiSuggestions: List<PlaceSuggestion> = emptyList(),
    val searchResults: List<NominatimResult> = emptyList(),
    val currentRoute: OsrmRoute? = null,
    val isNinjaModeEnabled: Boolean = false,
    val latestAiResponse: AiResponse? = null
)

enum class RoutingMode(val title: String) {
    SMART("Smart Core"),
    SCENIC("Scenic Route"),
    QUIET("Quiet Path"),
    SHADE("Shadow Routing"),
    FRUGAL("Frugal (No Tolls)"),
    KASHTA("Kashta (Off-Road)")
}

data class PlaceSuggestion(val name: String, val point: GeoPoint, val vibe: String)

class MapViewModel(
    private val locationTracker: LocationTrackingManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapState())
    val uiState: StateFlow<MapState> = _uiState.asStateFlow()
    
    private val aiOrchestrator = AiOrchestrator()

    fun startTracking() {
        _uiState.update { it.copy(isTracking = true) }
        viewModelScope.launch {
            try {
                locationTracker.getLocationFlow().collect { location ->
                    // Ninja Mode (Phase 2): Instantly drops all telemetry/tracking logging.
                    // Only volatile memory holds the location, no disk writes, no network uploads.
                    if (!_uiState.value.isNinjaModeEnabled) {
                        // Standard telemetry/history logging would happen here
                    }
                    _uiState.update { 
                        it.copy(userLocation = GeoPoint(location.latitude, location.longitude)) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isTracking = false) }
            }
        }
    }

    fun toggleNinjaMode() {
        _uiState.update { it.copy(isNinjaModeEnabled = !it.isNinjaModeEnabled) }
    }

    fun setDestination(point: GeoPoint, name: String = "Selected location") {
        _uiState.update { it.copy(selectedDestination = point, destinationName = name) }
        calculateRoute(point)
    }

    fun searchPlace(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }
        viewModelScope.launch {
            try {
                val results = NetworkProvider.nominatimApi.searchPlace(query)
                _uiState.update { it.copy(searchResults = results) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearSearch() {
         _uiState.update { it.copy(searchResults = emptyList()) }
    }

    private fun calculateRoute(destination: GeoPoint) {
        val start = _uiState.value.userLocation ?: return
        viewModelScope.launch {
            try {
                val coords = "${start.longitude},${start.latitude};${destination.longitude},${destination.latitude}"
                val response = NetworkProvider.osrmApi.getRoute(
                    profile = "driving",
                    coordinates = coords
                )
                if (response.routes.isNotEmpty()) {
                    _uiState.update { it.copy(currentRoute = response.routes[0]) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(currentRoute = null) }
            }
        }
    }
    
    fun setRoutingMode(mode: RoutingMode) {
        _uiState.update { it.copy(routingMode = mode) }
    }

    fun searchVibe(vibeQuery: String) {
        viewModelScope.launch {
            val center = _uiState.value.userLocation ?: GeoPoint(24.7136, 46.6753)
            
            // Phase 2: PROMPT SCRUBBING
            // AI never sees exact coordinates. It gets a fuzzed bounding center.
            val fuzzedLocation = PrivacyUtils.scrubPromptLocation(center)
            
            // Phase 3: AI ORCHESTRATION
            val response = aiOrchestrator.processUserIntent(vibeQuery, fuzzedLocation)
            
            _uiState.update { it.copy(latestAiResponse = response) }

            // Mocking return data based on vibe
            val offsets = listOf(0.01 to 0.02, -0.015 to 0.01)
            val suggestions = offsets.mapIndexed { index, (latOffset, lonOffset) ->
                PlaceSuggestion(
                    name = "Scouted by ${response.modelUsed.split(" ")[0]}",
                    point = GeoPoint(center.latitude + latOffset, center.longitude + lonOffset),
                    vibe = vibeQuery
                )
            }
            _uiState.update { it.copy(aiSuggestions = suggestions) }
        }
    }

    class Factory(private val locationTracker: LocationTrackingManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MapViewModel(locationTracker) as T
        }
    }
}
