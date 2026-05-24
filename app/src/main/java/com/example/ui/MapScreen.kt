package com.example.ui

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.MapState
import com.example.MapViewModel
import com.example.RoutingMode
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.example.common.GeoPoint
import com.example.auth.AuthBottomSheet
import androidx.compose.material.icons.filled.AccountCircle

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        if (!permissionState.allPermissionsGranted) {
            permissionState.launchMultiplePermissionRequest()
        } else {
            viewModel.startTracking()
        }
    }

    LaunchedEffect(state.latestAiResponse) {
        state.latestAiResponse?.let {
            snackbarHostState.showSnackbar("Orchestrator [${it.modelUsed}]: ${it.action}")
        }
    }

    var showAuthMenu by remember { mutableStateOf(false) }
    val authSheetState = rememberModalBottomSheetState()
    
    var searchQuery by remember { mutableStateOf("") }
    var showAR by remember { mutableStateOf(false) }
    var showOfflineMenu by remember { mutableStateOf(false) }
    
    GoogleMapsLayout(
        mapContent = {
            val mapView = rememberMapViewWithLifecycle()
            
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    view.getMapAsync { map ->
                        map.setStyle("https://demotiles.maplibre.org/style.json") { style ->
                            
                            // Initialize sources if not present
                            if (style.getSource("route-source") == null) {
                                style.addSource(org.maplibre.android.style.sources.GeoJsonSource("route-source"))
                                style.addLayer(org.maplibre.android.style.layers.LineLayer("route-layer", "route-source").withProperties(
                                    org.maplibre.android.style.layers.PropertyFactory.lineWidth(5f),
                                    org.maplibre.android.style.layers.PropertyFactory.lineColor(android.graphics.Color.BLUE)
                                ))
                            }
                            if (style.getSource("marker-source") == null) {
                                style.addSource(org.maplibre.android.style.sources.GeoJsonSource("marker-source"))
                                // Add a default circle layer as marker
                                style.addLayer(org.maplibre.android.style.layers.CircleLayer("marker-layer", "marker-source").withProperties(
                                    org.maplibre.android.style.layers.PropertyFactory.circleRadius(8f),
                                    org.maplibre.android.style.layers.PropertyFactory.circleColor(android.graphics.Color.RED)
                                ))
                            }
                            
                            // Update route
                            val routeSource = style.getSourceAs<org.maplibre.android.style.sources.GeoJsonSource>("route-source")
                            val route = state.currentRoute
                            if (route != null) {
                                val coordinates = route.geometry.coordinates.map { 
                                    org.maplibre.geojson.Point.fromLngLat(it[0], it[1]) 
                                }
                                val lineString = org.maplibre.geojson.LineString.fromLngLats(coordinates)
                                routeSource?.setGeoJson(lineString)
                            } else {
                                routeSource?.setGeoJson(org.maplibre.geojson.FeatureCollection.fromFeatures(emptyArray()))
                            }

                            // Update marker
                            val markerSource = style.getSourceAs<org.maplibre.android.style.sources.GeoJsonSource>("marker-source")
                            val destination = state.selectedDestination
                            if (destination != null) {
                                val pt = org.maplibre.geojson.Point.fromLngLat(destination.longitude, destination.latitude)
                                markerSource?.setGeoJson(org.maplibre.geojson.Feature.fromGeometry(pt))
                                
                                // Animate camera to destination only strictly once we have result, wait let map handle bounds
                            } else {
                                markerSource?.setGeoJson(org.maplibre.geojson.FeatureCollection.fromFeatures(emptyArray()))
                            }

                            state.userLocation?.let { loc ->
                                // Optional: map.cameraPosition if you want tracking 
                                // We keep it free if user is dragging
                            }
                        }
                    }
                }
            )
        },
        searchQuery = searchQuery,
        onSearchQueryChange = { query -> 
            searchQuery = query 
            viewModel.searchPlace(query)
        },
        searchResults = state.searchResults,
        onSearchResultClick = { result ->
            searchQuery = result.display_name
            viewModel.clearSearch()
            viewModel.setDestination(GeoPoint(result.lat.toDouble(), result.lon.toDouble()), result.name ?: "Destination")
        },
        onAvatarClick = { showAuthMenu = true },
        onVoiceClick = { /* No-op */ },
        onLayersClick = { /* Will implement layers */ },
        onMyLocationClick = {
            if (!permissionState.allPermissionsGranted) {
                permissionState.launchMultiplePermissionRequest()
            }
        },
        onDirectionsClick = { /* Route */ },
        onVoiceNavClick = {
            val intent = android.content.Intent(context, com.example.navigation.VoiceNavigationService::class.java)
            intent.putExtra("TEXT_TO_SPEAK", "Recalculating route. In 100 meters, turn left.")
            context.startService(intent)
        },
        onARExploreClick = {
            showAR = !showAR
        },
        onOfflineClick = {
            showOfflineMenu = true
        }
    )
    
    if (showAR) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showAR = false }) {
            Box(modifier = Modifier.fillMaxSize()) {
                ARNavigationOverlay()
                IconButton(
                    onClick = { showAR = false },
                    modifier = Modifier.align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha=0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close AR", tint = Color.White)
                }
            }
        }
    }

    if (showOfflineMenu) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showOfflineMenu = false },
            title = { androidx.compose.material3.Text("Download Offline Map") },
            text = { androidx.compose.material3.Text("Download the current visible region (approx. 150 MB)? This will be saved to your device storage locally.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { 
                    // Make a fake call to the offline map manager
                    val dummyDefinition = org.maplibre.android.offline.OfflineTilePyramidRegionDefinition(
                        "https://demotiles.maplibre.org/style.json",
                        org.maplibre.android.geometry.LatLngBounds.Builder()
                            .include(org.maplibre.android.geometry.LatLng(25.0, 47.0))
                            .include(org.maplibre.android.geometry.LatLng(24.0, 46.0))
                            .build(),
                        10.0, 15.0, context.resources.displayMetrics.density
                    )
                    com.example.location.OfflineMapManager.downloadRegion(context, dummyDefinition)
                    showOfflineMenu = false 
                }) { androidx.compose.material3.Text("Download") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showOfflineMenu = false }) { androidx.compose.material3.Text("Cancel") }
            }
        )
    }

    if (showAuthMenu) {
        AuthBottomSheet(
            onDismiss = { showAuthMenu = false },
            sheetState = authSheetState
        )
    }
}

@Composable
fun AssistantItem(title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun getRoutingColor(mode: RoutingMode): Int {
    return when(mode) {
        RoutingMode.SMART -> android.graphics.Color.BLUE
        RoutingMode.SCENIC -> android.graphics.Color.MAGENTA
        RoutingMode.QUIET -> android.graphics.Color.CYAN
        RoutingMode.SHADE -> android.graphics.Color.DKGRAY
        RoutingMode.FRUGAL -> android.graphics.Color.parseColor("#43A047") // Green
        RoutingMode.KASHTA -> android.graphics.Color.parseColor("#FFB300") // Amber
    }
}
