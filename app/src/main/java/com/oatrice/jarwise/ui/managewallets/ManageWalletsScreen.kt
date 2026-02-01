package com.oatrice.jarwise.ui.managewallets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.SubdirectoryArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// import androidx.lifecycle.viewmodel.compose.viewModel // Unused
import com.oatrice.jarwise.model.Wallet
import com.oatrice.jarwise.ui.theme.*
import androidx.compose.material.icons.rounded.Wallet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageWalletsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ManageWalletsViewModel
) {
    val wallets by viewModel.wallets.collectAsState()
    val uiEvent by viewModel.uiEvent.collectAsState()
    
    // Handle UI Events (Error Toasts/Snackbars)
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiEvent) {
        uiEvent?.let { event ->
            when (event) {
                is ManageWalletsViewModel.UiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                    viewModel.clearEvent()
                }
            }
        }
    }

    // Organize wallets into tree order for display
    // flattenTree returns a list where children follow their parents immediately
    val displayList = remember(wallets) { flattenTree(wallets) }

    // State for Add/Edit Modal
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingWallet by remember { mutableStateOf<Wallet?>(null) }

    if (showAddEditDialog) {
        AddEditWalletDialog(
            onDismiss = { showAddEditDialog = false },
            onSave = { name, parentId ->
                if (editingWallet == null) {
                    // Add New
                    viewModel.addWallet(
                        Wallet(
                            id = "", // ID generated in VM
                            name = name,
                            balance = 0.0, // Default balance
                            color = Blue500, // Default color
                            icon = Icons.Rounded.Wallet, // Default icon
                            parentId = parentId,
                            level = 0 // Calculated in VM
                        )
                    )
                } else {
                    // Update Existing
                    viewModel.updateWallet(
                        editingWallet!!.copy(
                            name = name,
                            parentId = parentId
                        )
                    )
                }
                showAddEditDialog = false
            },
            allWallets = wallets,
            editingWallet = editingWallet
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Wallets", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Gray950)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    editingWallet = null
                    showAddEditDialog = true 
                },
                containerColor = Blue500,
                contentColor = Color.White
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Wallet")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Gray950
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(displayList) { wallet ->
                WalletTreeItem(
                    wallet = wallet,
                    onDelete = { viewModel.deleteWallet(wallet.id) },
                    onEdit = { 
                        editingWallet = wallet
                        showAddEditDialog = true
                    }
                )
            }
        }
    }
}

@Composable
fun WalletTreeItem(
    wallet: Wallet,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    // Indentation based on level
    val indent = (wallet.level * 24).dp 

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min) 
    ) {
        // Visual Connector Lines (Simplified: Just spacing for now, or vertical line)
        if (wallet.level > 0) {
            // Draw Spacer
            Spacer(modifier = Modifier.width(indent))
            
            // Draw visual indicator "L" shape
            Icon(
                imageVector = Icons.Rounded.SubdirectoryArrowRight,
                contentDescription = null,
                tint = Gray500,
                modifier = Modifier
                    .padding(top = 18.dp, end = 8.dp)
                    .size(16.dp)
            )
        }

        // Wallet Card
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onEdit),
            colors = CardDefaults.cardColors(containerColor = Gray900),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Gray800)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(wallet.color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = wallet.icon,
                        contentDescription = null,
                        tint = wallet.color
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Text Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = wallet.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (wallet.level > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sub",
                                style = MaterialTheme.typography.labelSmall,
                                color = Gray500,
                                modifier = Modifier
                                    .background(Gray800, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "$${wallet.balance}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray400
                    )
                }

                // Actions (Delete only for now)
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Delete",
                        tint = Gray500,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// Recursive function to flatten the tree for lazy list display
fun flattenTree(allWallets: List<Wallet>): List<Wallet> {
    val result = mutableListOf<Wallet>()
    val rootWallets = allWallets.filter { it.parentId == null }

    fun addNode(wallet: Wallet) {
        result.add(wallet)
        val children = allWallets.filter { it.parentId == wallet.id }
        children.forEach { addNode(it) } // Depth-first traversal guarantees hierarchy order
    }

    rootWallets.forEach { addNode(it) }
    
    // Safety fallback: Add any orphans that might exist due to bugs (though specific logic shouldn't allow it)
    val processedIds = result.map { it.id }.toSet()
    val orphans = allWallets.filter { !processedIds.contains(it.id) }
    result.addAll(orphans)

    return result
}
