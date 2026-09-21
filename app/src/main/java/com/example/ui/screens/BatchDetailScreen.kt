package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.data.Scan
import com.example.data.ScanAggregation
import com.example.ui.FoamViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchDetailScreen(
    viewModel: FoamViewModel,
    onBack: () -> Unit
) {
    val batch by viewModel.activeBatch.collectAsState()
    val batchId = batch?.id ?: return
    
    val aggregation by viewModel.getAggregation(batchId).collectAsState(initial = emptyList())
    val scans by viewModel.getScans(batchId).collectAsState(initial = emptyList())
    
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf<Scan?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(batch?.name ?: "Results") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        exportToCsv(context, batch!!.name, aggregation, scans.size)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Export")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Report", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("History", modifier = Modifier.padding(16.dp))
                }
            }
            
            if (selectedTab == 0) {
                ReportTab(aggregation, scans.size)
            } else {
                HistoryTab(scans, onDelete = { showDeleteConfirm = it })
            }
        }
    }
    
    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Remove Scan?") },
            text = { Text("Removing SKU ${showDeleteConfirm!!.sku} will reduce the count for this batch.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteScan(showDeleteConfirm!!)
                    showDeleteConfirm = null
                }) {
                    Text("REMOVE", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
fun ReportTab(aggregation: List<ScanAggregation>, total: Int) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Code", fontWeight = FontWeight.Bold)
                Text("Quantity", fontWeight = FontWeight.Bold)
            }
            HorizontalDivider()
        }
        items(aggregation) { item ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(item.code)
                Text(item.quantity.toString())
            }
        }
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("TOTAL", fontWeight = FontWeight.Bold)
                Text(total.toString(), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HistoryTab(scans: List<Scan>, onDelete: (Scan) -> Unit) {
    if (scans.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No scans found.")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(scans) { scan ->
                ListItem(
                    headlineContent = { Text(scan.sku) },
                    supportingContent = { Text("Code: ${scan.code} • ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(scan.scannedAt))}") },
                    trailingContent = {
                        IconButton(onClick = { onDelete(scan) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}

fun exportToCsv(context: Context, batchName: String, aggregation: List<ScanAggregation>, total: Int) {
    val timestamp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val fileName = "HYSAM_Foam_Count_${batchName.replace(" ", "_")}_$timestamp.csv"
    val file = File(context.cacheDir, fileName)
    
    file.bufferedWriter().use { writer ->
        writer.write("Code,Quantity\n")
        aggregation.forEach {
            writer.write("${it.code},${it.quantity}\n")
        }
        writer.write("TOTAL,$total\n")
    }
    
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share CSV Report"))
}
