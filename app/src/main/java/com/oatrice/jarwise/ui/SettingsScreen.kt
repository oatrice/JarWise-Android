package com.oatrice.jarwise.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.oatrice.jarwise.data.backup.SyncStatus
import com.oatrice.jarwise.ui.settings.SettingsViewModel
import com.oatrice.jarwise.utils.TransactionDisplayUtils
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToManageWallets: () -> Unit = {},
    onNavigateToMigration: () -> Unit = {},
    viewModel: MainViewModel,
    settingsViewModel: SettingsViewModel = koinViewModel()
) {
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val currentUser by settingsViewModel.user.collectAsState()
    val syncStatus by settingsViewModel.syncStatus.collectAsState()
    val uiState by settingsViewModel.uiState.collectAsState()
    
    var expanded by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Restore Dialog Handling
    when (val state = uiState) {
        is SettingsViewModel.SettingsUiState.RestoreAvailable -> {
            AlertDialog(
                onDismissRequest = { /* Force choice */ },
                title = { Text("Backup Found") },
                text = { 
                    Column {
                        Text("We found a backup from ${state.backupDate}.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Do you want to restore it? Current data will be replaced.")
                    }
                },
                confirmButton = {
                    Button(onClick = { settingsViewModel.onRestoreConfirmed(state.fileId) }) {
                        Text("Restore")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { settingsViewModel.onRestoreCancelled() }) {
                        Text("No, start fresh")
                    }
                }
            )
        }
        is SettingsViewModel.SettingsUiState.RestoreInProgress -> {
             AlertDialog(
                onDismissRequest = {},
                title = { Text("Restoring...") },
                text = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Please wait")
                    }
                },
                confirmButton = {}
            )
        }
        is SettingsViewModel.SettingsUiState.RestoreSuccess -> {
             AlertDialog(
                onDismissRequest = {},
                title = { Text("Restore Successful") },
                text = { Text("App will restart to load your data.") },
                confirmButton = {
                    // Logic to restart app same as LoginScreen
                    val context = androidx.compose.ui.platform.LocalContext.current
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(1000)
                        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                        intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        if (intent != null) {
                            context.startActivity(intent)
                            android.os.Process.killProcess(android.os.Process.myPid())
                        }
                    }
                }
            )
        }
        is SettingsViewModel.SettingsUiState.CheckingBackup -> {
             AlertDialog(
                onDismissRequest = {},
                title = { Text("Checking Backups...") },
                text = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Connecting to Google Drive")
                    }
                },
                confirmButton = {}
            )
        }
        is SettingsViewModel.SettingsUiState.BackupNotFound -> {
             AlertDialog(
                onDismissRequest = { settingsViewModel.resetState() },
                title = { Text("No Backup Found") },
                text = { Text("We couldn't find any JarWise backups in your Google Drive.") },
                confirmButton = {
                    TextButton(onClick = { settingsViewModel.resetState() }) {
                        Text("OK")
                    }
                }
            )
        }
        else -> {}
    }
    
    // Listen for Restart Event (Delete Data)
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        settingsViewModel.restartAppEvent.collect {
            kotlinx.coroutines.delay(500) // Small buffer
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            if (intent != null) {
                context.startActivity(intent)
                android.os.Process.killProcess(android.os.Process.myPid())
            }
        }
    }
    
    val currencies = listOf("THB", "USD", "EUR", "JPY")

    if (showLogoutDialog) {
        var deleteData by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out") },
            text = { 
                Column(horizontalAlignment = Alignment.Start) {
                    Text("Are you sure you want to sign out?")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { deleteData = !deleteData }
                    ) {
                        Checkbox(
                            checked = deleteData,
                            onCheckedChange = { deleteData = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Also delete local data (start fresh)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        settingsViewModel.signOut(clearData = deleteData)
                        showLogoutDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // --- Profile Section ---
            if (currentUser != null) {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = currentUser!!.photoUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            fallback = rememberVectorPainter(Icons.Rounded.AccountCircle)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = currentUser!!.name, style = MaterialTheme.typography.titleMedium)
                            Text(text = currentUser!!.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                // Not logged in state
                
                // Activity Result Launcher for Google Sign-In
                val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == android.app.Activity.RESULT_OK) {
                        settingsViewModel.handleSignInResult(result.data)
                    }
                }

                Text(
                    text = "Account",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Button(
                    onClick = {
                        val intent = settingsViewModel.getSignInIntent()
                        if (intent.action != null || intent.component != null) {
                            launcher.launch(intent)
                        } else {
                            // Mock
                            settingsViewModel.handleSignInResult(null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Icon(Icons.Rounded.AccountCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign In with Google")
                }
            }

            // --- Backup & Sync Section ---
            if (currentUser != null) {
                Text(
                    text = "Backup & Sync",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            val (icon, tint, message) = when (val status = syncStatus) {
                                is SyncStatus.Idle -> Triple(Icons.Rounded.CloudDone, MaterialTheme.colorScheme.primary, "Up to date")
                                is SyncStatus.Syncing -> Triple(Icons.Rounded.Sync, MaterialTheme.colorScheme.primary, "Syncing...")
                                is SyncStatus.Success -> {
                                    val date = java.text.SimpleDateFormat("dd MMM HH:mm", java.util.Locale.getDefault()).format(java.util.Date(status.lastSyncedTime))
                                    Triple(Icons.Rounded.CloudDone, MaterialTheme.colorScheme.primary, "Last synced: $date")
                                }
                                is SyncStatus.Error -> Triple(Icons.Rounded.Error, MaterialTheme.colorScheme.error, "Error: ${status.message}")
                            }
                            
                            Icon(imageVector = icon, contentDescription = null, tint = tint)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = message, style = MaterialTheme.typography.bodyMedium)
                        }
                        
                        Button(
                            onClick = { settingsViewModel.triggerBackup() },
                            enabled = syncStatus !is SyncStatus.Syncing,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Back up now")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedButton(
                            onClick = { settingsViewModel.checkForBackups() },
                            enabled = syncStatus !is SyncStatus.Syncing,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Restore from Server")
                        }
                    }
                }
            }

            // --- General Section ---
            Text(
                text = "General",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedButton(
                onClick = onNavigateToManageWallets,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null)
                    Text("Manage Wallets (Sub-accounts)")
                    }
            }
            
            OutlinedButton(
                onClick = onNavigateToMigration,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                 Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.CloudDone, contentDescription = null) // Using CloudDone as a placeholder if Import icon not available
                    Text("Migrate from Money Manager")
                }
            }


            Text(
                text = "Currency",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(modifier = Modifier.padding(bottom = 24.dp)) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "$selectedCurrency (${TransactionDisplayUtils.formatCurrency(0.0, selectedCurrency).first()})")
                }
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    currencies.forEach { currency ->
                        DropdownMenuItem(
                            text = { 
                                val symbol = TransactionDisplayUtils.formatCurrency(0.0, currency).first()
                                Text("$currency ($symbol)") 
                            },
                            onClick = {
                                viewModel.updateCurrency(currency)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // --- Sign Out ---
            if (currentUser != null) {
                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Rounded.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out")
                }
            }
        }
    }
}
