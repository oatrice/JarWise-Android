package com.oatrice.jarwise.ui.managejars

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
    private lateinit var repository: JarConfigRepository

    @Before
    fun setup() {
        fakeDao = FakeJarConfigDao()
        repository = JarConfigRepository(fakeDao)
        viewModel = ManageJarsViewModel(repository)
    }

    @Test
    fun `initial state should load defaults`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.jars.collect()
        }

        // Wait for flow to emit defaults
        val jars = viewModel.jars.value
        
        // If empty, it might need time or dispatch
        if (jars.isEmpty()) {
            // Wait for update (using first filter) or use advanceUntilIdle if needed
            // But Unconfined should be fast enough if collected
        }

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
    fun `resetToDefaults should restore original configuration`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.jars.collect() }
        
        // Given modified state
        val targetId = viewModel.jars.value[0].id
        viewModel.updateJar(targetId, name = "Changed")
        assertEquals("Changed", viewModel.jars.value[0].name)
        
        // When
        viewModel.resetToDefaults()
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
        assertEquals(50, savedData.find { it.id == jar1 }?.percentage)
        assertEquals(15, savedData.find { it.id == jar2 }?.percentage)
    }
}
