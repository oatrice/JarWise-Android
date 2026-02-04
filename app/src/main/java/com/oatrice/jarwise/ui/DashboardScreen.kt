package com.oatrice.jarwise.ui

/**
 * The primary dashboard screen of the JarWise application.
 * Matches the "MagicPatterns" aesthetic from the Web Mobile version.
 */
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.oatrice.jarwise.data.GeneratedMockData
import com.oatrice.jarwise.data.Transaction
import com.oatrice.jarwise.model.Jar
import com.oatrice.jarwise.ui.components.JarCard
import com.oatrice.jarwise.ui.components.TransactionCard
import com.oatrice.jarwise.ui.theme.Blue400
import com.oatrice.jarwise.ui.theme.Blue500
import com.oatrice.jarwise.ui.theme.GradientBlueToCyan
import com.oatrice.jarwise.ui.theme.Gray100
import com.oatrice.jarwise.ui.theme.Gray400
import com.oatrice.jarwise.ui.theme.Gray500
import com.oatrice.jarwise.ui.theme.Gray800
import com.oatrice.jarwise.ui.theme.Gray900
import com.oatrice.jarwise.ui.theme.Gray950
import com.oatrice.jarwise.ui.theme.JarWiseTheme
import com.oatrice.jarwise.ui.theme.Orange400
import com.oatrice.jarwise.ui.theme.Orange500
import com.oatrice.jarwise.ui.components.BottomNav
import com.oatrice.jarwise.ui.components.NavPage
import com.oatrice.jarwise.ui.components.DashboardTopBar
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import com.oatrice.jarwise.ui.utils.rememberScrollVisibilityState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    jars: List<Jar> = GeneratedMockData.jars,
    transactions: List<Transaction> = emptyList(),
    formattedTotalBalance: String = "...",
    selectedCurrency: String = "THB",
    onNavigateToHistory: () -> Unit = {},
    onNavigateToScan: () -> Unit = {},
    onNavigateToImport: () -> Unit = {},
    onNavigateToAdd: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToManageJars: () -> Unit = {},
    onNavigate: (NavPage) -> Unit = {}
) {
    // Scroll state for visibility tracking
    val lazyListState = rememberLazyListState()
    val scrollState = rememberScrollVisibilityState(lazyListState)
    
    // Listen to scroll changes
    LaunchedEffect(lazyListState) {
        snapshotFlow { 
            lazyListState.firstVisibleItemIndex to lazyListState.firstVisibleItemScrollOffset 
        }
        .collect {
            scrollState.updateScrollVisibility()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Gray950, // Dark background
        ) { paddingValues ->
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 100.dp, bottom = 120.dp) // Extra padding for TopBar & Floating Nav
            ) {
                // HEADER SECTION (Sticky-ish visuals, but scrolls with content for Balance)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        // Balance & Streak (Scrolls with content)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "Total Balance",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = Gray400,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = formattedTotalBalance,
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 36.sp
                                    )
                                )
                            }

                            // Streak Pill
                            Surface(
                                shape = RoundedCornerShape(100),
                                color = Orange500.copy(alpha = 0.1f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Orange500.copy(alpha = 0.2f)),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.LocalFireDepartment,
                                        contentDescription = "Streak",
                                        tint = Orange500,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "5 Day Streak!",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            color = Orange400,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // JARS SECTION
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Your Jars", style = MaterialTheme.typography.titleMedium.copy(color = Gray100))
                        TextButton(onClick = onNavigateToManageJars) {
                            Text("Manage", color = Blue500)
                        }
                    }
                }
                
                itemsIndexed(jars) { index, jar ->
                    JarCard(jar = jar, isPriority = index == 0, currencyCode = selectedCurrency)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // TRANSACTIONS SECTION
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Recent Activity", style = MaterialTheme.typography.titleMedium.copy(color = Gray100))
                    }
                }

                if (transactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ReceiptLong,
                                    contentDescription = null,
                                    tint = Gray800,
                                    modifier = Modifier.size(64.dp)
                                )
                                Text(
                                    text = "No transactions yet",
                                    style = MaterialTheme.typography.bodyLarge.copy(color = Gray500)
                                )
                                Button(
                                    onClick = onNavigateToAdd,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Blue500.copy(alpha = 0.1f),
                                        contentColor = Blue400
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Blue500.copy(alpha = 0.3f))
                                ) {
                                    Text("Add First Transaction")
                                }
                            }
                        }
                    }
                } else {
                    // Filter out the income side of transfers (if not already done upstream)
                    val visibleTransactions = transactions
                        .filter { tx -> !(tx.type == "income" && tx.linkedTransactionId != null) }
                        .take(3)
                    
                    itemsIndexed(visibleTransactions) { _, transaction ->
                        // Lookup linked transaction for transfer display
                        val linkedTransaction = transaction.linkedTransactionId?.let { linkedId ->
                            transactions.find { it.id.toString() == linkedId }
                        }
                        TransactionCard(
                            transaction = transaction,
                            linkedTransaction = linkedTransaction,
                            currencyCode = selectedCurrency,
                            showDate = true
                        )
                    }
                }
            }
        }
        
        // Top Bar
        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            DashboardTopBar(
                visible = scrollState.isVisible,
                onScanClick = onNavigateToScan,
                onImportClick = onNavigateToImport,
                onSettingsClick = onNavigateToSettings
            )
        }
        
        // BottomNav with scroll visibility
        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BottomNav(
                activePage = NavPage.DASHBOARD,
                visible = scrollState.isVisible,
                onNavigate = { page ->
                    when (page) {
                        NavPage.HISTORY -> onNavigateToHistory()
                        NavPage.ADD -> onNavigateToAdd()
                        else -> onNavigate(page)
                    }
                }
            )
        }
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit = {}
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .background(Gray900, CircleShape)
            .border(1.dp, Gray800, CircleShape)
    ) {
        Icon(icon, contentDescription, tint = Gray400, modifier = Modifier.size(20.dp))
    }
}



@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    val mockTransactions = listOf(
        Transaction(
            id = 1,
            amount = 1250.00,
            jarId = "necessities",
            note = "Groceries from Lotus",
            date = "2024-05-20T10:30:00.000Z"
        ),
        Transaction(
            id = 2,
            amount = 500.00,
            jarId = "play",
            note = "Movie tickets",
            date = "2024-05-21T18:00:00.000Z"
        )
    )

    JarWiseTheme {
        DashboardScreen(
            jars = GeneratedMockData.jars.take(2),
            transactions = mockTransactions
        )
    }
}
