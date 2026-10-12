package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.screens.StudioViewModel
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.MotionStudioTheme
import com.example.ui.theme.StudioBgDark

sealed class AppScreen {
  data object Dashboard : AppScreen()
  data class Studio(val projectId: String) : AppScreen()
}

class MainActivity : ComponentActivity() {

  private val viewModel: StudioViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      val activeProject by viewModel.activeProject.collectAsState()
      val accentColor = remember(activeProject?.accentColorHex) {
        try {
          Color(android.graphics.Color.parseColor(activeProject?.accentColorHex ?: "#00F0FF"))
        } catch (_: Exception) {
          AccentCyan
        }
      }

      var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Dashboard) }

      // Permission launcher for microphone voice-over recording
      val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
      ) { /* Handled gracefully in ViewModel */ }

      LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO)
          != PackageManager.PERMISSION_GRANTED
        ) {
          audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
      }

      MotionStudioTheme(accentColor = accentColor) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = StudioBgDark
        ) {
          when (val screen = currentScreen) {
            is AppScreen.Dashboard -> {
              DashboardScreen(
                viewModel = viewModel,
                onOpenStudio = { projectId ->
                  currentScreen = AppScreen.Studio(projectId)
                }
              )
            }
            is AppScreen.Studio -> {
              StudioScreen(
                viewModel = viewModel,
                onNavigateBack = {
                  currentScreen = AppScreen.Dashboard
                }
              )
            }
          }
        }
      }
    }
  }
}
