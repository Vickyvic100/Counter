package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FoamViewModel
import com.example.ui.ScanEvent
import com.example.ui.components.CameraPreview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScanningScreen(
    viewModel: FoamViewModel,
    onBack: () -> Unit,
    onViewResults: () -> Unit
) {
    val context = LocalContext.current
    val activeBatch by viewModel.activeBatch.collectAsState()
    val totalScanned by (activeBatch?.id?.let { viewModel.getCount(it) } ?: flowOf(0)).collectAsState(initial = 0)
    val differentCodes by (activeBatch?.id?.let { viewModel.getDifferentCodesCount(it) } ?: flowOf(0)).collectAsState(initial = 0)
    val lastScan by viewModel.lastScan.collectAsState()
    
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showSuccess by remember { mutableStateOf(false) }

    // Audio and Haptic setup
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100) }
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    LaunchedEffect(Unit) {
        viewModel.scanEvents.collect { event ->
            when (event) {
                ScanEvent.SUCCESS -> {
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 150)
                }
                ScanEvent.DUPLICATE -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(500)
                    }
                }
                ScanEvent.ERROR -> {
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 200)
                }
            }
        }
    }

    LaunchedEffect(viewModel.scanError) {
        viewModel.scanError.collect { error ->
            snackbarHostState.showSnackbar(error)
        }
    }
    
    LaunchedEffect(lastScan) {
        if (lastScan != null) {
            showSuccess = true
            delay(1500)
            showSuccess = false
        }
    }

    if (cameraPermissionState.status.isGranted) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text(activeBatch?.name ?: "Scanning") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.5f),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                CameraPreview { qrText ->
                    viewModel.processScan(qrText)
                }

                // Overlay UI
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Stats Box
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.7f),
                            contentColor = Color.White
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Scanned", style = MaterialTheme.typography.labelSmall)
                                    Text(totalScanned.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Different Codes", style = MaterialTheme.typography.labelSmall)
                                    Text(differentCodes.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            if (lastScan != null) {
                                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)
                                Text("Last Scan:", style = MaterialTheme.typography.labelSmall)
                                Text("Code: ${lastScan!!.code}", fontWeight = FontWeight.Bold)
                                Text("SKU: ${lastScan!!.sku}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = onViewResults,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.BarChart, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("RESULTS")
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Button(
                            onClick = {
                                viewModel.finishBatch()
                                onBack()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("FINISH")
                        }
                    }
                }
                
                // Success Indication
                if (showSuccess) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                                Text("Counted", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                lastScan?.let {
                                    Text(it.code, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Camera permission is required to scan QR codes.")
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

private fun <T> flowOf(value: T): kotlinx.coroutines.flow.Flow<T> = kotlinx.coroutines.flow.flowOf(value)
