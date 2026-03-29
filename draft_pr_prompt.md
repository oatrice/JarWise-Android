# PR Draft Prompt

You are an AI assistant helping to create a Pull Request description.
    
TASK: [Web | Android] Financial Reports & Data Export
ISSUE: {
  "title": "[Web | Android] Financial Reports & Data Export",
  "number": 59,
  "body": "# \ud83c\udfaf Objective\nImplement comprehensive financial reporting with charts, graphs, and data export capabilities.\n\n## \ud83e\udde0 AI Brain Context\n- [task.md](https://raw.githubusercontent.com/oatrice/JarWise-Android/feat/59-financial-reports-export/docs/features/59_issue-59/ai_brain/task.md)\n- [walkthrough.md](https://raw.githubusercontent.com/oatrice/JarWise-Android/feat/59-financial-reports-export/docs/features/59_issue-59/ai_brain/walkthrough.md)\n- [implementation_plan.md](https://raw.githubusercontent.com/oatrice/JarWise-Android/feat/59-financial-reports-export/docs/features/59_issue-59/ai_brain/implementation_plan.md)\n\n\nCloses #59",
  "url": "https://github.com/oatrice/JarWise-Root/issues/59"
}

GIT CONTEXT:
COMMITS:
09bd185 docs: sync AI brain artifacts
d7f70fa ✨ feat(reports): Enhance financial reporting features
413cf60 ✨ feat(reports): Enhance error handling and date range options
2b03bb8 feat(reports): add chart legends for income, expense, and comparison charts
61744c4 refactor(ui): use AxisValuesOverrider for chart axes
b0fbbb7 ✨ feat(reports): Improve chart axis scaling
9f96048 ✨ feat(reports): Enhance chart axis formatting
8ac0288 fix(reports): remove gradient brush from chart axis lines
2ea2a5b refactor: update chart axis styling for improved readability and add pie chart colors
a0abdba refactor: update vico chart implementation and simplify report filters
0cda76d ✨ feat(reports): Enhance financial reporting UI and data
4c4163f feat(reports): add 7-day and 30-day time range options and custom range display
3015c16 ✨ feat(reports): Add custom date range filtering
ddc790e refactor(reports): Replace ViewModel with Koin injection
db3e4eb feat: add modifier parameter to BottomNav component
2647d6a ✨ feat(reports): Add CSV export functionality
3ca5fc9 ✨ feat(reports): Implement financial reporting feature

STATS:
CHANGELOG.md                                       |  19 +
 app/build.gradle.kts                               |   2 +-
 .../java/com/oatrice/jarwise/data/api/ReportApi.kt |  24 +
 .../com/oatrice/jarwise/data/model/ReportDto.kt    |  48 ++
 .../jarwise/data/repository/ReportRepository.kt    |  36 +
 .../java/com/oatrice/jarwise/di/NetworkModule.kt   |   1 +
 .../com/oatrice/jarwise/di/RepositoryModule.kt     |   1 +
 .../java/com/oatrice/jarwise/di/ViewModelModule.kt |   2 +-
 .../com/oatrice/jarwise/ui/components/BottomNav.kt |   3 +-
 .../oatrice/jarwise/ui/reports/ReportsScreen.kt    | 783 +++++++++++++++++----
 .../oatrice/jarwise/ui/reports/ReportsViewModel.kt | 241 +++++--
 .../jarwise/ui/reports/ReportsViewModelTest.kt     |  86 +++
 .../59_issue-59/ai_brain/implementation_plan.md    |  51 ++
 docs/features/59_issue-59/ai_brain/task.md         |  13 +
 docs/features/59_issue-59/ai_brain/walkthrough.md  |  42 ++
 15 files changed, 1166 insertions(+), 186 deletions(-)

