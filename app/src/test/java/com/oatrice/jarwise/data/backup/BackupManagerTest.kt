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
    private val dbFile = mock<File>()
    
    @Test
    fun `triggerBackup SHOULD wait for debounce period BEFORE uploading`() = testScope.runTest {
        val manager = BackupManager(
            cloudStorageService = cloudStorageService,
            externalScope = this,
            dbFileProvider = { dbFile },
            logger = logger
        )

        manager.triggerBackup()

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

        manager.triggerBackup()
        advanceTimeBy(5000) // 5s passed
        verifyNoInteractions(cloudStorageService)

        manager.triggerBackup() // RESET timer
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

        manager.triggerBackup()
        
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

        manager.triggerBackup()
        advanceTimeBy(10001)
        
        assert(manager.syncStatus.value is SyncStatus.Error)
    }
}
