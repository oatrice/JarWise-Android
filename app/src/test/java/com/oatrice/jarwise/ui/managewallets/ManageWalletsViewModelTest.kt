package com.oatrice.jarwise.ui.managewallets

import com.oatrice.jarwise.model.Wallet
import com.oatrice.jarwise.ui.theme.Blue500
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

import com.oatrice.jarwise.data.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.mockito.Mockito.mock

// A simple fake repository for testing
class FakeWalletRepository : WalletRepository(mock(com.oatrice.jarwise.data.WalletDao::class.java)) {
    private val walletsData = MutableStateFlow<List<Wallet>>(emptyList())

    override val wallets: Flow<List<Wallet>> = walletsData

    override suspend fun insertWallet(wallet: Wallet) {
        val currentList = walletsData.value.toMutableList()
        val walletToInsert = if (wallet.id.isBlank()) wallet.copy(id = System.nanoTime().toString()) else wallet
        currentList.add(walletToInsert)
        walletsData.value = currentList
    }

    override suspend fun updateWallet(wallet: Wallet) {
        val currentList = walletsData.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == wallet.id }
        if (index != -1) {
            currentList[index] = wallet
            walletsData.value = currentList
        }
    }
    
    override suspend fun deleteWallet(id: String) {
        val currentList = walletsData.value.toMutableList()
        currentList.removeIf { it.id == id }
        walletsData.value = currentList
    }
    
    override suspend fun initializeDefaultsIfEmpty() {
        // No-op for tests
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ManageWalletsViewModelTest {

    private lateinit var viewModel: ManageWalletsViewModel
    private lateinit var fakeRepository: FakeWalletRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeWalletRepository()
        viewModel = ManageWalletsViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createWallet(id: String, parentId: String? = null, level: Int = 0): Wallet {
        return Wallet(
            id = id,
            name = "Wallet $id",
            balance = 0.0,
            color = Blue500,
            icon = Icons.Rounded.Wallet,
            parentId = parentId,
            level = level
        )
    }

    @Test
    fun `adding a child wallet correctly sets its level`() = runTest(testDispatcher) {
        // Setup: Parent (L0)
        val parent = createWallet(id = "parent", level = 0)
        viewModel.addWallet(parent)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act: Add Child with parentId
        val child = createWallet(id = "child", parentId = "parent", level = 0) // Passing 0 initially
        viewModel.addWallet(child)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Child should have level 1
        val addedChild = viewModel.wallets.value.find { it.id == "child" }
        assertEquals(1, addedChild?.level)
    }

    @Test
    fun `re-parenting a wallet updates its level correctly`() = runTest(testDispatcher) {
        // Setup: Parent (L0) and Child (L0 initially)
        val parent = createWallet(id = "parent", level = 0)
        val child = createWallet(id = "child", level = 0)
        
        // Initial State (clear mock data first for clean test if possible, but VM init loads mock data)
        // Since VM loads mock data, let's just work with new wallets added or find existing ones?
        // Better to rely on what logic does. 
        // For strict testing, we might want to empty the list first or just add ours.
        
        // Let's rely on adding new ones to be safe from mock data interference
        viewModel.addWallet(parent)
        viewModel.addWallet(child)
        testDispatcher.scheduler.advanceUntilIdle() // Process updates

        // Act: Move child to parent
        val updatedChild = child.copy(parentId = parent.id)
        viewModel.updateWallet(updatedChild)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val wallets = viewModel.wallets.value
        val resultChild = wallets.find { it.id == "child" }
        assertEquals(1, resultChild?.level)
        assertEquals("parent", resultChild?.parentId)
    }

    @Test
    fun `re-parenting updates children recursively`() = runTest(testDispatcher) {
        // Setup: Root -> Parent -> Child
        // Initially: All separate
        val root = createWallet(id = "root", level = 0)
        val middle = createWallet(id = "middle", level = 0)
        val leaf = createWallet(id = "leaf", level = 0)

        // Add them
        viewModel.addWallet(root)
        viewModel.addWallet(middle)
        viewModel.addWallet(leaf)
        testDispatcher.scheduler.advanceUntilIdle()

        // Link Middle -> Root
        viewModel.updateWallet(middle.copy(parentId = root.id))
        testDispatcher.scheduler.advanceUntilIdle()

        // Link Leaf -> Middle
        var currentMiddle = viewModel.wallets.value.find { it.id == "middle" }!!
        viewModel.updateWallet(leaf.copy(parentId = currentMiddle.id))
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify Initial Chain: Root(0) -> Middle(1) -> Leaf(2)
        assertEquals(0, viewModel.wallets.value.find { it.id == "root" }?.level)
        assertEquals(1, viewModel.wallets.value.find { it.id == "middle" }?.level)
        assertEquals(2, viewModel.wallets.value.find { it.id == "leaf" }?.level)

        // Act: Move Middle to Top Level (parentId = null)
        currentMiddle = viewModel.wallets.value.find { it.id == "middle" }!!
        viewModel.updateWallet(currentMiddle.copy(parentId = null))
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Middle becomes 0, Leaf becomes 1
        assertEquals(0, viewModel.wallets.value.find { it.id == "middle" }?.level)
        assertEquals(1, viewModel.wallets.value.find { it.id == "leaf" }?.level)
    }

    @Test
    fun `circular dependency is detected and blocked`() = runTest(testDispatcher) {
        // A -> B
        val a = createWallet("A", level = 0)
        val b = createWallet("B", parentId = "A", level = 1)
        
        viewModel.addWallet(a)
        viewModel.addWallet(b)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act: Try to make A a child of B (A -> B -> A)
        viewModel.updateWallet(a.copy(parentId = "B"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Error Event Triggered, A's parent NOT changed
        val event = viewModel.uiEvent.value
        assertTrue(event is ManageWalletsViewModel.UiEvent.ShowError)
        assertTrue((event as ManageWalletsViewModel.UiEvent.ShowError).message.contains("Circular"))

        val resultA = viewModel.wallets.value.find { it.id == "A" }
        assertNull(resultA?.parentId)
    }

    @Test
    fun `max depth exceeded is blocked`() = runTest(testDispatcher) {
        // 0 -> 1 -> 2 (Max is 2, Depth 3 levels: 0, 1, 2)
        // If we try to add another level, it should fail
        
        val l0 = createWallet("L0", level = 0)
        val l1 = createWallet("L1", parentId = "L0", level = 1)
        val l2 = createWallet("L2", parentId = "L1", level = 2)

        viewModel.addWallet(l0)
        viewModel.addWallet(l1)
        viewModel.addWallet(l2)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act: Try to add L3 -> L2
        val l3 = createWallet("L3") // New wallet
        viewModel.addWallet(l3)
        testDispatcher.scheduler.advanceUntilIdle()

        // Update L3 to be child of L2
        val savedL3 = viewModel.wallets.value.find { it.name == "Wallet L3" }!!
        viewModel.updateWallet(savedL3.copy(parentId = "L2")) 
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert needs to check validation
        val event = viewModel.uiEvent.value
        assertTrue(event is ManageWalletsViewModel.UiEvent.ShowError)
        assertTrue((event as ManageWalletsViewModel.UiEvent.ShowError).message.contains("depth"))
        
        // Ensure L3 was not updated
        val resultL3 = viewModel.wallets.value.find { it.id == savedL3.id }
        assertNull(resultL3?.parentId)
    }
}
