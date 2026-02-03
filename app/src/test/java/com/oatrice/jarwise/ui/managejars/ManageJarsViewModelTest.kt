package com.oatrice.jarwise.ui.managejars

import com.oatrice.jarwise.data.Allocation
import com.oatrice.jarwise.data.repository.JarConfigRepository
import com.oatrice.jarwise.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ManageJarsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ManageJarsViewModel
    private lateinit var fakeDao: FakeJarConfigDao
    private val backupManager: com.oatrice.jarwise.data.backup.BackupManager = org.mockito.kotlin.mock()

    @Before
    fun setup() = runTest {
        fakeDao = FakeJarConfigDao()
        
        // Seed initial data
        val defaults = listOf(
            Allocation(id = 1, userId = "local_user", name = "Necessities", targetPercent = 55, sortOrder = 1, level = 0, parentId = null),
            Allocation(id = 2, userId = "local_user", name = "Education", targetPercent = 10, sortOrder = 2, level = 0, parentId = null),
            Allocation(id = 3, userId = "local_user", name = "Savings", targetPercent = 10, sortOrder = 3, level = 0, parentId = null),
            Allocation(id = 4, userId = "local_user", name = "Play", targetPercent = 10, sortOrder = 4, level = 0, parentId = null),
            Allocation(id = 5, userId = "local_user", name = "Investment", targetPercent = 10, sortOrder = 5, level = 0, parentId = null),
            Allocation(id = 6, userId = "local_user", name = "Give", targetPercent = 5, sortOrder = 6, level = 0, parentId = null)
        )
        fakeDao.insertAll(defaults)
        
        viewModel = ManageJarsViewModel(fakeDao, backupManager)
    }



    @Test
    fun `initial state should load defaults`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.jars.collect()
        }
        
        // Wait for potential emission
        advanceUntilIdle()

        assertEquals(6, viewModel.jars.value.size)
        assertEquals("Necessities", viewModel.jars.value[0].name)
        assertEquals(55, viewModel.jars.value[0].percentage)
    }

    @Test
    fun `updateJar should modify jar state`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.jars.collect() }
        
        val initialJars = viewModel.jars.value
        val targetId = initialJars[0].id

        viewModel.updateJar(targetId, name = "New Name", percentage = 60)

        val updatedJar = viewModel.jars.value.find { it.id == targetId }!!
        assertEquals("New Name", updatedJar.name)
        assertEquals(60, updatedJar.percentage)
    }

    @Test
    fun `totalPercentage should calculate correctly`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.jars.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.totalPercentage.collect() }
        
        // Given defaults (Sum = 100)
        assertEquals(100, viewModel.totalPercentage.value)

        // When modifying a jar
        val targetId = viewModel.jars.value[0].id // 55%
        viewModel.updateJar(targetId, percentage = 60) // +5% -> 105%

        // Then total should be 105
        assertEquals(105, viewModel.totalPercentage.value)
    }

    @Test
    fun `isValid should reflect total percentage validation`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.jars.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.totalPercentage.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.isValid.collect() }

        // Given defaults (100%)
        assertTrue("Initially valid", viewModel.isValid.value)

        // When invalid
        val targetId = viewModel.jars.value[0].id
        viewModel.updateJar(targetId, percentage = 60) // Total 105%

        // Then
        assertFalse("Should be invalid when total > 100", viewModel.isValid.value)
    }
    

    @Test
    fun `revertUnsavedChanges should restore original configuration`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.jars.collect() }
        
        // Given modified state
        val targetId = viewModel.jars.value[0].id
        viewModel.updateJar(targetId, name = "Changed")
        assertEquals("Changed", viewModel.jars.value[0].name)
        
        // When
        viewModel.revertUnsavedChanges()
        advanceUntilIdle() // Wait for ensure coroutine execution
        
        // Then
        assertEquals("Necessities", viewModel.jars.value[0].name)
    }
    
    @Test
    fun `save should persist to repository`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.jars.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.totalPercentage.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.isValid.collect() }
        
        val jar1 = viewModel.jars.value[0].id // 55 -> 50 (-5)
        val jar2 = viewModel.jars.value[1].id // 10 -> 15 (+5)
        
        viewModel.updateJar(jar1, percentage = 50)
        viewModel.updateJar(jar2, percentage = 15)
        
        assertEquals(100, viewModel.totalPercentage.value)
        assertTrue("Should be valid (100%) now", viewModel.isValid.value)
        
        // When
        var successCalled = false
        viewModel.save { successCalled = true }
        
        // Then
        assertTrue("Save callback should be called", successCalled)
        val savedData = fakeDao.getAll()
        assertEquals(50, savedData.find { it.id == jar1 }?.targetPercent)
        assertEquals(15, savedData.find { it.id == jar2 }?.targetPercent)
    }
}
