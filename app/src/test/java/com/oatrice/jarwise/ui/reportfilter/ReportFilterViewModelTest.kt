package com.oatrice.jarwise.ui.reportfilter

import com.oatrice.jarwise.data.repository.WalletSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.ui.graphics.Color
import com.oatrice.jarwise.model.Wallet
import com.oatrice.jarwise.util.MainDispatcherRule
import com.oatrice.jarwise.utils.JARS_METADATA
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReportFilterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ReportFilterViewModel

    @Before
    fun setUp() {
        val walletSource = FakeWalletSource(
            listOf(
                Wallet(
                    id = "wallet-1",
                    name = "Cash",
                    balance = 0.0,
                    color = Color(0xFF22C55E),
                    icon = Icons.Default.AccountBalanceWallet
                ),
                Wallet(
                    id = "wallet-2",
                    name = "Bank",
                    balance = 0.0,
                    color = Color(0xFF3B82F6),
                    icon = Icons.Default.AccountBalanceWallet
                )
            )
        )
        viewModel = ReportFilterViewModel(walletSource)
    }

    @Test
    fun `init loads data and updates uiState`() = runTest {
        val uiState = viewModel.uiState.first { !it.isLoading }

        assertFalse(uiState.isLoading)
        assertEquals(JARS_METADATA.size, uiState.jars.size)
        assertEquals("Necessities", uiState.jars.first().name)
        assertEquals(2, uiState.wallets.size)
        assertEquals("Cash", uiState.wallets[0].name)
        assertTrue(uiState.jars.all { !it.isSelected })
        assertTrue(uiState.wallets.all { !it.isSelected })
    }

    @Test
    fun `toggleJarSelection updates selected state`() = runTest {
        viewModel.uiState.first { !it.isLoading }

        viewModel.toggleJarSelection("necessities")

        val uiState = viewModel.uiState.value
        assertTrue(uiState.jars.first { it.id == "necessities" }.isSelected)
        assertFalse(uiState.jars.first { it.id == "education" }.isSelected)

        val selected = viewModel.getSelectedFilterIds()
        assertTrue(selected.first.contains("necessities"))
    }

    @Test
    fun `toggleWalletSelection updates selected state`() = runTest {
        viewModel.uiState.first { !it.isLoading }

        viewModel.toggleWalletSelection("wallet-2")

        val uiState = viewModel.uiState.value
        assertTrue(uiState.wallets.first { it.id == "wallet-2" }.isSelected)
        assertFalse(uiState.wallets.first { it.id == "wallet-1" }.isSelected)

        val selected = viewModel.getSelectedFilterIds()
        assertTrue(selected.second.contains("wallet-2"))
    }

    @Test
    fun `clearSelections resets all selections`() = runTest {
        viewModel.uiState.first { !it.isLoading }

        viewModel.toggleJarSelection("necessities")
        viewModel.toggleWalletSelection("wallet-2")

        viewModel.clearSelections()

        val uiState = viewModel.uiState.value
        assertTrue(uiState.jars.all { !it.isSelected })
        assertTrue(uiState.wallets.all { !it.isSelected })

        val selected = viewModel.getSelectedFilterIds()
        assertTrue(selected.first.isEmpty())
        assertTrue(selected.second.isEmpty())
    }

    @Test
    fun `init handles empty wallet list`() = runTest {
        val emptyWalletSource = FakeWalletSource(emptyList())
        val emptyViewModel = ReportFilterViewModel(emptyWalletSource)

        val uiState = emptyViewModel.uiState.first { !it.isLoading }

        assertEquals(JARS_METADATA.size, uiState.jars.size)
        assertTrue(uiState.wallets.isEmpty())
    }

    @Test
    fun `init handles wallet source failure gracefully`() = runTest {
        val failingViewModel = ReportFilterViewModel(FailingWalletSource())

        val uiState = failingViewModel.uiState.first { !it.isLoading }

        assertEquals(JARS_METADATA.size, uiState.jars.size)
        assertTrue(uiState.wallets.isEmpty())
    }

    @Test
    fun `wallets with unusual names are preserved`() = runTest {
        val walletSource = FakeWalletSource(
            listOf(
                Wallet(
                    id = "wallet-special",
                    name = "Bank & Co. (Primary)",
                    balance = 0.0,
                    color = Color(0xFF3B82F6),
                    icon = Icons.Default.AccountBalanceWallet
                )
            )
        )
        val customViewModel = ReportFilterViewModel(walletSource)

        val uiState = customViewModel.uiState.first { !it.isLoading }

        assertEquals(1, uiState.wallets.size)
        assertEquals("Bank & Co. (Primary)", uiState.wallets[0].name)
    }
}

private class FakeWalletSource(
    private val wallets: List<Wallet>
) : WalletSource {
    override suspend fun getAllWallets(): List<Wallet> = wallets
    override suspend fun initializeDefaultsIfEmpty() = Unit
}

private class FailingWalletSource : WalletSource {
    override suspend fun getAllWallets(): List<Wallet> {
        throw IllegalStateException("Wallet data unavailable")
    }

    override suspend fun initializeDefaultsIfEmpty() = Unit
}
