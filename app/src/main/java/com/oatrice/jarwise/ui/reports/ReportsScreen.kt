package com.oatrice.jarwise.ui.reports

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.toArgb
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.drawscope.Stroke
import java.text.NumberFormat
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
                    title = { Text("รายงานการเงิน", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "กลับ")
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
                        "7d" to "7 วัน",
                        "30d" to "30 วัน",
                        "month" to "เดือน", 
                        "quarter" to "ไตรมาส", 
                        "year" to "ปี",
                        "all" to "ทั้งหมด",
                        "custom" to "กำหนดเอง"
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
                                    
                                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                                    val nowStr = sdf.format(Date())
                                    
                                    when (key) {
                                        "7d" -> {
                                            val c = Calendar.getInstance()
                                            c.add(Calendar.DAY_OF_YEAR, -7)
                                            viewModel.fetchReport("custom", sdf.format(c.time), nowStr)
                                        }
                                        "30d" -> {
                                            val c = Calendar.getInstance()
                                            c.add(Calendar.DAY_OF_YEAR, -30)
                                            viewModel.fetchReport("custom", sdf.format(c.time), nowStr)
                                        }
                                        else -> viewModel.fetchReport(key)
                                    }
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

                if (selectedRange == "custom" && customStartDate != null && customEndDate != null) {
                    Text(
                        text = "Range: ${customStartDate!!.split("T")[0]} - ${customEndDate!!.split("T")[0]}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF6366F1).copy(alpha = 0.8f),
                        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF6366F1))
                    }
                } else {
                    // Summary Cards
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { 40 })
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SummaryCard(
                                modifier = Modifier.weight(1f),
                                title = "รายรับ",
                                amount = uiState.income,
                                icon = Icons.Rounded.TrendingUp,
                                color = Color(0xFF10B981)
                            )
                            SummaryCard(
                                modifier = Modifier.weight(1f),
                                title = "รายจ่าย",
                                amount = uiState.expense,
                                icon = Icons.Rounded.TrendingDown,
                                color = Color(0xFFF43F5E)
                            )
                            SummaryCard(
                                modifier = Modifier.weight(1f),
                                title = "คงเหลือ",
                                amount = uiState.net,
                                icon = Icons.Rounded.Wallet,
                                color = Color(0xFF6366F1)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Trend Chart
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(600)) + slideInVertically(initialOffsetY = { 60 })
                    ) {
                        ChartSection(title = "📈 แนวโน้มรายรับ-รายจ่าย", icon = Icons.Rounded.TrendingUp) {
                            Chart(
                                chart = lineChart(
                                    lines = listOf(
                                        com.patrykandpatrick.vico.core.chart.line.LineChart.LineSpec(
                                            lineColor = Color(0xFF10B981).toArgb(),
                                            areaBrush = com.patrykandpatrick.vico.core.component.shape.shader.ComponentShader(
                                                com.patrykandpatrick.vico.core.component.shape.ShapeComponent(
                                                    fill = com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders.fromBrush(
                                                        Brush.verticalGradient(listOf(Color(0xFF10B981).copy(alpha = 0.3f), Color.Transparent))
                                                    )
                                                )
                                            )
                                        ),
                                        com.patrykandpatrick.vico.core.chart.line.LineChart.LineSpec(
                                            lineColor = Color(0xFFF43F5E).toArgb(),
                                            areaBrush = com.patrykandpatrick.vico.core.component.shape.shader.ComponentShader(
                                                com.patrykandpatrick.vico.core.component.shape.ShapeComponent(
                                                    fill = com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders.fromBrush(
                                                        Brush.verticalGradient(listOf(Color(0xFFF43F5E).copy(alpha = 0.3f), Color.Transparent))
                                                    )
                                                )
                                            )
                                        )
                                    )
                                ),
                                chartModelProducer = uiState.trendData,
                                startAxis = rememberStartAxis(),
                                bottomAxis = rememberBottomAxis(),
                                isZoomEnabled = false,
                                horizontalLayout = HorizontalLayout.fullWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Income Breakdown
                    if (uiState.incomeBreakdownData.getModel()?.entries?.isNotEmpty() == true) {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(800)) + slideInVertically(initialOffsetY = { 80 })
                        ) {
                            ChartSection(title = "💰 วิเคราะห์รายรับตามหมวดหมู่", icon = Icons.Rounded.TrendingUp) {
                                Chart(
                                    chart = columnChart(
                                        columns = listOf(
                                            com.patrykandpatrick.vico.core.chart.column.ColumnChart.ColumnSpec(
                                                com.patrykandpatrick.vico.core.component.shape.LineComponent(
                                                    color = Color(0xFF10B981).toArgb(),
                                                    thicknessDp = 8f,
                                                    shape = com.patrykandpatrick.vico.core.component.shape.Shapes.roundedCornerShape(allPercent = 40)
                                                )
                                            ),
                                            com.patrykandpatrick.vico.core.chart.column.ColumnChart.ColumnSpec(
                                                com.patrykandpatrick.vico.core.component.shape.LineComponent(
                                                    color = Color(0xFF10B981).copy(alpha = 0.3f).toArgb(),
                                                    thicknessDp = 8f,
                                                    shape = com.patrykandpatrick.vico.core.component.shape.Shapes.roundedCornerShape(allPercent = 40)
                                                )
                                            )
                                        )
                                    ),
                                    chartModelProducer = uiState.incomeBreakdownData,
                                    startAxis = rememberStartAxis(),
                                    bottomAxis = rememberBottomAxis(),
                                    isZoomEnabled = false,
                                    horizontalLayout = HorizontalLayout.fullWidth()
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Income Distribution
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(900)) + slideInVertically(initialOffsetY = { 90 })
                        ) {
                            DistributionSection(title = "🏺 สัดส่วนรายรับ", items = uiState.incomeDistribution)
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Expense Breakdown
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(initialOffsetY = { 100 })
                    ) {
                        ChartSection(title = "📊 วิเคราะห์รายจ่ายตามหมวดหมู่", icon = Icons.Rounded.TrendingDown) {
                            Chart(
                                chart = columnChart(
                                    columns = listOf(
                                        com.patrykandpatrick.vico.core.chart.column.ColumnChart.ColumnSpec(
                                            com.patrykandpatrick.vico.core.component.shape.LineComponent(
                                                color = Color(0xFFF43F5E).toArgb(),
                                                thicknessDp = 8f,
                                                shape = com.patrykandpatrick.vico.core.component.shape.Shapes.roundedCornerShape(allPercent = 40)
                                            )
                                        ),
                                        com.patrykandpatrick.vico.core.chart.column.ColumnChart.ColumnSpec(
                                            com.patrykandpatrick.vico.core.component.shape.LineComponent(
                                                color = Color(0xFFF43F5E).copy(alpha = 0.3f).toArgb(),
                                                thicknessDp = 8f,
                                                shape = com.patrykandpatrick.vico.core.component.shape.Shapes.roundedCornerShape(allPercent = 40)
                                            )
                                        )
                                    )
                                ),
                                chartModelProducer = uiState.expenseBreakdownData,
                                startAxis = rememberStartAxis(),
                                bottomAxis = rememberBottomAxis(),
                                isZoomEnabled = false,
                                horizontalLayout = HorizontalLayout.fullWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Expense Distribution
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(1100)) + slideInVertically(initialOffsetY = { 110 })
                    ) {
                        DistributionSection(title = "🏺 สัดส่วนรายจ่าย", items = uiState.expenseDistribution)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Comparison (Overall)
                    if (uiState.comparisonData.getModel()?.entries?.isNotEmpty() == true) {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(1200)) + slideInVertically(initialOffsetY = { 120 })
                        ) {
                            ChartSection(title = "⚖️ เทียบกับช่วงก่อนหน้า", icon = Icons.Rounded.Wallet) {
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
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
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
fun ChartSection(title: String, icon: ImageVector? = null, content: @Composable () -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(title, style = MaterialTheme.typography.titleSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Gray900.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.height(260.dp).fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f))
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun DistributionSection(title: String, items: List<JarDistribution>) {
    Column {
        Text(title, style = MaterialTheme.typography.titleSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Gray900.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Modified Pie Chart using simple Canvas
                Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val total = items.sumOf { it.amount }.toFloat()
                        var startAngle = -90f
                        items.forEach { item ->
                            val sweepAngle = (item.amount.toFloat() / total) * 360f
                            drawArc(
                                color = Color(item.color),
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = 30f)
                            )
                            startAngle += sweepAngle
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("รวม", fontSize = 10.sp, color = Color.Gray)
                        val total = items.sumOf { it.amount }
                        Text(
                            total.toFormattedSmall(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(20.dp))
                
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items.take(5).forEach { item ->
                        JarItem(item)
                    }
                }
            }
        }
    }
}

@Composable
fun JarItem(jar: JarDistribution) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(jar.color))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            jar.name, 
            color = Color.LightGray, 
            modifier = Modifier.weight(1f), 
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "฿${"%,.2f".format(jar.amount)}",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
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
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.15f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Subtle Gradient Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(color.copy(alpha = 0.1f), Color.Transparent),
                            start = androidx.compose.ui.geometry.Offset.Zero,
                            end = androidx.compose.ui.geometry.Offset.Infinite
                        )
                    )
            )
            
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "฿${"%,.2f".format(amount)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    amount.toFormattedSmall(),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = Color.Gray.copy(alpha = 0.6f)
                )
            }
        }
    }
}

fun Double.toFormattedSmall(): String {
    return if (this >= 1_000_000) {
        "฿${"%.1f".format(this / 1_000_000)}M"
    } else if (this >= 1_000) {
        "฿${"%.1f".format(this / 1_000)}k"
    } else {
        "฿${"%.0f".format(this)}"
    }
}
