package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ScanEvent { SUCCESS, DUPLICATE, ERROR }

class FoamViewModel(private val repository: FoamRepository) : ViewModel() {

    val batchSummaries = repository.batchSummaries
    
    private val _activeBatch = MutableStateFlow<Batch?>(null)
    val activeBatch = _activeBatch.asStateFlow()

    private val _scanEvents = MutableSharedFlow<ScanEvent>()
    val scanEvents = _scanEvents.asSharedFlow()

    private val _scanError = MutableSharedFlow<String>()
    val scanError = _scanError.asSharedFlow()

    private val _lastScan = MutableStateFlow<Scan?>(null)
    val lastScan = _lastScan.asStateFlow()

    fun createBatch(name: String, notes: String) {
        viewModelScope.launch {
            val batch = Batch(
                name = name,
                date = System.currentTimeMillis(),
                notes = notes
            )
            val id = repository.insertBatch(batch)
            _activeBatch.value = repository.getBatchById(id.toInt())
        }
    }

    fun loadActiveBatch() {
        viewModelScope.launch {
            _activeBatch.value = repository.getActiveBatch()
        }
    }
    
    fun setBatch(batch: Batch) {
        _activeBatch.value = batch
    }

    fun processScan(qrText: String) {
        val batch = _activeBatch.value ?: return
        if (batch.status == "COMPLETED") return

        val result = QRParser.parse(qrText)
        if (result == null) {
            viewModelScope.launch { 
                _scanEvents.emit(ScanEvent.ERROR)
                _scanError.emit("Invalid QR Code: Missing SKU or Code") 
            }
            return
        }

        viewModelScope.launch {
            if (repository.isSkuScanned(batch.id, result.sku)) {
                _scanEvents.emit(ScanEvent.DUPLICATE)
                _scanError.emit("Duplicate Scan: This SKU has already been counted.")
            } else {
                val scan = Scan(
                    batchId = batch.id,
                    sku = result.sku,
                    code = result.code
                )
                repository.insertScan(scan)
                _lastScan.value = scan
                _scanEvents.emit(ScanEvent.SUCCESS)
            }
        }
    }

    fun getScans(batchId: Int) = repository.getScansForBatch(batchId)
    fun getAggregation(batchId: Int) = repository.getAggregationForBatch(batchId)
    fun getCount(batchId: Int) = repository.getCountForBatch(batchId)
    fun getDifferentCodesCount(batchId: Int) = repository.getDifferentCodesCountForBatch(batchId)

    fun finishBatch() {
        val batch = _activeBatch.value ?: return
        viewModelScope.launch {
            val completedBatch = batch.copy(
                status = "COMPLETED",
                completedAt = System.currentTimeMillis()
            )
            repository.updateBatch(completedBatch)
            _activeBatch.value = completedBatch
        }
    }

    fun deleteScan(scan: Scan) {
        viewModelScope.launch {
            repository.deleteScan(scan)
        }
    }

    suspend fun getBackupData(): BackupData {
        return BackupData(
            batches = repository.getAllBatchesSync(),
            scans = repository.getAllScansSync()
        )
    }

    fun restoreBackup(backupData: BackupData) {
        viewModelScope.launch {
            repository.restoreData(backupData.batches, backupData.scans)
        }
    }
}

class FoamViewModelFactory(private val repository: FoamRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoamViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FoamViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
