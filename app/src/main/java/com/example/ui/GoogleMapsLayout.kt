package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleMapsLayout(
    mapContent: @Composable () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchResults: List<com.example.network.NominatimResult>,
    onSearchResultClick: (com.example.network.NominatimResult) -> Unit,
    onAvatarClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onLayersClick: () -> Unit,
    onMyLocationClick: () -> Unit,
    onDirectionsClick: () -> Unit,
    onVoiceNavClick: () -> Unit,
    onARExploreClick: () -> Unit,
    onOfflineClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedBottomNav by remember { mutableStateOf(0) }
    var isSearchFocused by remember { mutableStateOf(false) }
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 8.dp
            ) {
                val items = listOf(
                    Triple("Explore", Icons.Default.Explore, 0),
                    Triple("Go", Icons.Default.DirectionsCar, 1),
                    Triple("Saved", Icons.Default.Bookmark, 2),
                    Triple("Contribute", Icons.Default.AddCircleOutline, 3),
                    Triple("Updates", Icons.Default.Notifications, 4)
                )
                items.forEach { (title, icon, index) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = title) },
                        label = { Text(title) },
                        selected = selectedBottomNav == index,
                        onClick = { selectedBottomNav = index }
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 120.dp,
            sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            sheetContent = {
                // Persistent Explore Sheet
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(300.dp) // Provide some scrollable area
                ) {
                    Text("Explore nearby", style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Quick Action Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        QuickActionItem(Icons.Default.Restaurant, "Restaurants")
                        QuickActionItem(Icons.Default.LocalCafe, "Coffee")
                        QuickActionItem(Icons.Default.Hotel, "Hotels")
                        QuickActionItem(Icons.Default.MoreHoriz, "More")
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Recents / Home / Work
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        SuggestionChip(onClick = {}, label = { Text("Home") }, icon = { Icon(Icons.Default.Home, null) })
                        SuggestionChip(onClick = {}, label = { Text("Work") }, icon = { Icon(Icons.Default.Work, null) })
                        SuggestionChip(onClick = onOfflineClick, label = { Text("Offline") }, icon = { Icon(Icons.Default.Download, null) })
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        SuggestionChip(onClick = onVoiceNavClick, label = { Text("Voice Nav") }, icon = { Icon(Icons.Default.Mic, null) })
                        SuggestionChip(onClick = onARExploreClick, label = { Text("AR Explore") }, icon = { Icon(Icons.Default.CameraAlt, null) })
                    }
                }
            },
            modifier = Modifier.padding(padding).fillMaxSize()
        ) { _ ->
            Box(modifier = Modifier.fillMaxSize()) {
                
                // Background Map Layer
                mapContent()
                
                // Top Floating Search Bar Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("Search here") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            leadingIcon = {
                                IconButton(onClick = { /* Handle icon click */ }) {
                                    Icon(Icons.Default.Search, contentDescription = "Search icon")
                                }
                            },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = onVoiceClick) {
                                        Icon(Icons.Default.Mic, contentDescription = "Voice Search")
                                    }
                                    IconButton(
                                        onClick = onAvatarClick,
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text(
                                            "A", 
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        )
                    }
                    
                    if (searchResults.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            androidx.compose.foundation.lazy.LazyColumn(
                                modifier = Modifier.heightIn(max = 300.dp)
                            ) {
                                items(searchResults.size) { index ->
                                    val result = searchResults[index]
                                    ListItem(
                                        headlineContent = { Text(result.name ?: result.display_name.split(",").firstOrNull() ?: "Unknown Result") },
                                        supportingContent = { Text(result.display_name, maxLines = 1) },
                                        leadingContent = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                                        modifier = Modifier.clickable { onSearchResultClick(result) }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Right Side Controls: Compass, Layers, Location, Direction
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp, top = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FloatingMapAction(icon = Icons.Default.Explore, onClick = {}) // Compass
                    FloatingMapAction(icon = Icons.Default.Layers, onClick = onLayersClick)
                }
                
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 140.dp, end = 16.dp), // Leaves space for bottom sheet
                    verticalArrangement = Arrangement.spacedBy(16.dp) // Added Directions shortcut
                ) {
                    FloatingMapAction(icon = Icons.Default.MyLocation, onClick = onMyLocationClick)
                    FloatingMapAction(
                        icon = Icons.Default.Directions, 
                        onClick = onDirectionsClick, 
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionItem(icon: ImageVector, title: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun FloatingMapAction(
    icon: ImageVector,
    onClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = containerColor,
        contentColor = contentColor,
        modifier = Modifier.size(48.dp)
    ) {
        Icon(icon, contentDescription = null)
    }
}
