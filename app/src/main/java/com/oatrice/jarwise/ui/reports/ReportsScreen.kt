package com.oatrice.jarwise.ui.reports

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import com.oatrice.jarwise.ui.theme.Gray900
import com.oatrice.jarwise.ui.theme.Gray800
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.layout.fullWidth
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.oatrice.jarwise.ui.components.BottomNav
import com.oatrice.jarwise.ui.components.NavPage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onBack: () -> Unit,
    onNavigate: (NavPage) -> Unit,
    viewModel: ReportsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var selectedRange by remember { mutableStateOf("month") }
    
    // Custom range state
    var showDatePicker by remember { mutableStateOf(false) }
    var customStartDate by remember { mutableStateOf<String?>(null) }
    var customEndDate by remember { mutableStateOf<String?>(null) }

    if (showDatePicker) {
        val dateRangePickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val start = dateRangePickerState.selectedStartDateMillis
                    val end = dateRangePickerState.selectedEndDateMillis
                    if (start != null && end != null) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                        customStartDate = sdf.format(Date(start))
                        customEndDate = sdf.format(Date(end))
                        viewModel.fetchReport("custom", customStartDate, customEndDate)
                        selectedRange = "custom"
                    }
                    showDatePicker = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                modifier = Modifier.padding(16.dp).height(500.dp),
                title = { Text("Select Date Range", modifier = Modifier.padding(16.dp)) }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Financial Reports", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        val context = LocalContext.current
                        var csvBytes by remember { mutableStateOf<ByteArray?>(null) }
                        
                        val launcher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.CreateDocument("text/csv")
                        ) { uri ->
                            uri?.let {
                                context.contentResolver.openOutputStream(it)?.use { stream ->
                                    csvBytes?.let { bytes -> stream.write(bytes) }
                                }
                            }
                        }

                        IconButton(
                            onClick = {
                                viewModel.exportReport(selectedRange, customStartDate, customEndDate) { bytes ->
                                    csvBytes = bytes
                                    launcher.launch("jarwise-export-${selectedRange}.csv")
                                }
                            },
                            enabled = !uiState.isLoading
                        ) {
                            Icon(Icons.Rounded.FileDownload, contentDescription = "Export CSV", tint = Color.White)
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
                    .padding(bottom = 80.dp)
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Date Range Picker
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Gray900, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val ranges = listOf(
                        "month" to "Month", 
                        "quarter" to "Quarter", 
                        "year" to "Year",
                        "all" to "All",
                        "custom" to "Custom"
                    )
                    ranges.forEach { (key, label) ->
                        FilterChip(
                            selected = selectedRange == key,
                            onClick = { 
                                if (key == "custom") {
                                    showDatePicker = true
                                } else {
                                    selectedRange = key
                                    customStartDate = null
                                    customEndDate = null
                                    viewModel.fetchReport(key)
                                }
                            },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF6366F1),
                                selectedLabelColor = Color.White,
                                labelColor = Color.Gray
                            ),
                            border = null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF6366F1))
                    }
                } else {
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
                            color = Color(0xFF10B981)
                        )
                        SummaryCard(
                            modifier = Modifier.weight(1f),
                            title = "Expense",
                            amount = uiState.expense,
                            icon = Icons.Rounded.TrendingDown,
                            color = Color(0xFFF43F5E)
                        )
                        SummaryCard(
                            modifier = Modifier.weight(1f),
                            title = "Net",
                            amount = uiState.net,
                            icon = Icons.Rounded.Wallet,
                            color = Color(0xFF6366F1)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Trend Chart
                    ChartSection(title = "📈 Income vs Expense Trends") {
                        Chart(
                            chart = lineChart(),
                            chartModelProducer = uiState.trendData,
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(),
                            isZoomEnabled = false,
                            horizontalLayout = HorizontalLayout.fullWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Category Breakdown
                    ChartSection(title = "📊 Category Breakdown") {
                        Chart(
                            chart = columnChart(),
                            chartModelProducer = uiState.categoryData,
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(),
                            isZoomEnabled = false,
                            horizontalLayout = HorizontalLayout.fullWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Jar Distribution
                    Text("🏺 Jar Distribution", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Gray900),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            uiState.jarData.forEach { jar ->
                                JarItem(jar)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Comparison (if available)
                    if (uiState.comparisonData.getModel()?.entries?.isNotEmpty() == true) {
                        ChartSection(title = "⚖️ vs Previous Period") {
                            Chart(
                                chart = columnChart(),
                                chartModelProducer = uiState.comparisonData,
                                startAxis = rememberStartAxis(),
                                bottomAxis = rememberBottomAxis(),
                                isZoomEnabled = false,
                                horizontalLayout = HorizontalLayout.fullWidth()
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        BottomNav(
            activePage = NavPage.REPORTS,
            visible = true,
            modifier = Modifier.align(Alignment.BottomCenter),
            onNavigate = onNavigate
        )
    }
}

@Composable
fun ChartSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Gray900),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.height(250.dp).fillMaxWidth()
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun JarItem(jar: JarDistribution) {
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
        Text(jar.name, color = Color.LightGray, modifier = Modifier.weight(1f), fontSize = 13.sp)
        Text(
            "฿${"%,.0f".format(jar.amount)}",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
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
