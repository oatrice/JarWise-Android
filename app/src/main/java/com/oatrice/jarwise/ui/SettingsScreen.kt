package com.oatrice.jarwise.ui

import androidx.compose.foundation.layout.*
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
    viewModel: MainViewModel,
    settingsViewModel: SettingsViewModel = koinViewModel()
) {
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val currentUser by settingsViewModel.user.collectAsState()
    val syncStatus by settingsViewModel.syncStatus.collectAsState()
    
    var expanded by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    val currencies = listOf("THB", "USD", "EUR", "JPY")

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        settingsViewModel.signOut()
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
