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
import com.oatrice.jarwise.data.service.SlipDetectorServiceImpl
import com.oatrice.jarwise.ui.AddTransactionScreen
import com.oatrice.jarwise.ui.DashboardScreen
import com.oatrice.jarwise.ui.MainViewModel
import com.oatrice.jarwise.ui.ScanScreen
import com.oatrice.jarwise.ui.TransactionHistoryScreen
import com.oatrice.jarwise.ui.theme.JarWiseTheme

sealed class Screen {
    data object Dashboard : Screen()
    data object TransactionHistory : Screen()
    data object Scan : Screen()
    data object AddTransaction : Screen()
    data object SlipImport : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "jarwise-db"
        ).build()
        
        val viewModel: MainViewModel by viewModels { MainViewModel.Factory(db.transactionDao()) }

        val slipRepository = com.oatrice.jarwise.data.repository.SlipRepository(applicationContext)
        val slipDetector = SlipDetectorServiceImpl(applicationContext)
        val slipViewModel: com.oatrice.jarwise.ui.SlipViewModel by viewModels { 
            com.oatrice.jarwise.ui.SlipViewModel.Factory(slipRepository, slipDetector) 
        }

        enableEdgeToEdge()
        setContent {
            JarWiseTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
                val transactions by viewModel.transactions.collectAsState()
                val recentImages by slipViewModel.recentImages.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        is Screen.Dashboard -> DashboardScreen(
                            transactions = transactions,
                            onNavigateToHistory = { currentScreen = Screen.TransactionHistory },
                            onNavigateToScan = { currentScreen = Screen.Scan },
                            onNavigateToImport = { currentScreen = Screen.SlipImport },
                            onNavigateToAdd = { currentScreen = Screen.AddTransaction }
                        )
                        is Screen.TransactionHistory -> TransactionHistoryScreen(
                            transactions = transactions,
                            onBack = { currentScreen = Screen.Dashboard }
                        )
                        is Screen.Scan -> ScanScreen(
                            onNavigateBack = { currentScreen = Screen.Dashboard },
                            onPhotoCaptured = { _ ->
                                // For now, just return to dashboard after capture
                                currentScreen = Screen.Dashboard
                            }
                        )
                        is Screen.SlipImport -> com.oatrice.jarwise.ui.SlipImportScreen(
                            recentImages = recentImages,
                            onBack = { currentScreen = Screen.Dashboard },
                            onPermissionResult = { slipViewModel.refreshImages() },
                            onImagesSelected = { uris -> slipViewModel.addSelectedImages(uris) }
                        )
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
