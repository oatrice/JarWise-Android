package com.oatrice.jarwise.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oatrice.jarwise.ui.theme.Gray900
import com.oatrice.jarwise.ui.theme.Gray800
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.oatrice.jarwise.ui.components.BottomNav
import com.oatrice.jarwise.ui.components.NavPage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onBack: () -> Unit,
    onNavigate: (NavPage) -> Unit,
    viewModel: ReportsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Reports", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Gray900,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            containerColor = Color.Black
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 100.dp) // Extra padding for BottomNav
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Date Range (Mock)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Gray900, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(selected = true, onClick = {}, label = { Text("Month") })
                    FilterChip(selected = false, onClick = {}, label = { Text("Quarter") })
                    FilterChip(selected = false, onClick = {}, label = { Text("Year") })
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Summary Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        title = "Income",
                        amount = uiState.income,
                        icon = Icons.Rounded.TrendingUp,
                        color = Color(0xFF10B981) // Emerald
                    )
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        title = "Expense",
                        amount = uiState.expense,
                        icon = Icons.Rounded.TrendingDown,
                        color = Color(0xFFF43F5E) // Rose
                    )
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        title = "Net",
                        amount = uiState.net,
                        icon = Icons.Rounded.Wallet,
                        color = Color(0xFF6366F1) // Indigo
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Trend Chart
                Text("Spending Trends", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Gray900),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(250.dp).fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Chart(
                            chart = lineChart(),
                            chartModelProducer = uiState.trendData,
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(),
                            isZoomEnabled = false,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Category Chart
                Text("Category Breakdown", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Gray900),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(250.dp).fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Chart(
                            chart = columnChart(),
                            chartModelProducer = uiState.categoryData,
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(),
                            isZoomEnabled = false,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Jar Distribution (List for now)
                Text("Jar Distribution", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Gray900),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        uiState.jarData.forEach { jar ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(jar.color))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(jar.name, color = Color.LightGray, modifier = Modifier.weight(1f))
                                Text(
                                    "฿${"%,.0f".format(jar.amount)}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // BottomNav fixed at bottom
        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BottomNav(
                activePage = NavPage.REPORTS,
                visible = true,
                onNavigate = onNavigate
            )
        }
    }
}

@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: Double,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(color.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(
                "฿${"%.1f".format(amount / 1000)}k",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
