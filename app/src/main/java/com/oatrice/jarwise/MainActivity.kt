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
import androidx.room.Room
import com.oatrice.jarwise.data.AppDatabase

import com.oatrice.jarwise.data.GeneratedMockData
import com.oatrice.jarwise.data.repository.CurrencyRepository
import com.oatrice.jarwise.data.repository.UserPreferencesRepository
import com.oatrice.jarwise.data.service.SlipDetectorServiceImpl
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
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "jarwise-db"
        ).build()
        
        val userPreferencesRepository = UserPreferencesRepository(applicationContext)
        val currencyRepository = CurrencyRepository(userPreferencesRepository)
        
        val viewModel: MainViewModel by viewModels { MainViewModel.Factory(db.transactionDao(), currencyRepository) }

        val slipRepository = com.oatrice.jarwise.data.repository.SlipRepository(applicationContext)
        val slipDetector = SlipDetectorServiceImpl(applicationContext)
        val slipViewModel: SlipViewModel by viewModels { 
            SlipViewModel.Factory(slipRepository, slipDetector) 
        }

        enableEdgeToEdge()
        setContent {
            JarWiseTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
                val transactions by viewModel.transactions.collectAsState()
                val formattedTotalBalance by viewModel.formattedTotalBalance.collectAsState()
                val selectedCurrency by viewModel.selectedCurrency.collectAsState()
                val recentImages by slipViewModel.recentImages.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        is Screen.Dashboard -> DashboardScreen(
                            jars = GeneratedMockData.jars, // Ideally usage ViewModel for jars too
                            transactions = transactions,
                            formattedTotalBalance = formattedTotalBalance,
                            selectedCurrency = selectedCurrency,
                            onNavigateToHistory = { currentScreen = Screen.TransactionHistory },
                            onNavigateToScan = { currentScreen = Screen.Scan },
                            onNavigateToImport = { currentScreen = Screen.SlipImport },
                            onNavigateToAdd = { currentScreen = Screen.AddTransaction },
                            onNavigateToSettings = { currentScreen = Screen.Settings }
                        )
                        is Screen.Settings -> SettingsScreen(
                             onBack = { currentScreen = Screen.Dashboard },
                             viewModel = viewModel
                        )
                        is Screen.TransactionHistory -> TransactionHistoryScreen(
                            transactions = transactions,
                            selectedCurrency = selectedCurrency,
                            onBack = { currentScreen = Screen.Dashboard }
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
                                    viewModel.saveTransaction(amount, jarId, note, date)
                                    android.widget.Toast.makeText(applicationContext, "Slip saved successfully", android.widget.Toast.LENGTH_SHORT).show()
                                    // Navigate back or show success? For now, stay on screen or go to dashboard.
                                    // currentScreen = Screen.Dashboard // Optional: auto-navigate
                                }
                            )
                        }
                        is Screen.AddTransaction -> AddTransactionScreen(
                            onBack = { currentScreen = Screen.Dashboard },
                            onSave = { amount, jarId, note ->
                                viewModel.saveTransaction(amount, jarId, note)
                                currentScreen = Screen.Dashboard
                            }
                        )
                    }
                }
            }
        }
    }
}
