package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.location.LocationTrackingManager
import com.example.ui.MapScreen
import com.example.ui.theme.MyApplicationTheme
import org.maplibre.android.MapLibre

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Explicitly initialize MapLibre
    MapLibre.getInstance(this)

    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          val locationTracker = remember { LocationTrackingManager(this@MainActivity) }
          val mapViewModel: MapViewModel = viewModel(
              factory = MapViewModel.Factory(locationTracker)
          )
          
          MapScreen(
              viewModel = mapViewModel,
              modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}
