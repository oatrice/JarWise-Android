package com.oatrice.jarwise.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oatrice.jarwise.data.Transaction
import com.oatrice.jarwise.ui.components.TransactionCard
import com.oatrice.jarwise.ui.theme.Blue400
import com.oatrice.jarwise.ui.theme.Gray400
import com.oatrice.jarwise.ui.theme.Gray500
import com.oatrice.jarwise.ui.theme.Gray800
import com.oatrice.jarwise.ui.theme.Gray950
import com.oatrice.jarwise.ui.theme.JarWiseTheme
import com.oatrice.jarwise.ui.theme.Red400
import com.oatrice.jarwise.utils.TransactionDisplayUtils
import com.oatrice.jarwise.utils.TransactionGroupingUtils
import com.oatrice.jarwise.ui.theme.Green400
import com.oatrice.jarwise.ui.components.BottomNav
import com.oatrice.jarwise.ui.components.NavPage
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.ui.input.nestedscroll.nestedScroll
import kotlin.math.abs
import com.oatrice.jarwise.ui.reportfilter.ReportFilterSheet
import com.oatrice.jarwise.ui.reportfilter.ReportFilterViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Transaction History Screen
 * Shows all transactions grouped by date
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    transactions: List<Transaction> = emptyList(),
    selectedCurrency: String = "THB",
    onBack: () -> Unit,
    onNavigate: (NavPage) -> Unit = {}
) {
    val reportFilterViewModel: ReportFilterViewModel = koinViewModel()
    var showFilters by remember { mutableStateOf(false) }
    var activeJarFilters by remember { mutableStateOf<Set<String>>(emptySet()) }
    var activeWalletFilters by remember { mutableStateOf<Set<String>>(emptySet()) }

    val hasActiveFilters = activeJarFilters.isNotEmpty() || activeWalletFilters.isNotEmpty()

    LaunchedEffect(showFilters) {
        if (showFilters) {
            reportFilterViewModel.setSelections(activeJarFilters, activeWalletFilters)
        }
    }

    val filteredTransactions = remember(transactions, activeJarFilters, activeWalletFilters) {
        if (!hasActiveFilters) {
            transactions
        } else {
            transactions.filter { tx ->
                val jarMatch = activeJarFilters.isEmpty() || activeJarFilters.contains(tx.jarId)
                val walletMatch = activeWalletFilters.isEmpty() || activeWalletFilters.contains(tx.walletId)
                jarMatch && walletMatch
            }
        }
    }

    // Fix: Only sum expenses and exclude transfers for "Total Spent"
    val totalSpent = remember(filteredTransactions) {
        filteredTransactions
            .filter { it.type == "expense" && it.linkedTransactionId == null }
            .sumOf { it.amount }
    }
    
    // Create lookup map for O(1) access
    val transactionsById = remember(transactions) {
        transactions.associateBy { it.id.toString() }
    }
    
    // Scroll behavior for TopAppBar hide/show
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val lazyListState = rememberLazyListState()
    
    // Track if header should be visible based on scroll
    val isHeaderVisible = remember {
        derivedStateOf {
            scrollBehavior.state.collapsedFraction < 0.5f
        }
    }

    // Wrap in Box to overlay BottomNav correctly
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = Gray950,
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "Transaction History",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = Gray400
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Search */ }) {
                            Icon(
                                Icons.Rounded.Search,
                                contentDescription = "Search",
                                tint = Gray400
                            )
                        }
                        IconButton(onClick = { showFilters = true }) {
                            Icon(
                                Icons.Rounded.FilterList,
                                contentDescription = "Filter",
                                tint = if (hasActiveFilters) Blue400 else Gray400
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Gray950.copy(alpha = 0.8f)
                    ),
                    scrollBehavior = scrollBehavior
                )
            }
        ) { paddingValues ->
            // Group transactions by date
            val groupedTransactions = TransactionGroupingUtils.groupByDate(filteredTransactions)
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Summary Card
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SummaryCard(
                            totalSpent = totalSpent,
                            transactionCount = filteredTransactions.size,
                            currencyCode = selectedCurrency
                        )
                    }

                    // Grouped Transactions
                    groupedTransactions.forEach { group ->
                        // Date Header with Daily Totals
                        item {
                            DailyHeader(
                                dateHeader = group.dateHeader,
                                totalIncome = group.totalIncome,
                                totalExpense = group.totalExpense,
                                currencyCode = selectedCurrency
                            )
                        }
                        
                        // Transactions for this day
                        items(group.transactions) { transaction ->
                            // Optimized O(1) lookup
                            val linkedTransaction = transaction.linkedTransactionId?.let { linkedId ->
                                transactionsById[linkedId]
                            }
                            TransactionCard(
                                transaction = transaction,
                                linkedTransaction = linkedTransaction,
                                currencyCode = selectedCurrency,
                                showDate = false
                            )
                        }
                    }

                    if (filteredTransactions.isEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            EmptyState(
                                title = if (hasActiveFilters) "No matches found" else "No transactions yet",
                                subtitle = if (hasActiveFilters) "Try adjusting your filters." else "Your transaction history will appear here."
                            )
                        }
                    }

                    // Bottom Spacer for BottomNav
                    item {
                        Spacer(modifier = Modifier.height(120.dp))
                    }
                }
            }
        }
        
        // BottomNav aligned to screen bottom (outside Scaffold padding)
        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BottomNav(
                activePage = NavPage.HISTORY,
                visible = isHeaderVisible.value,
                onNavigate = onNavigate
            )
        }

        if (showFilters) {
            ReportFilterSheet(
                viewModel = reportFilterViewModel,
                onApply = { jarIds, walletIds ->
                    activeJarFilters = jarIds
                    activeWalletFilters = walletIds
                    showFilters = false
                },
                onDismiss = {
                    reportFilterViewModel.setSelections(activeJarFilters, activeWalletFilters)
                    showFilters = false
                }
            )
        }
    }
}

