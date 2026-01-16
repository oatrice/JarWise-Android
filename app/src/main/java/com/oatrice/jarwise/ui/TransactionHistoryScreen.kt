package com.oatrice.jarwise.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oatrice.jarwise.data.GeneratedMockData
import com.oatrice.jarwise.ui.components.TransactionCard
import com.oatrice.jarwise.ui.theme.*
import kotlin.math.abs

/**
 * Transaction History Screen
 * Shows all transactions grouped by date
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    onBack: () -> Unit
) {
    val transactions = GeneratedMockData.transactions
    val totalSpent = transactions.sumOf { it.amount }
    
    // Group transactions by date
    val todayTransactions = transactions.filter { it.date.contains("Today") }
    val yesterdayTransactions = transactions.filter { it.date.contains("Yesterday") }
    val olderTransactions = transactions.filter { 
        !it.date.contains("Today") && !it.date.contains("Yesterday") 
    }

    Scaffold(
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
                    IconButton(onClick = { /* Filter */ }) {
                        Icon(
                            Icons.Rounded.FilterList,
                            contentDescription = "Filter",
                            tint = Gray400
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Gray950.copy(alpha = 0.8f)
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Card
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SummaryCard(
                    totalSpent = totalSpent,
                    transactionCount = transactions.size
                )
            }

            // Today's Transactions
            if (todayTransactions.isNotEmpty()) {
                item {
                    Text(
                        "Today",
                        color = Gray500,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(todayTransactions) { transaction ->
                    TransactionCard(transaction = transaction)
                }
            }

            // Yesterday's Transactions
            if (yesterdayTransactions.isNotEmpty()) {
                item {
                    Text(
                        "Yesterday",
                        color = Gray500,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(yesterdayTransactions) { transaction ->
                    TransactionCard(transaction = transaction)
                }
            }

            // Older Transactions
            if (olderTransactions.isNotEmpty()) {
                item {
                    Text(
                        "Older",
                        color = Gray500,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(olderTransactions) { transaction ->
                    TransactionCard(transaction = transaction)
                }
            }

            // Bottom Spacer
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun SummaryCard(
    totalSpent: Double,
    transactionCount: Int
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
                        "-$${String.format("%,.2f", abs(totalSpent))}",
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
    TransactionHistoryScreen(onBack = {})
}
