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
import org.koin.android.ext.android.inject
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
    data object Migration : Screen()
    data object Login : Screen()
    data object Reports : Screen()
}

private val slipDateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
    timeZone = java.util.TimeZone.getTimeZone("UTC")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val viewModel: MainViewModel by viewModel()
        val slipViewModel: SlipViewModel by viewModel()
        val manageJarsViewModel: ManageJarsViewModel by viewModel()
        val manageWalletsViewModel: ManageWalletsViewModel by viewModel()
        
        // Inject AuthService to check login status
        val authService: com.oatrice.jarwise.data.auth.AuthService by inject()

        enableEdgeToEdge()
        setContent {
            JarWiseTheme {
                // Determine initial screen based on auth state
                val currentUser by authService.currentUser.collectAsState()
                val initialScreen = if (currentUser != null) Screen.Dashboard else Screen.Login
                var currentScreen by remember { mutableStateOf<Screen>(initialScreen) }
                // Track previous screen for ManageWallets (Dashboard vs Settings)
                var previousScreen by remember { mutableStateOf<Screen?>(null) }
                
                val transactions by viewModel.transactions.collectAsState()
                val formattedTotalBalance by viewModel.formattedTotalBalance.collectAsState()
                val selectedCurrency by viewModel.selectedCurrency.collectAsState()
                val recentImages by slipViewModel.recentImages.collectAsState()

                val handleNavigation: (com.oatrice.jarwise.ui.components.NavPage) -> Unit = { page ->
                    when (page) {
                        com.oatrice.jarwise.ui.components.NavPage.DASHBOARD -> currentScreen = Screen.Dashboard
                        com.oatrice.jarwise.ui.components.NavPage.HISTORY -> currentScreen = Screen.TransactionHistory
                        com.oatrice.jarwise.ui.components.NavPage.ADD -> {
                            previousScreen = currentScreen
                            currentScreen = Screen.AddTransaction
                        }
                        com.oatrice.jarwise.ui.components.NavPage.REPORTS -> {
                            // Reports
                            currentScreen = Screen.Reports
                        }
                        com.oatrice.jarwise.ui.components.NavPage.PROFILE -> currentScreen = Screen.Settings
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
                                onNavigateToAdd = { 
                                    previousScreen = Screen.Dashboard
                                    currentScreen = Screen.AddTransaction 
                                },
                                onNavigateToSettings = { currentScreen = Screen.Settings },
                                onNavigateToManageJars = {
                                    manageJarsViewModel.revertUnsavedChanges()
                                    currentScreen = Screen.ManageJars
                                },
                                onNavigate = handleNavigation
                            )
                        }
                        is Screen.Settings -> SettingsScreen(
                             onBack = { currentScreen = Screen.Dashboard },
                             onNavigateToManageWallets = { 
                                 previousScreen = Screen.Settings
                                 currentScreen = Screen.ManageWallets 
                             },
                             onNavigateToMigration = { currentScreen = Screen.Migration },

                             viewModel = viewModel,
                             onNavigate = handleNavigation
                        )
                        is Screen.Migration -> {
                           val migrationViewModel: com.oatrice.jarwise.ui.migration.MigrationViewModel = org.koin.androidx.compose.koinViewModel()
                           com.oatrice.jarwise.ui.migration.MigrationScreen(
                               onBack = { 
                                   migrationViewModel.resetState()
                                   currentScreen = Screen.Settings 
                               },
                               onGoToDashboard = {
                                    migrationViewModel.resetState()
                                    currentScreen = Screen.Dashboard
                               },
                               viewModel = migrationViewModel
                           )
                        }
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
                                        slipDateFormat.format(it)
                                    }
                                    viewModel.saveTransaction(amount, jarId, "wallet-bank", note, date, "expense")
                                    android.widget.Toast.makeText(applicationContext, "Slip saved successfully", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                onSaveDraft = { _, parsedSlip, jarId ->
                                    val amount = parsedSlip.amount ?: 0.0
                                    val note = "Slip: ${parsedSlip.bankName ?: "Unknown"}"
                                    val date = parsedSlip.date?.let {
                                        slipDateFormat.format(it)
                                    }
                                    viewModel.saveDraft(amount, jarId, "wallet-bank", note, date, "expense")
                                    android.widget.Toast.makeText(applicationContext, "Draft saved!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        is Screen.AddTransaction -> AddTransactionScreen(
                            onBack = { 
                                currentScreen = previousScreen ?: Screen.Dashboard 
                                previousScreen = null
                            },
                            onSave = { amount, jarId, walletId, note, date, type ->
                                viewModel.saveTransaction(amount, jarId, walletId, note, date, type)
                                currentScreen = previousScreen ?: Screen.Dashboard
                                previousScreen = null
                            },
                            onSaveTransfer = { amount, fromWalletId, toWalletId, note, date ->
                                viewModel.saveTransfer(amount, fromWalletId, toWalletId, note, date)
                                currentScreen = previousScreen ?: Screen.Dashboard
                                previousScreen = null
                            }
                        )
                        is Screen.ManageJars -> ManageJarsScreen(
                            viewModel = manageJarsViewModel,
                            onBack = { currentScreen = Screen.Dashboard }
                        )
                        is Screen.ManageWallets -> com.oatrice.jarwise.ui.managewallets.ManageWalletsScreen(
                            onNavigateBack = { 
                                currentScreen = previousScreen ?: Screen.Dashboard
                                previousScreen = null // clear after use
                            },
                            viewModel = manageWalletsViewModel
                        )
                        is Screen.Login -> com.oatrice.jarwise.ui.login.LoginScreen(
                            onLoginSuccess = { currentScreen = Screen.Dashboard }
                        )
                        is Screen.Reports -> com.oatrice.jarwise.ui.reports.ReportsScreen(
                            onBack = { currentScreen = Screen.Dashboard },
                            onNavigate = handleNavigation
                        )
                    }
                }
            }
        }
    }
}