KEY FILE DIFFS:
diff --git a/app/build.gradle.kts b/app/build.gradle.kts
index b79cffa..ddf1133 100644
--- a/app/build.gradle.kts
+++ b/app/build.gradle.kts
@@ -16,7 +16,7 @@ android {
         minSdk = 24
         targetSdk = 34
         versionCode = 1
-        versionName = "1.11.0"
+        versionName = "1.12.0"
 
         testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
         vectorDrawables {
diff --git a/app/src/main/java/com/oatrice/jarwise/data/api/ReportApi.kt b/app/src/main/java/com/oatrice/jarwise/data/api/ReportApi.kt
new file mode 100644
index 0000000..b59a437
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/data/api/ReportApi.kt
@@ -0,0 +1,24 @@
+package com.oatrice.jarwise.data.api
+
+import com.oatrice.jarwise.data.model.ReportResponse
+import okhttp3.ResponseBody
+import retrofit2.http.GET
+import retrofit2.http.Query
+import retrofit2.http.Streaming
+
+interface ReportApi {
+    @GET("api/v1/reports")
+    suspend fun getReport(
+        @Query("start_date") startDate: String,
+        @Query("end_date") endDate: String,
+        @Query("jar_id") jarId: String? = null
+    ): ReportResponse
+
+    @Streaming
+    @GET("api/v1/reports/export")
+    suspend fun exportReport(
+        @Query("start_date") startDate: String,
+        @Query("end_date") endDate: String,
+        @Query("jar_id") jarId: String? = null
+    ): ResponseBody
+}
diff --git a/app/src/main/java/com/oatrice/jarwise/data/model/ReportDto.kt b/app/src/main/java/com/oatrice/jarwise/data/model/ReportDto.kt
new file mode 100644
index 0000000..dda9315
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/data/model/ReportDto.kt
@@ -0,0 +1,48 @@
+package com.oatrice.jarwise.data.model
+
+import com.google.gson.annotations.SerializedName
+
+data class ReportResponse(
+    @SerializedName("summary") val summary: ChartSummaryDto,
+    @SerializedName("trend") val trend: List<TrendPointDto>,
+    @SerializedName("by_category") val byCategory: List<CategoryAmountDto>,
+    @SerializedName("by_jar") val byJar: List<JarAmountDto>,
+    @SerializedName("comparison") val comparison: ComparisonDataDto? = null
+)
+
+data class ChartSummaryDto(
+    @SerializedName("income") val income: Double,
+    @SerializedName("expense") val expense: Double,
+    @SerializedName("net") val net: Double
+)
+
+data class TrendPointDto(
+    @SerializedName("date") val date: String,
+    @SerializedName("income") val income: Double,
+    @SerializedName("expense") val expense: Double
+)
+
+data class CategoryAmountDto(
+    @SerializedName("id") val id: String,
+    @SerializedName("name") val name: String,
+    @SerializedName("income") val income: Double,
+    @SerializedName("expense") val expense: Double,
+    @SerializedName("amount") val amount: Double,
+    @SerializedName("prev_income") val prevIncome: Double = 0.0,
+    @SerializedName("prev_expense") val prevExpense: Double = 0.0
+)
+
+data class JarAmountDto(
+    @SerializedName("id") val id: String,
+    @SerializedName("name") val name: String,
+    @SerializedName("income") val income: Double,
+    @SerializedName("expense") val expense: Double,
+    @SerializedName("amount") val amount: Double,
+    @SerializedName("prev_income") val prevIncome: Double = 0.0,
+    @SerializedName("prev_expense") val prevExpense: Double = 0.0
+)
+
+data class ComparisonDataDto(
+    @SerializedName("current") val current: ChartSummaryDto,
+    @SerializedName("previous") val previous: ChartSummaryDto
+)
diff --git a/app/src/main/java/com/oatrice/jarwise/data/repository/ReportRepository.kt b/app/src/main/java/com/oatrice/jarwise/data/repository/ReportRepository.kt
new file mode 100644
index 0000000..7844b26
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/data/repository/ReportRepository.kt
@@ -0,0 +1,36 @@
+package com.oatrice.jarwise.data.repository
+
+import com.oatrice.jarwise.data.api.ReportApi
+import com.oatrice.jarwise.data.model.ReportResponse
+import kotlinx.coroutines.flow.Flow
+import kotlinx.coroutines.flow.flow
+
+interface ReportRepository {
+    fun getReport(startDate: String, endDate: String, jarId: String? = null): Flow<ReportResponse?>
+    fun exportReport(startDate: String, endDate: String, jarId: String? = null): Flow<okhttp3.ResponseBody?>
+}
+
+class ReportRepositoryImpl(
+    private val api: ReportApi
+) : ReportRepository {
+
+    override fun getReport(startDate: String, endDate: String, jarId: String?): Flow<ReportResponse?> = flow {
+        try {
+            val response = api.getReport(startDate, endDate, jarId)
+            emit(response)
+        } catch (e: Exception) {
+            e.printStackTrace()
+            emit(null)
+        }
+    }
+
+    override fun exportReport(startDate: String, endDate: String, jarId: String?): Flow<okhttp3.ResponseBody?> = flow {
+        try {
+            val response = api.exportReport(startDate, endDate, jarId)
+            emit(response)
+        } catch (e: Exception) {
+            e.printStackTrace()
+            emit(null)
+        }
+    }
+}
diff --git a/app/src/main/java/com/oatrice/jarwise/di/NetworkModule.kt b/app/src/main/java/com/oatrice/jarwise/di/NetworkModule.kt
index ee89744..1432470 100644
--- a/app/src/main/java/com/oatrice/jarwise/di/NetworkModule.kt
+++ b/app/src/main/java/com/oatrice/jarwise/di/NetworkModule.kt
@@ -34,4 +34,5 @@ val networkModule = module {
 
     single { get<Retrofit>().create(MigrationApi::class.java) }
     single { get<Retrofit>().create(com.oatrice.jarwise.data.api.GraphApi::class.java) }
+    single { get<Retrofit>().create(com.oatrice.jarwise.data.api.ReportApi::class.java) }
 }
diff --git a/app/src/main/java/com/oatrice/jarwise/di/RepositoryModule.kt b/app/src/main/java/com/oatrice/jarwise/di/RepositoryModule.kt
index 5b813c6..84a49ab 100644
--- a/app/src/main/java/com/oatrice/jarwise/di/RepositoryModule.kt
+++ b/app/src/main/java/com/oatrice/jarwise/di/RepositoryModule.kt
@@ -15,4 +15,5 @@ val repositoryModule = module {
     single { MigrationRepository(get(), androidContext()) }
     single<TransactionRepository> { TransactionRepositoryImpl(get(), get()) }
     single<GraphRepository> { GraphRepositoryImpl(get()) }
+    single<ReportRepository> { ReportRepositoryImpl(get()) }
 }
diff --git a/app/src/main/java/com/oatrice/jarwise/di/ViewModelModule.kt b/app/src/main/java/com/oatrice/jarwise/di/ViewModelModule.kt
index 2742c4a..a4881ed 100644
--- a/app/src/main/java/com/oatrice/jarwise/di/ViewModelModule.kt
+++ b/app/src/main/java/com/oatrice/jarwise/di/ViewModelModule.kt
@@ -20,5 +20,5 @@ val viewModelModule = module {
     viewModel { SettingsViewModel(get(), get()) }
     viewModel { MigrationViewModel(get(), get()) }
     viewModel { ReportFilterViewModel(get()) }
-    viewModel { com.oatrice.jarwise.ui.reports.ReportsViewModel() }
+    viewModel { com.oatrice.jarwise.ui.reports.ReportsViewModel(get()) }
 }
diff --git a/app/src/main/java/com/oatrice/jarwise/ui/components/BottomNav.kt b/app/src/main/java/com/oatrice/jarwise/ui/components/BottomNav.kt
index adcca35..6014d96 100644
--- a/app/src/main/java/com/oatrice/jarwise/ui/components/BottomNav.kt
+++ b/app/src/main/java/com/oatrice/jarwise/ui/components/BottomNav.kt
@@ -45,6 +45,7 @@ enum class NavPage {
 fun BottomNav(
     activePage: NavPage = NavPage.DASHBOARD,
     visible: Boolean = true,
+    modifier: Modifier = Modifier,
     onNavigate: (NavPage) -> Unit
 ) {
     // Animate visibility
@@ -55,7 +56,7 @@ fun BottomNav(
     )
     
     Box(
-        modifier = Modifier
+        modifier = modifier
             .fillMaxWidth()
             .windowInsetsPadding(WindowInsets.navigationBars) // Handle system navigation bar
             .padding(horizontal = 20.dp)
diff --git a/app/src/main/java/com/oatrice/jarwise/ui/reports/ReportsScreen.kt b/app/src/main/java/com/oatrice/jarwise/ui/reports/ReportsScreen.kt
index 25cdd82..20dd356 100644
--- a/app/src/main/java/com/oatrice/jarwise/ui/reports/ReportsScreen.kt
+++ b/app/src/main/java/com/oatrice/jarwise/ui/reports/ReportsScreen.kt
@@ -1,5 +1,10 @@
 package com.oatrice.jarwise.ui.reports
 
+import androidx.activity.compose.rememberLauncherForActivityResult
+import androidx.activity.result.contract.ActivityResultContracts
+import androidx.compose.animation.*
+import androidx.compose.animation.core.*
+import androidx.compose.ui.graphics.toArgb
 import androidx.compose.foundation.background
 import androidx.compose.foundation.layout.*
 import androidx.compose.foundation.rememberScrollState
@@ -7,22 +12,30 @@ import androidx.compose.foundation.shape.RoundedCornerShape
 import androidx.compose.foundation.verticalScroll
 import androidx.compose.material.icons.Icons
 import androidx.compose.material.icons.automirrored.rounded.ArrowBack
+import androidx.compose.material.icons.rounded.FileDownload
 import androidx.compose.material.icons.rounded.TrendingDown
 import androidx.compose.material.icons.rounded.TrendingUp
 import androidx.compose.material.icons.rounded.Wallet
 import androidx.compose.material3.*
-import androidx.compose.runtime.Composable
-import androidx.compose.runtime.collectAsState
-import androidx.compose.runtime.getValue
+import androidx.compose.runtime.*
 import androidx.compose.ui.Alignment
 import androidx.compose.ui.Modifier
 import androidx.compose.ui.draw.clip
+import androidx.compose.ui.graphics.Brush
 import androidx.compose.ui.graphics.Color
+import androidx.compose.ui.graphics.SolidColor
 import androidx.compose.ui.graphics.vector.ImageVector
+import androidx.compose.ui.platform.LocalContext
 import androidx.compose.ui.text.font.FontWeight
+import androidx.compose.ui.text.style.TextOverflow
 import androidx.compose.ui.unit.dp
 import androidx.compose.ui.unit.sp
-import androidx.lifecycle.viewmodel.compose.viewModel
+import androidx.compose.foundation.Canvas
+import androidx.compose.foundation.gestures.detectTapGestures
+import androidx.compose.ui.input.pointer.pointerInput
+import androidx.compose.ui.graphics.drawscope.Stroke
+import java.text.NumberFormat
+import org.koin.androidx.compose.koinViewModel
 import com.oatrice.jarwise.ui.theme.Gray900
 import com.oatrice.jarwise.ui.theme.Gray800
 import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
@@ -30,28 +43,127 @@ import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
 import com.patrykandpatrick.vico.compose.chart.Chart
 import com.patrykandpatrick.vico.compose.chart.column.columnChart
 import com.patrykandpatrick.vico.compose.chart.line.lineChart
+import com.patrykandpatrick.vico.compose.chart.layout.fullWidth
+import com.patrykandpatrick.vico.compose.axis.axisLabelComponent
+import com.patrykandpatrick.vico.compose.axis.axisLineComponent
+import com.patrykandpatrick.vico.compose.axis.axisGuidelineComponent
+import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
+import com.patrykandpatrick.vico.core.axis.AxisPosition
+import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
+import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
+import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
+import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
+import com.patrykandpatrick.vico.core.component.shape.LineComponent
+import com.patrykandpatrick.vico.core.component.shape.Shapes
+import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
+import com.patrykandpatrick.vico.core.chart.line.LineChart
+import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
+import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
 import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
 import com.oatrice.jarwise.ui.components.BottomNav
 import com.oatrice.jarwise.ui.components.NavPage
+import java.text.SimpleDateFormat
+import java.util.Calendar
+import java.util.Date
+import java.util.Locale
+
+private val PIE_COLORS = listOf(
+    Color(0xFF6366F1), // Indigo
+    Color(0xFF8B5CF6), // Violet
+    Color(0xFFA78BFA), // Purple
+    Color(0xFFC4B5FD), // Lavender
+    Color(0xFF60A5FA), // Blue
+    Color(0xFF93C5FD)  // Light Blue
+)
+
+private val yAxisFormatter = AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
+    if (value >= 1000) "${(value / 1000).toInt()}k" else value.toInt().toString()
+}
 
 @OptIn(ExperimentalMaterial3Api::class)
 @Composable
 fun ReportsScreen(
     onBack: () -> Unit,
     onNavigate: (NavPage) -> Unit,
-    viewModel: ReportsViewModel = viewModel()
+    viewModel: ReportsViewModel = koinViewModel()
 ) {
     val uiState by viewModel.uiState.collectAsState()
     val scrollState = rememberScrollState()
+    var selectedRange by remember { mutableStateOf("month") }
+    
+    // Custom range state
+    var showDatePicker by remember { mutableStateOf(false) }
+    var customStartDate by remember { mutableStateOf<String?>(null) }
+    var customEndDate by remember { mutableStateOf<String?>(null) }
 
-    Box(modifier = Modifier.fillMaxSize()) {
+    if (showDatePicker) {
+        val dateRangePickerState = rememberDateRangePickerState()
+        DatePickerDialog(
+            onDismissRequest = { showDatePicker = false },
+            confirmButton = {
+                TextButton(onClick = {
+                    val start = dateRangePickerState.selectedStartDateMillis
+                    val end = dateRangePickerState.selectedEndDateMillis
+                    if (start != null && end != null) {
+                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
+                        customStartDate = sdf.format(Date(start))
+                        customEndDate = sdf.format(Date(end))
+                        viewModel.fetchReport("custom", customStartDate, customEndDate)
+                        selectedRange = "custom"
+                    }
+                    showDatePicker = false
+                }) {
+                    Text("Confirm")
+                }
+            },
+            dismissButton = {
+                TextButton(onClick = { showDatePicker = false }) {
+                    Text("Cancel")
+                }
+            }
+        ) {
+            DateRangePicker(
+                state = dateRangePickerState,
+                modifier = Modifier.padding(16.dp).height(500.dp),
+                title = { Text("Select Date Range", modifier = Modifier.padding(16.dp)) }
+            )
+        }
+    }
+
+    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
         Scaffold(
             topBar = {
                 TopAppBar(
-                    title = { Text("Reports", fontWeight = FontWeight.Bold) },
+                    title = { Text("รายงานการเงิน", fontWeight = FontWeight.Bold) },
                     navigationIcon = {
                         IconButton(onClick = onBack) {
-                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
+                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "กลับ")
+                        }
+                    },
+                    actions = {
+                        val context = LocalContext.current
+                        var csvBytes by remember { mutableStateOf<ByteArray?>(null) }
+                        
+                        val launcher = rememberLauncherForActivityResult(
+                            contract = ActivityResultContracts.CreateDocument("text/csv")
+                        ) { uri ->
+                            uri?.let {
+                                context.contentResolver.openOutputStream(it)?.use { stream ->
+                                    csvBytes?.let { bytes -> stream.write(bytes) }
+                                }
+                            }
+                        }
+
+                        IconButton(
+                            onClick = {
+                                viewModel.exportReport(selectedRange, customStartDate, customEndDate) { bytes ->
+                                    csvBytes = bytes
+                                    launcher.launch("jarwise-export-${selectedRange}.csv")
+                                }
+                            },
+                            enabled = !uiState.isLoading
+                        ) {
+                            Icon(Icons.Rounded.FileDownload, contentDescription = "Export CSV", tint = Color.White)
                         }
                     },
                     colors = TopAppBarDefaults.topAppBarColors(
@@ -68,12 +180,12 @@ fun ReportsScreen(
                     .padding(paddingValues)
                     .fillMaxSize()
                     .padding(horizontal = 16.dp)
-                    .padding(bottom = 100.dp) // Extra padding for BottomNav
+                    .padding(bottom = 80.dp)
                     .verticalScroll(scrollState)
             ) {
                 Spacer(modifier = Modifier.height(16.dp))
 
-                // Date Range (Mock)
+                // Date Range Picker
                 Row(
                     modifier = Modifier
                         .fillMaxWidth()
@@ -81,134 +193,512 @@ fun ReportsScreen(
                         .padding(4.dp),
                     horizontalArrangement = Arrangement.SpaceEvenly
                 ) {
-                    FilterChip(selected = true, onClick = {}, label = { Text("Month") })
-                    FilterChip(selected = false, onClick = {}, label = { Text("Quarter") })
-                    FilterChip(selected = false, onClick = {}, label = { Text("Year") })
+                    val ranges = listOf(
+                        "month" to "เดือน", 
+                        "quarter" to "ไตรมาส", 
+                        "year" to "ปี",
+                        "7d" to "7 วัน",
+                        "30d" to "30 วัน",
+                        "all" to "ทั้งหมด",
+                        "custom" to "กำหนดเอง"
+                    )
+                    ranges.forEach { (key, label) ->
+                        FilterChip(
+                            selected = selectedRange == key,
+                            onClick = { 
+                                if (key == "custom") {
+                                    showDatePicker = true
+                                } else {
+                                    selectedRange = key
+                                    customStartDate = null
+                                    customEndDate = null
+                                    
+                                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
+                                    val nowStr = sdf.format(Date())
+                                    
+                                    when (key) {
+                                        "7d" -> {
+                                            val c = Calendar.getInstance()
+                                            c.add(Calendar.DAY_OF_YEAR, -7)
+                                            viewModel.fetchReport("custom", sdf.format(c.time), nowStr)
+                                        }
+                                        "30d" -> {
+                                            val c = Calendar.getInstance()
+                                            c.add(Calendar.DAY_OF_YEAR, -30)
+                                            viewModel.fetchReport("custom", sdf.format(c.time), nowStr)
+                                        }
+                                        else -> viewModel.fetchReport(key)
+                                    }
+                                }
+                            },
+                            label = { Text(label) },
+                            colors = FilterChipDefaults.filterChipColors(
+                                selectedContainerColor = Color(0xFF6366F1),
+                                selectedLabelColor = Color.White,
+                                labelColor = Color.
... (Diff truncated for size) ...


PR TEMPLATE:
# 📋 Summary
<!-- Brief description of changes for the Android Application -->

## ✅ Checklist
- [ ] 🏗️ I have moved the related issue to "In Progress" on the Kanban board

# 🎯 Type
- [ ] 🐛 Bug fix
- [ ] ✨ New feature
- [ ] ⚡ Performance improvement
- [ ] 🔧 Refactoring
- [ ] 🎨 UI Update (Jetpack Compose)
- [ ] 🤖 SDK/Dependency Update
- [ ] 💥 Breaking change

# 📱 Android Specific Checks
- [ ] Verified on Emulator
- [ ] Verified on Real Device
- [ ] Screen Orientation Support (Portrait/Landscape)
- [ ] Dark/Light Mode Tested

# 📝 Changes
<!-- Describe what changed in detail -->

# 📸 UI/UX Screenshots
<!-- Include screenshots from the Android device/emulator. MUST include screenshots for UI changes. -->

# 🧪 Testing
- [ ] `./gradlew build` passes
- [ ] Unit Tests pass

# 🚀 Migration/Deployment
- [ ] Database migration required (Room)
- [ ] Environment variables/Secrets updated
- [ ] New Dependencies added

```bash
# Migration commands if applicable
```

# 🔗 Related Issues
<!-- Link to related issues or PRs using FULL URL e.g. https://github.com/oatrice/JarWise-Root/issues/1 -->
- Closes #
- Related to #
- Fixes #

**Breaking Changes**: <!-- Yes/No -->
**Migration Required**: <!-- Yes/No -->


INSTRUCTIONS:
1. Generate a comprehensive PR description in Markdown format.
2. If a template is provided, fill it out intelligently.
3. If no template, use a standard structure: Summary, Changes, Impact.
4. Focus on 'Why' and 'What'.
5. Do not include 'Here is the PR description' preamble. Just the body.
6. IMPORTANT: Always use the exact FULL URL for closing issues. You must write `Closes https://github.com/oatrice/JarWise-Root/issues/59`. Do NOT use short syntax (e.g., #123) and do not invent an owner/repo.
