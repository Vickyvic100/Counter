package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "batches")
data class Batch(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val date: Long,
    val notes: String = "",
    val status: String = "ACTIVE", // ACTIVE, COMPLETED
    val completedAt: Long? = null
)

@Entity(
    tableName = "scans",
    foreignKeys = [
        ForeignKey(
            entity = Batch::class,
            parentColumns = ["id"],
            childColumns = ["batchId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["batchId", "sku"], unique = true)
    ]
)
data class Scan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val batchId: Int,
    val sku: String,
    val code: String,
    val scannedAt: Long = System.currentTimeMillis()
)

data class ScanAggregation(
    val code: String,
    val quantity: Int
)

data class BatchSummary(
    val id: Int,
    val name: String,
    val date: Long,
    val status: String,
    val totalScanned: Int,
    val differentCodes: Int
)