@Composable
private fun DailyHeader(
    dateHeader: String,
    totalIncome: Double,
    totalExpense: Double,
    currencyCode: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateHeader,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (totalIncome > 0) {
                Text(
                    text = "+" + TransactionDisplayUtils.formatCurrency(totalIncome, currencyCode),
                    color = Green400,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            if (totalExpense < 0) {
                Text(
                    text = TransactionDisplayUtils.formatCurrency(totalExpense, currencyCode),
                    color = Red400,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    totalSpent: Double,
    transactionCount: Int,
    currencyCode: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Gray800.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Gray800.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Rounded.CalendarMonth,
                        contentDescription = null,
                        tint = Gray400,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "This Month",
                        color = Gray400,
                        fontSize = 14.sp
                    )
                }
                Text(
                    "Change Period",
                    color = Blue400,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { /* Change period */ }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Total Spent",
                        color = Gray500,
                        fontSize = 12.sp
                    )
                    Text(
                        "-" + TransactionDisplayUtils.formatCurrency(abs(totalSpent), currencyCode),
                        color = Red400,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Transactions",
                        color = Gray500,
                        fontSize = 12.sp
                    )
                    Text(
                        transactionCount.toString(),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = Gray500,
            modifier = Modifier.size(40.dp)
        )
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = subtitle,
            color = Gray500,
            fontSize = 13.sp
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun TransactionHistoryScreenPreview() {
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
        ),
        Transaction(
            id = 3,
            amount = 120.00,
            jarId = "necessities",
            note = "Water Bill",
            date = "2024-05-18T09:15:00.000Z"
        )
    )

    JarWiseTheme {
        TransactionHistoryScreen(
            transactions = mockTransactions,
            selectedCurrency = "THB",
            onBack = {}
        )
    }
}
