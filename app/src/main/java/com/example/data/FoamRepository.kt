package com.example.data

import kotlinx.coroutines.flow.Flow

class FoamRepository(private val batchDao: BatchDao, private val scanDao: ScanDao) {

    val allBatches: Flow<List<Batch>> = batchDao.getAllBatches()
    val batchSummaries: Flow<List<BatchSummary>> = batchDao.getBatchSummaries()

    suspend fun insertBatch(batch: Batch): Long = batchDao.insertBatch(batch)
    suspend fun updateBatch(batch: Batch) = batchDao.updateBatch(batch)
    suspend fun getBatchById(id: Int): Batch? = batchDao.getBatchById(id)
    suspend fun getActiveBatch(): Batch? = batchDao.getActiveBatch()

    fun getScansForBatch(batchId: Int): Flow<List<Scan>> = scanDao.getScansForBatch(batchId)
    fun getAggregationForBatch(batchId: Int): Flow<List<ScanAggregation>> = scanDao.getAggregationForBatch(batchId)
    fun getCountForBatch(batchId: Int): Flow<Int> = scanDao.getCountForBatch(batchId)
    fun getDifferentCodesCountForBatch(batchId: Int): Flow<Int> = scanDao.getDifferentCodesCountForBatch(batchId)

    suspend fun insertScan(scan: Scan) = scanDao.insertScan(scan)
    suspend fun deleteScan(scan: Scan) = scanDao.deleteScan(scan)
    suspend fun isSkuScanned(batchId: Int, sku: String): Boolean {
        return scanDao.getScanBySku(batchId, sku) != null
    }
    
    suspend fun getAllBatchesSync() = batchDao.getAllBatchesSync()
    suspend fun getAllScansSync() = scanDao.getAllScansSync()

    suspend fun restoreData(batches: List<Batch>, scans: List<Scan>) {
        batchDao.insertAllBatches(batches)
        scanDao.insertAllScans(scans)
    }
}
