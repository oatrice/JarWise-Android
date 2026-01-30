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
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.ui.input.nestedscroll.nestedScroll
import kotlin.math.abs

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
    val totalSpent = transactions.sumOf { it.amount }
    
    // Scroll behavior for TopAppBar hide/show
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val lazyListState = rememberLazyListState()
    
    // Track if header should be visible based on scroll
    val isHeaderVisible = remember {
        derivedStateOf {
            scrollBehavior.state.collapsedFraction < 0.5f
        }
    }

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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Gray950.copy(alpha = 0.8f)
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        // Group transactions by date
        val groupedTransactions = TransactionGroupingUtils.groupByDate(transactions)
        
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
                        transactionCount = transactions.size,
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
                        TransactionCard(
                            transaction = transaction,
                            currencyCode = selectedCurrency,
                            showDate = false
                        )
                    }
                }

                // Bottom Spacer for BottomNav
                item {
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
            
            // BottomNav
            Box(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                BottomNav(
                    activePage = NavPage.HISTORY,
                    visible = isHeaderVisible.value,
                    onNavigate = onNavigate
                )
            }
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
