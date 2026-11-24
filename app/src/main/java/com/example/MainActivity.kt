package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.navigation.CalcoraApp
import com.example.ui.theme.CalcoraTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val calcoraViewModel: CalcoraViewModel = viewModel()
      val preferences by calcoraViewModel.preferences.collectAsStateWithLifecycle()
      CalcoraTheme(
        themeId = preferences.themeId,
        appearanceMode = preferences.appearanceMode,
      ) {
        CalcoraApp(viewModel = calcoraViewModel)
      }
    }
  }
}
