package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BatchDao {
    @Query("SELECT * FROM batches ORDER BY date DESC")
    fun getAllBatches(): Flow<List<Batch>>

    @Query("SELECT * FROM batches WHERE id = :id")
    suspend fun getBatchById(id: Int): Batch?

    @Insert
    suspend fun insertBatch(batch: Batch): Long

    @Update
    suspend fun updateBatch(batch: Batch)

    @Delete
    suspend fun deleteBatch(batch: Batch)

    @Query("SELECT * FROM batches WHERE status = 'ACTIVE' LIMIT 1")
    suspend fun getActiveBatch(): Batch?

    @Query("""
        SELECT b.id, b.name, b.date, b.status, 
               COUNT(s.id) as totalScanned, 
               COUNT(DISTINCT s.code) as differentCodes
        FROM batches b
        LEFT JOIN scans s ON b.id = s.batchId
        GROUP BY b.id
        ORDER BY b.date DESC
    """)
    fun getBatchSummaries(): Flow<List<BatchSummary>>
    @Query("SELECT * FROM batches")
    suspend fun getAllBatchesSync(): List<Batch>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBatches(batches: List<Batch>)
}
