package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.BatchSummary
import com.example.ui.FoamViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: FoamViewModel,
    onBack: () -> Unit,
    onBatchClick: (BatchSummary) -> Unit
) {
    val summaries by viewModel.batchSummaries.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Count History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (summaries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No previous counts.")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(summaries) { summary ->
                    ListItem(
                        headlineContent = { Text(summary.name, fontWeight = FontWeight.Bold) },
                        supportingContent = { 
                            Text("${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(summary.date))} • ${summary.totalScanned} foams") 
                        },
                        trailingContent = {
                            Text(
                                summary.status,
                                color = if (summary.status == "COMPLETED") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.clickable { onBatchClick(summary) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
