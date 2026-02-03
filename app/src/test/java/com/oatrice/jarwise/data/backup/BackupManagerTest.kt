package com.oatrice.jarwise.data.backup

import com.oatrice.jarwise.data.auth.AuthService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import java.io.File

import com.oatrice.jarwise.utils.AppLogger

@OptIn(ExperimentalCoroutinesApi::class)
class BackupManagerTest {

    private val cloudStorageService: CloudStorageService = mock()
    private val logger: AppLogger = mock()
    private val authService: AuthService = mock() // We will likely need this later, but kept minimal for now
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    // We need a way to provide the file to upload.
    // For now, let's assume the manager knows how to get the DB file or we pass it in constructor/method.
    // Let's assume a DB file provider for simplicity in test
    // Use a real file in a temporary directory for tests to avoid Mockito issues with File
    private val tempDir = java.nio.file.Files.createTempDirectory("jarwise_test").toFile()
    private val dbFile = File(tempDir, "test.db")
    
    @Test
    fun `triggerBackup SHOULD wait for debounce period BEFORE uploading`() = testScope.runTest {
        val manager = BackupManager(
            cloudStorageService = cloudStorageService,
            externalScope = this,
            dbFileProvider = { dbFile },
            logger = logger
        )

        manager.triggerAutoBackup()

        // Advance time by 9 seconds (less than 10s debounce)
        advanceTimeBy(9000)
        verifyNoInteractions(cloudStorageService)

        // Advance time by 1 more second + epsilon
        advanceTimeBy(1001)
        verify(cloudStorageService).uploadBackup(dbFile)
    }

    @Test
    fun `triggerBackup SHOULD reset timer IF called again within window`() = testScope.runTest {
        val manager = BackupManager(
            cloudStorageService = cloudStorageService,
            externalScope = this,
             dbFileProvider = { dbFile },
             logger = logger
        )

        manager.triggerAutoBackup()
        advanceTimeBy(5000) // 5s passed
        verifyNoInteractions(cloudStorageService)

        manager.triggerAutoBackup() // RESET timer
        advanceTimeBy(6000) // 6s passed since restart (total 11s from start)
        // Should STILL be no interaction because timer restarted
        verifyNoInteractions(cloudStorageService)

        advanceTimeBy(4001) // 10s passed since second trigger
        verify(cloudStorageService).uploadBackup(dbFile)
    }

    @Test
    fun `SyncStatus should update to Syncing then Success on successful backup`() = testScope.runTest {
        val manager = BackupManager(
            cloudStorageService = cloudStorageService,
            externalScope = this,
             dbFileProvider = { dbFile },
             logger = logger
        )

        // Mock success
        org.mockito.kotlin.whenever(cloudStorageService.uploadBackup(dbFile))
            .thenReturn(Result.success("fileId"))

        manager.triggerAutoBackup()
        
        // Initial state should be Idle/Unknown ideally, but we test change
        
        advanceTimeBy(10001)
        
        // We need to access state. Since flow is cold/hot, we might need to collect it.
        // But for unit test, value property of StateFlow is easier if exposed.
        assert(manager.syncStatus.value is SyncStatus.Success)
    }

    @Test
    fun `SyncStatus should update to Syncing then Error on failure`() = testScope.runTest {
        val manager = BackupManager(
            cloudStorageService = cloudStorageService,
            externalScope = this,
             dbFileProvider = { dbFile },
             logger = logger
        )

        // Mock failure
        org.mockito.kotlin.whenever(cloudStorageService.uploadBackup(dbFile))
            .thenReturn(Result.failure(Exception("Network error")))

        manager.triggerAutoBackup()
        advanceTimeBy(10001)
        
        assert(manager.syncStatus.value is SyncStatus.Error)
    }

    @Test
    fun `checkForBackup should return list of backups`() = testScope.runTest {
        val manager = BackupManager(
            cloudStorageService = cloudStorageService,
            externalScope = this,
             dbFileProvider = { dbFile },
             logger = logger
        )

        val backups = listOf(
            BackupMetadata("id1", "backup1.db", 1000L, 500L),
            BackupMetadata("id2", "backup2.db", 2000L, 500L)
        )
        org.mockito.kotlin.whenever(cloudStorageService.listBackups())
            .thenReturn(Result.success(backups))

        val result = manager.checkForBackup()
        
        assert(result.isSuccess)
        assert(result.getOrNull() == backups)
        verify(cloudStorageService).listBackups()
    }

    @Test
    fun `restoreBackup should download file and return success`() = testScope.runTest {
        val manager = BackupManager(
            cloudStorageService = cloudStorageService,
            externalScope = this,
             dbFileProvider = { dbFile },
             logger = logger
        )

        val fileId = "testFileId"
        org.mockito.kotlin.whenever(cloudStorageService.downloadBackup(org.mockito.kotlin.eq(fileId), org.mockito.kotlin.any()))
            .thenReturn(Result.success(Unit))

        val result = manager.restoreBackup(fileId)
        
        assert(result.isSuccess)
        verify(cloudStorageService).downloadBackup(org.mockito.kotlin.eq(fileId), org.mockito.kotlin.any())
        @org.junit.After
    fun tearDown() {
        tempDir.deleteRecursively()
    }
}
}
