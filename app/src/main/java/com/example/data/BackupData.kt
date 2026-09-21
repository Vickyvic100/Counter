package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BackupData(
    val batches: List<Batch>,
    val scans: List<Scan>
)
