package com.oatrice.jarwise.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oatrice.jarwise.utils.TransactionDisplayUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToManageWallets: () -> Unit = {},
    viewModel: MainViewModel
) {
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val currencies = listOf("THB", "USD", "EUR", "JPY")

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
            Text(
                text = "General",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedButton(
                onClick = onNavigateToManageWallets,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
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

            Box {
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
        }
    }
}
