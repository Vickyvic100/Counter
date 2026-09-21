package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {
    @Query("SELECT * FROM scans WHERE batchId = :batchId ORDER BY scannedAt DESC")
    fun getScansForBatch(batchId: Int): Flow<List<Scan>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertScan(scan: Scan)

    @Delete
    suspend fun deleteScan(scan: Scan)

    @Query("SELECT code, COUNT(*) as quantity FROM scans WHERE batchId = :batchId GROUP BY code ORDER BY code ASC")
    fun getAggregationForBatch(batchId: Int): Flow<List<ScanAggregation>>

    @Query("SELECT COUNT(*) FROM scans WHERE batchId = :batchId")
    fun getCountForBatch(batchId: Int): Flow<Int>

    @Query("SELECT COUNT(DISTINCT code) FROM scans WHERE batchId = :batchId")
    fun getDifferentCodesCountForBatch(batchId: Int): Flow<Int>

    @Query("SELECT * FROM scans WHERE batchId = :batchId AND sku = :sku LIMIT 1")
    suspend fun getScanBySku(batchId: Int, sku: String): Scan?

    @Query("DELETE FROM scans WHERE batchId = :batchId")
    suspend fun deleteAllScansForBatch(batchId: Int)

    @Query("SELECT * FROM scans")
    suspend fun getAllScansSync(): List<Scan>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllScans(scans: List<Scan>)
}
