package com.oatrice.jarwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.oatrice.jarwise.ui.managejars.ManageJarsScreen
import com.oatrice.jarwise.ui.managejars.ManageJarsViewModel
import com.oatrice.jarwise.ui.managewallets.ManageWalletsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.oatrice.jarwise.ui.AddTransactionScreen
import com.oatrice.jarwise.ui.DashboardScreen
import com.oatrice.jarwise.ui.MainViewModel
import com.oatrice.jarwise.ui.ScanScreen
import com.oatrice.jarwise.ui.SettingsScreen
import com.oatrice.jarwise.ui.SlipViewModel
import com.oatrice.jarwise.ui.TransactionHistoryScreen
import com.oatrice.jarwise.ui.theme.JarWiseTheme



sealed class Screen {
    data object Dashboard : Screen()
    data object TransactionHistory : Screen()
    data object Scan : Screen()
    data object AddTransaction : Screen()
    data object SlipImport : Screen()
    data object Settings : Screen()
    data object ManageJars : Screen()
    data object ManageWallets : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val viewModel: MainViewModel by viewModel()
        val slipViewModel: SlipViewModel by viewModel()
        val manageJarsViewModel: ManageJarsViewModel by viewModel()
        val manageWalletsViewModel: ManageWalletsViewModel by viewModel()

        enableEdgeToEdge()
        setContent {
            JarWiseTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
                val transactions by viewModel.transactions.collectAsState()
                val formattedTotalBalance by viewModel.formattedTotalBalance.collectAsState()
                val selectedCurrency by viewModel.selectedCurrency.collectAsState()
                val recentImages by slipViewModel.recentImages.collectAsState()

                val handleNavigation: (com.oatrice.jarwise.ui.components.NavPage) -> Unit = { page ->
                    when (page) {
                        com.oatrice.jarwise.ui.components.NavPage.DASHBOARD -> currentScreen = Screen.Dashboard
                        com.oatrice.jarwise.ui.components.NavPage.HISTORY -> currentScreen = Screen.TransactionHistory
                        com.oatrice.jarwise.ui.components.NavPage.ADD -> currentScreen = Screen.AddTransaction
                        // Add other destinations here when ready (WALLET, PROFILE)
                        else -> {}
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        is Screen.Dashboard -> {
                            val jars by viewModel.jars.collectAsState()
                            DashboardScreen(
                                jars = jars,
                                transactions = transactions,
                                formattedTotalBalance = formattedTotalBalance,
                                selectedCurrency = selectedCurrency,
                                onNavigateToHistory = { currentScreen = Screen.TransactionHistory },
                                onNavigateToScan = { currentScreen = Screen.Scan },
                                onNavigateToImport = { currentScreen = Screen.SlipImport },
                                onNavigateToAdd = { currentScreen = Screen.AddTransaction },
                                onNavigateToSettings = { currentScreen = Screen.Settings },
                                onNavigateToManageJars = { currentScreen = Screen.ManageJars },
                                onNavigate = handleNavigation
                            )
                        }
                        is Screen.Settings -> SettingsScreen(
                             onBack = { currentScreen = Screen.Dashboard },
                             onNavigateToManageWallets = { currentScreen = Screen.ManageWallets },
                             viewModel = viewModel
                        )
                        is Screen.TransactionHistory -> TransactionHistoryScreen(
                            transactions = transactions,
                            selectedCurrency = selectedCurrency,
                            onBack = { currentScreen = Screen.Dashboard },
                            onNavigate = handleNavigation
                        )
                        is Screen.Scan -> ScanScreen(
                            onNavigateBack = { currentScreen = Screen.Dashboard },
                            onPhotoCaptured = { _ ->
                                // For now, just return to dashboard after capture
                                currentScreen = Screen.Dashboard
                            }
                        )
                        is Screen.SlipImport -> {
                            val buckets by slipViewModel.buckets.collectAsState()
                            val selectedBucketId by slipViewModel.selectedBucketId.collectAsState()
                            val isScanning by slipViewModel.isScanning.collectAsState()
                            val scanStats by slipViewModel.scanStats.collectAsState()
                            
                            com.oatrice.jarwise.ui.SlipImportScreen(
                                recentImages = recentImages,
                                buckets = buckets,
                                selectedBucketId = selectedBucketId,
                                isScanning = isScanning,
                                scanStats = scanStats,
                                onBack = { currentScreen = Screen.Dashboard },
                                onPermissionResult = { slipViewModel.refreshImages() },
                                onBucketSelected = { bucketId -> slipViewModel.selectBucket(bucketId) },
                                onConfirmSlip = { _, parsedSlip, jarId ->
                                    val amount = parsedSlip.amount ?: 0.0
                                    val note = "Slip: ${parsedSlip.bankName ?: "Unknown"}"
                                    val date = parsedSlip.date?.let {
                                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                                        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                                        sdf.format(it)
                                    }
                                    viewModel.saveTransaction(amount, jarId, "wallet-bank", note, date)
                                    android.widget.Toast.makeText(applicationContext, "Slip saved successfully", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                onSaveDraft = { _, parsedSlip, jarId ->
                                    val amount = parsedSlip.amount ?: 0.0
                                    val note = "Slip: ${parsedSlip.bankName ?: "Unknown"}"
                                    val date = parsedSlip.date?.let {
                                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                                        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                                        sdf.format(it)
                                    }
                                    viewModel.saveDraft(amount, jarId, "wallet-bank", note, date)
                                    android.widget.Toast.makeText(applicationContext, "Draft saved!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        is Screen.AddTransaction -> AddTransactionScreen(
                            onBack = { currentScreen = Screen.Dashboard },
                            onSave = { amount, jarId, walletId, note, date ->
                                viewModel.saveTransaction(amount, jarId, walletId, note, date)
                                currentScreen = Screen.Dashboard
                            }
                        )
                        is Screen.ManageJars -> ManageJarsScreen(
                            viewModel = manageJarsViewModel,
                            onBack = { currentScreen = Screen.Dashboard }
                        )
                        is Screen.ManageWallets -> com.oatrice.jarwise.ui.managewallets.ManageWalletsScreen(
                            onNavigateBack = { currentScreen = Screen.Settings },
                            // viewModel = manageWalletsViewModel // Explicitly pass or let it use default if we change Screen signature
                            // Current Screen composable likely uses default viewModel() which won't work with Factory unless provided via LocalViewModelStoreOwner or passed directly.
                            // Assuming ManageWalletsScreen instantiates VM internally with koin/hilt or we need to pass it. 
                            // Looking at ManageWalletsScreen.kt (Step 441 in history), it uses `viewModel: ManageWalletsViewModel = viewModel()`.
                            // Without Hilt, `viewModel()` won't pick up the Factory associated with MainActivity unless we pass the *instance* or change how it's retrieved.
                            // Correct approach for simple DI: Pass the viewModel instance created in MainActivity.
                            viewModel = manageWalletsViewModel 
                        )
                    }
                }
            }
        }
    }
}
