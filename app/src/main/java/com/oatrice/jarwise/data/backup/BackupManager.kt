package com.oatrice.jarwise.data.backup

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class SyncStatus {
    data object Idle : SyncStatus()
    data object Syncing : SyncStatus()
    data class Success(val lastSyncedTime: Long) : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}

class BackupManager(
    private val cloudStorageService: CloudStorageService,
    private val externalScope: CoroutineScope,
    private val dbFileProvider: () -> File,
    private val logger: com.oatrice.jarwise.utils.AppLogger
) {
    private var backupJob: Job? = null
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()
    
    companion object {
        private const val DEBOUNCE_DELAY_MS = 10000L
    }

    fun triggerAutoBackup() {
        // Cancel previous job if it exists (debounce reset)
        backupJob?.cancel()
        
        backupJob = externalScope.launch {
            delay(DEBOUNCE_DELAY_MS)
            performBackup()
        }
    }

    fun triggerManualBackup() {
        // Cancel any pending auto backup to avoid double upload if user clicks just after edit
        backupJob?.cancel()
        
        externalScope.launch {
            performBackup()
        }
    }

    private suspend fun performBackup() {
        val file = dbFileProvider()
        logger.d("BackupManager", "Start backup: ${file.name}")
        _syncStatus.value = SyncStatus.Syncing
        val result = cloudStorageService.uploadBackup(file)
        result.onSuccess {
            logger.d("BackupManager", "End backup. File ID: $it")
            _syncStatus.value = SyncStatus.Success(System.currentTimeMillis())
        }.onFailure {
            logger.e("BackupManager", "Backup failed", it)
            _syncStatus.value = SyncStatus.Error(it.message ?: "Unknown error")
        }
    }
}
