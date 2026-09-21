package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.FoamCounterTheme
import com.example.ui.FoamViewModel
import com.example.ui.FoamViewModelFactory
import com.example.ui.screens.*

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    val app = application as FoamCounterApp
    val viewModelFactory = FoamViewModelFactory(app.repository)
    val viewModel: FoamViewModel = ViewModelProvider(this, viewModelFactory)[FoamViewModel::class.java]
    viewModel.loadActiveBatch()
    
    setContent {
      FoamCounterTheme {
        val navController = rememberNavController()
        
        NavHost(navController = navController, startDestination = "home") {
          composable("home") {
            HomeScreen(
              viewModel = viewModel,
              onNewCount = { navController.navigate("new_count") },
              onHistory = { navController.navigate("history") },
              onSettings = { navController.navigate("settings") },
              onBatchClick = { batch ->
                viewModel.setBatch(batch.let { b -> com.example.data.Batch(b.id, b.name, b.date, status = b.status) }) // Simplified conversion
                navController.navigate("batch_detail")
              }
            )
          }
          composable("new_count") {
            NewBatchScreen(
              viewModel = viewModel,
              onBack = { navController.popBackStack() },
              onStartScanning = { navController.navigate("scanning") }
            )
          }
          composable("scanning") {
            ScanningScreen(
              viewModel = viewModel,
              onBack = { navController.popBackStack() },
              onViewResults = { navController.navigate("batch_detail") }
            )
          }
          composable("batch_detail") {
            BatchDetailScreen(
              viewModel = viewModel,
              onBack = { navController.popBackStack() }
            )
          }
          composable("history") {
            HistoryScreen(
              viewModel = viewModel,
              onBack = { navController.popBackStack() },
              onBatchClick = { summary ->
                viewModel.setBatch(com.example.data.Batch(summary.id, summary.name, summary.date, status = summary.status))
                navController.navigate("batch_detail")
              }
            )
          }
          composable("settings") {
            SettingsScreen(
              viewModel = viewModel,
              onBack = { navController.popBackStack() }
            )
          }
        }
      }
    }
  }
}
