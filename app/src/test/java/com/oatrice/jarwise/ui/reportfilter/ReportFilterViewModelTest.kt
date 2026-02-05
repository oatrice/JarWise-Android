package com.oatrice.jarwise.ui.reportfilter

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.ui.graphics.Color
import com.oatrice.jarwise.data.JarConfig
import com.oatrice.jarwise.data.repository.JarConfigSource
import com.oatrice.jarwise.data.repository.WalletSource
import com.oatrice.jarwise.model.Wallet
import com.oatrice.jarwise.util.MainDispatcherRule
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
        val jarSource = FakeJarConfigSource(
            listOf(
                JarConfig("jar-1", "Necessities", 55, "blue", "home"),
                JarConfig("jar-2", "Play", 10, "pink", "gamepad")
            )
        )
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
        viewModel = ReportFilterViewModel(jarSource, walletSource)
    }

    @Test
    fun `init loads data and updates uiState`() = runTest {
        val uiState = viewModel.uiState.first { !it.isLoading }

        assertFalse(uiState.isLoading)
        assertEquals(2, uiState.jars.size)
        assertEquals("Necessities", uiState.jars[0].name)
        assertEquals(2, uiState.wallets.size)
        assertEquals("Cash", uiState.wallets[0].name)
        assertTrue(uiState.jars.all { !it.isSelected })
        assertTrue(uiState.wallets.all { !it.isSelected })
    }

    @Test
    fun `toggleJarSelection updates selected state`() = runTest {
        viewModel.uiState.first { !it.isLoading }

        viewModel.toggleJarSelection("jar-1")

        val uiState = viewModel.uiState.value
        assertTrue(uiState.jars.first { it.id == "jar-1" }.isSelected)
        assertFalse(uiState.jars.first { it.id == "jar-2" }.isSelected)

        val selected = viewModel.getSelectedFilterIds()
        assertTrue(selected.first.contains("jar-1"))
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

        viewModel.toggleJarSelection("jar-1")
        viewModel.toggleWalletSelection("wallet-2")

        viewModel.clearSelections()

        val uiState = viewModel.uiState.value
        assertTrue(uiState.jars.all { !it.isSelected })
        assertTrue(uiState.wallets.all { !it.isSelected })

        val selected = viewModel.getSelectedFilterIds()
        assertTrue(selected.first.isEmpty())
        assertTrue(selected.second.isEmpty())
    }
}

private class FakeJarConfigSource(
    private val jars: List<JarConfig>
) : JarConfigSource {
    override suspend fun getAllJarConfigs(): List<JarConfig> = jars
    override suspend fun initializeDefaultsIfEmpty() = Unit
}

private class FakeWalletSource(
    private val wallets: List<Wallet>
) : WalletSource {
    override suspend fun getAllWallets(): List<Wallet> = wallets
    override suspend fun initializeDefaultsIfEmpty() = Unit
}
