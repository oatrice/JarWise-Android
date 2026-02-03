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

    private var isAutoBackupPaused = false
    private var isBackupPending = false

    fun setAutoBackupPaused(paused: Boolean) {
        if (isAutoBackupPaused == paused) return
        isAutoBackupPaused = paused
        if (!paused && isBackupPending) {
            isBackupPending = false
            triggerAutoBackup()
        }
    }

    fun triggerAutoBackup() {
        if (isAutoBackupPaused) {
            isBackupPending = true
            return
        }
        
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
        isBackupPending = false
        
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
    
    suspend fun checkForBackup(): Result<List<BackupMetadata>> {
        return cloudStorageService.listBackups()
    }
    
    suspend fun restoreBackup(fileId: String): Result<Unit> {
        val destFile = dbFileProvider()
        // Simple overwrite strategy for now.
        // In reality, we might want to download to a temp file, validate, then move.
        // And importantly, close Room DB connection before this.
        val downloadResult = cloudStorageService.downloadBackup(fileId, destFile)
        
        if (downloadResult.isSuccess) {
            try {
                debugLogRestoredData(destFile)
            } catch (e: Exception) {
                logger.e("BackupManager", "Failed to debug log restored data (non-fatal)", e)
            }
        }
        
        return downloadResult
    }

    private suspend fun debugLogRestoredData(dbFile: File) {
        // Manually open the DB file to inspect contents
        val context = (logger as? com.oatrice.jarwise.utils.AndroidAppLogger)?.context 
            ?: return
            // If logger doesn't expose context, we can't build Room easily without passing context.
            // Alternative: Open SQLiteDatabase directly?
            // Let's assume we can use SQLite API directly to avoid Room complexity here.
            
        try {
             val db = android.database.sqlite.SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                android.database.sqlite.SQLiteDatabase.OPEN_READONLY
            )
            
            // Query Transactions
            val cursorTx = db.rawQuery("SELECT COUNT(*), MAX(date) FROM transactions", null)
            if (cursorTx.moveToFirst()) {
                val count = cursorTx.getInt(0)
                val lastDate = cursorTx.getString(1)
                logger.d("BackupManager", "[RESTORED DEBUG] Transactions Found: $count, Latest: $lastDate")
            }
            cursorTx.close()
            
            // Query Jars (Allocations)
            val cursorJars = db.rawQuery("SELECT COUNT(*), group_concat(name) FROM allocations WHERE parentId IS NULL", null)
             if (cursorJars.moveToFirst()) {
                val count = cursorJars.getInt(0)
                val names = cursorJars.getString(1)
                logger.d("BackupManager", "[RESTORED DEBUG] Top-level Jars Found: $count, Names: $names")
            }
            cursorJars.close()

            db.close()
        } catch (e: Exception) {
            logger.e("BackupManager", "Error inspecting DB file", e)
        }
    }
}
