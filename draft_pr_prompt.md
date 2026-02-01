# PR Draft Prompt

You are an AI assistant helping to create a Pull Request description.
    
TASK: [Web | Android] Manage Jars (Edit %, Name, Icon)
ISSUE: {
  "title": "[Web | Android] Manage Jars (Edit %, Name, Icon)",
  "number": 17
}

GIT CONTEXT:
COMMITS:
691dbf2 feat: [Web | Android] Manage Jars (Edit %, Name, Icon)...
e41e8e2 feat: [Web | Android] Manage Jars (Edit %, Name, Icon)...
0315bea feat: [Web | Android] Manage Jars (Edit %, Name, Icon)...
8f77971 feat: [Web | Android] Manage Jars (Edit %, Name, Icon)...
8e1d756 ✨ feat(jar): add jar management feature
cb2e11e ✨ feat(test): add jar configuration test infrastructure
fb81229 ✨ feat(ui): add jar management screen and integrate with dashboard
d72cd6e ✨ feat(jars): add jar configuration management system

STATS:
.luma_rules.json                                   |  29 +
 .luma_state.json                                   |  16 +-
 CHANGELOG.md                                       |   5 +
 README.md                                          |   4 +-
 app/build.gradle.kts                               |   3 +-
 .../com.oatrice.jarwise.data.AppDatabase/4.json    | 120 +++++
 .../main/java/com/oatrice/jarwise/MainActivity.kt  |  54 +-
 .../java/com/oatrice/jarwise/data/AppDatabase.kt   |  18 +-
 .../java/com/oatrice/jarwise/data/JarConfig.kt     |  31 ++
 .../java/com/oatrice/jarwise/data/JarConfigDao.kt  |  35 ++
 .../jarwise/data/repository/JarConfigRepository.kt |  53 ++
 .../java/com/oatrice/jarwise/ui/DashboardScreen.kt |   5 +-
 .../java/com/oatrice/jarwise/ui/MainViewModel.kt   |  50 +-
 .../jarwise/ui/managejars/ManageJarsScreen.kt      | 301 +++++++++++
 .../jarwise/ui/managejars/ManageJarsViewModel.kt   | 176 +++++++
 .../jarwise/ui/managejars/FakeJarConfigDao.kt      |  54 ++
 .../ui/managejars/ManageJarsViewModelTest.kt       | 146 ++++++
 .../oatrice/jarwise/utils/MainDispatcherRule.kt    |  23 +
 draft_pr_body.md                                   |  63 +++
 draft_pr_prompt.md                                 | 584 +++++++++++++++++++++
 20 files changed, 1740 insertions(+), 30 deletions(-)

KEY FILE DIFFS:
diff --git a/app/build.gradle.kts b/app/build.gradle.kts
index 4c8efcf..74e0edb 100644
--- a/app/build.gradle.kts
+++ b/app/build.gradle.kts
@@ -13,7 +13,7 @@ android {
         minSdk = 24
         targetSdk = 34
         versionCode = 1
-        versionName = "1.2.0"
+        versionName = "1.3.0"
 
         testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
         vectorDrawables {
@@ -98,6 +98,7 @@ dependencies {
     androidTestImplementation(libs.androidx.ui.test.junit4)
     debugImplementation(libs.androidx.ui.tooling)
     debugImplementation(libs.androidx.ui.test.manifest)
+    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
 }
 
 ksp {
diff --git a/app/src/main/java/com/oatrice/jarwise/MainActivity.kt b/app/src/main/java/com/oatrice/jarwise/MainActivity.kt
index afb2974..5961cd0 100644
--- a/app/src/main/java/com/oatrice/jarwise/MainActivity.kt
+++ b/app/src/main/java/com/oatrice/jarwise/MainActivity.kt
@@ -15,8 +15,11 @@ import com.oatrice.jarwise.data.AppDatabase
 
 import com.oatrice.jarwise.data.GeneratedMockData
 import com.oatrice.jarwise.data.repository.CurrencyRepository
+import com.oatrice.jarwise.data.repository.JarConfigRepository
 import com.oatrice.jarwise.data.repository.UserPreferencesRepository
 import com.oatrice.jarwise.data.service.SlipDetectorServiceImpl
+import com.oatrice.jarwise.ui.managejars.ManageJarsScreen
+import com.oatrice.jarwise.ui.managejars.ManageJarsViewModel
 import com.oatrice.jarwise.ui.AddTransactionScreen
 import com.oatrice.jarwise.ui.DashboardScreen
 import com.oatrice.jarwise.ui.MainViewModel
@@ -35,6 +38,7 @@ sealed class Screen {
     data object AddTransaction : Screen()
     data object SlipImport : Screen()
     data object Settings : Screen()
+    data object ManageJars : Screen()
 }
 
 class MainActivity : ComponentActivity() {
@@ -45,19 +49,33 @@ class MainActivity : ComponentActivity() {
             applicationContext,
             AppDatabase::class.java, "jarwise-db"
         )
-            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
+            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4)
             .build()
         
         val userPreferencesRepository = UserPreferencesRepository(applicationContext)
         val currencyRepository = CurrencyRepository(userPreferencesRepository)
         
-        val viewModel: MainViewModel by viewModels { MainViewModel.Factory(db.transactionDao(), currencyRepository) }
+        // JarConfig Repository
+        val jarConfigRepository = JarConfigRepository(db.jarConfigDao())
+        
+        
+        val viewModel: MainViewModel by viewModels { 
+            MainViewModel.Factory(
+                db.transactionDao(), 
+                currencyRepository,
+                jarConfigRepository
+            ) 
+        }
 
         val slipRepository = com.oatrice.jarwise.data.repository.SlipRepository(applicationContext)
         val slipDetector = SlipDetectorServiceImpl(applicationContext)
         val slipViewModel: SlipViewModel by viewModels { 
             SlipViewModel.Factory(slipRepository, slipDetector) 
         }
+        
+        val manageJarsViewModel: ManageJarsViewModel by viewModels {
+            ManageJarsViewModel.Factory(jarConfigRepository)
+        }
 
         enableEdgeToEdge()
         setContent {
@@ -83,18 +101,22 @@ class MainActivity : ComponentActivity() {
                     color = MaterialTheme.colorScheme.background
                 ) {
                     when (currentScreen) {
-                        is Screen.Dashboard -> DashboardScreen(
-                            jars = GeneratedMockData.jars, // Ideally usage ViewModel for jars too
-                            transactions = transactions,
-                            formattedTotalBalance = formattedTotalBalance,
-                            selectedCurrency = selectedCurrency,
-                            onNavigateToHistory = { currentScreen = Screen.TransactionHistory },
-                            onNavigateToScan = { currentScreen = Screen.Scan },
-                            onNavigateToImport = { currentScreen = Screen.SlipImport },
-                            onNavigateToAdd = { currentScreen = Screen.AddTransaction },
-                            onNavigateToSettings = { currentScreen = Screen.Settings },
-                            onNavigate = handleNavigation
-                        )
+                        is Screen.Dashboard -> {
+                            val jars by viewModel.jars.collectAsState()
+                            DashboardScreen(
+                                jars = jars,
+                                transactions = transactions,
+                                formattedTotalBalance = formattedTotalBalance,
+                                selectedCurrency = selectedCurrency,
+                                onNavigateToHistory = { currentScreen = Screen.TransactionHistory },
+                                onNavigateToScan = { currentScreen = Screen.Scan },
+                                onNavigateToImport = { currentScreen = Screen.SlipImport },
+                                onNavigateToAdd = { currentScreen = Screen.AddTransaction },
+                                onNavigateToSettings = { currentScreen = Screen.Settings },
+                                onNavigateToManageJars = { currentScreen = Screen.ManageJars },
+                                onNavigate = handleNavigation
+                            )
+                        }
                         is Screen.Settings -> SettingsScreen(
                              onBack = { currentScreen = Screen.Dashboard },
                              viewModel = viewModel
@@ -158,6 +180,10 @@ class MainActivity : ComponentActivity() {
                                 currentScreen = Screen.Dashboard
                             }
                         )
+                        is Screen.ManageJars -> ManageJarsScreen(
+                            viewModel = manageJarsViewModel,
+                            onBack = { currentScreen = Screen.Dashboard }
+                        )
                     }
                 }
             }
diff --git a/app/src/main/java/com/oatrice/jarwise/data/AppDatabase.kt b/app/src/main/java/com/oatrice/jarwise/data/AppDatabase.kt
index 5077124..57b1730 100644
--- a/app/src/main/java/com/oatrice/jarwise/data/AppDatabase.kt
+++ b/app/src/main/java/com/oatrice/jarwise/data/AppDatabase.kt
@@ -5,9 +5,10 @@ import androidx.room.RoomDatabase
 import androidx.room.migration.Migration
 import androidx.sqlite.db.SupportSQLiteDatabase
 
-@Database(entities = [Transaction::class], version = 3, exportSchema = true)
+@Database(entities = [Transaction::class, JarConfig::class], version = 4, exportSchema = true)
 abstract class AppDatabase : RoomDatabase() {
     abstract fun transactionDao(): TransactionDao
+    abstract fun jarConfigDao(): JarConfigDao
 
     companion object {
         val MIGRATION_1_2 = object : Migration(1, 2) {
@@ -22,5 +23,20 @@ abstract class AppDatabase : RoomDatabase() {
                 db.execSQL("ALTER TABLE transactions ADD COLUMN walletId TEXT NOT NULL DEFAULT 'wallet-cash'")
             }
         }
+
+        val MIGRATION_3_4 = object : Migration(3, 4) {
+            override fun migrate(db: SupportSQLiteDatabase) {
+                db.execSQL("""
+                    CREATE TABLE IF NOT EXISTS jar_configs (
+                        id TEXT PRIMARY KEY NOT NULL,
+                        name TEXT NOT NULL,
+                        percentage INTEGER NOT NULL,
+                        colorName TEXT NOT NULL,
+                        iconName TEXT NOT NULL
+                    )
+                """.trimIndent())
+            }
+        }
     }
 }
+
diff --git a/app/src/main/java/com/oatrice/jarwise/data/JarConfig.kt b/app/src/main/java/com/oatrice/jarwise/data/JarConfig.kt
new file mode 100644
index 0000000..7ca6c72
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/data/JarConfig.kt
@@ -0,0 +1,31 @@
+package com.oatrice.jarwise.data
+
+import androidx.room.Entity
+import androidx.room.PrimaryKey
+
+/**
+ * Room Entity for storing jar configuration
+ * Allows users to customize jar name, percentage, color, and icon
+ */
+@Entity(tableName = "jar_configs")
+data class JarConfig(
+    @PrimaryKey val id: String,
+    val name: String,
+    val percentage: Int,      // 0-100, total across all jars must = 100
+    val colorName: String,    // "blue", "green", "pink", "yellow", "purple", "red", "cyan", "orange"
+    val iconName: String      // "home", "dollar", "gamepad", "school", "flight", "heart", "work", "savings"
+) {
+    companion object {
+        /**
+         * Default 6 Jars configuration based on T. Harv Eker's money management system
+         */
+        val DEFAULTS = listOf(
+            JarConfig("1", "Necessities", 55, "blue", "home"),
+            JarConfig("2", "Financial Freedom", 10, "green", "dollar"),
+            JarConfig("3", "Play", 10, "pink", "gamepad"),
+            JarConfig("4", "Education", 10, "yellow", "school"),
+            JarConfig("5", "Long-term Savings", 10, "purple", "flight"),
+            JarConfig("6", "Give", 5, "red", "heart")
+        )
+    }
+}
diff --git a/app/src/main/java/com/oatrice/jarwise/data/JarConfigDao.kt b/app/src/main/java/com/oatrice/jarwise/data/JarConfigDao.kt
new file mode 100644
index 0000000..d726ded
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/data/JarConfigDao.kt
@@ -0,0 +1,35 @@
+package com.oatrice.jarwise.data
+
+import androidx.room.*
+import kotlinx.coroutines.flow.Flow
+
+/**
+ * DAO for JarConfig CRUD operations
+ */
+@Dao
+interface JarConfigDao {
+    
+    @Query("SELECT * FROM jar_configs ORDER BY id")
+    fun getAllFlow(): Flow<List<JarConfig>>
+    
+    @Query("SELECT * FROM jar_configs ORDER BY id")
+    suspend fun getAll(): List<JarConfig>
+    
+    @Query("SELECT * FROM jar_configs WHERE id = :id")
+    suspend fun getById(id: String): JarConfig?
+    
+    @Insert(onConflict = OnConflictStrategy.REPLACE)
+    suspend fun insertAll(configs: List<JarConfig>)
+    
+    @Insert(onConflict = OnConflictStrategy.REPLACE)
+    suspend fun insert(config: JarConfig)
+    
+    @Update
+    suspend fun update(config: JarConfig)
+    
+    @Query("DELETE FROM jar_configs")
+    suspend fun deleteAll()
+    
+    @Query("SELECT COUNT(*) FROM jar_configs")
+    suspend fun count(): Int
+}
diff --git a/app/src/main/java/com/oatrice/jarwise/data/repository/JarConfigRepository.kt b/app/src/main/java/com/oatrice/jarwise/data/repository/JarConfigRepository.kt
new file mode 100644
index 0000000..206d273
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/data/repository/JarConfigRepository.kt
@@ -0,0 +1,53 @@
+package com.oatrice.jarwise.data.repository
+
+import com.oatrice.jarwise.data.JarConfig
+import com.oatrice.jarwise.data.JarConfigDao
+import kotlinx.coroutines.flow.Flow
+
+/**
+ * Repository for managing jar configurations
+ */
+class JarConfigRepository(private val jarConfigDao: JarConfigDao) {
+    
+    /**
+     * Get all jar configs as Flow (reactive updates)
+     */
+    fun getAllJarConfigsFlow(): Flow<List<JarConfig>> = jarConfigDao.getAllFlow()
+    
+    /**
+     * Get all jar configs (one-shot)
+     */
+    suspend fun getAllJarConfigs(): List<JarConfig> = jarConfigDao.getAll()
+    
+    /**
+     * Get jar config by ID
+     */
+    suspend fun getJarConfigById(id: String): JarConfig? = jarConfigDao.getById(id)
+    
+    /**
+     * Update a single jar config
+     */
+    suspend fun updateJarConfig(config: JarConfig) = jarConfigDao.update(config)
+    
+    /**
+     * Save all jar configs (replace all)
+     */
+    suspend fun saveAllJarConfigs(configs: List<JarConfig>) = jarConfigDao.insertAll(configs)
+    
+    /**
+     * Reset to default 6 Jars configuration
+     */
+    suspend fun resetToDefaults() {
+        jarConfigDao.deleteAll()
+        jarConfigDao.insertAll(JarConfig.DEFAULTS)
+    }
+    
+    /**
+     * Initialize default jars if database is empty
+     */
+    suspend fun initializeDefaultsIfEmpty() {
+        if (jarConfigDao.count() == 0) {
+            jarConfigDao.insertAll(JarConfig.DEFAULTS)
+        }
+    }
+}
diff --git a/app/src/main/java/com/oatrice/jarwise/ui/DashboardScreen.kt b/app/src/main/java/com/oatrice/jarwise/ui/DashboardScreen.kt
index 695d662..4107fa4 100644
--- a/app/src/main/java/com/oatrice/jarwise/ui/DashboardScreen.kt
+++ b/app/src/main/java/com/oatrice/jarwise/ui/DashboardScreen.kt
@@ -103,6 +103,7 @@ fun DashboardScreen(
     onNavigateToImport: () -> Unit = {},
     onNavigateToAdd: () -> Unit = {},
     onNavigateToSettings: () -> Unit = {},
+    onNavigateToManageJars: () -> Unit = {},
     onNavigate: (NavPage) -> Unit = {}
 ) {
     // Scroll state for visibility tracking
@@ -198,8 +199,8 @@ fun DashboardScreen(
                         verticalAlignment = Alignment.CenterVertically
                     ) {
                         Text(text = "Your Jars", style = MaterialTheme.typography.titleMedium.copy(color = Gray100))
-                        TextButton(onClick = { /* View All */ }) {
-                            Text("View All", color = Blue500)
+                        TextButton(onClick = onNavigateToManageJars) {
+                            Text("Manage", color = Blue500)
                         }
                     }
                 }
diff --git a/app/src/main/java/com/oatrice/jarwise/ui/MainViewModel.kt b/app/src/main/java/com/oatrice/jarwise/ui/MainViewModel.kt
index f842c53..f7007d9 100644
--- a/app/src/main/java/com/oatrice/jarwise/ui/MainViewModel.kt
+++ b/app/src/main/java/com/oatrice/jarwise/ui/MainViewModel.kt
@@ -6,6 +6,9 @@ import androidx.lifecycle.viewModelScope
 import com.oatrice.jarwise.data.Transaction
 import com.oatrice.jarwise.data.TransactionDao
 import com.oatrice.jarwise.data.repository.CurrencyRepository
+import com.oatrice.jarwise.data.repository.JarConfigRepository
+import com.oatrice.jarwise.model.Jar
+import com.oatrice.jarwise.ui.managejars.ManageJarsViewModel
 import com.oatrice.jarwise.utils.TransactionDisplayUtils
 import kotlinx.coroutines.flow.SharingStarted
 import kotlinx.coroutines.flow.combine
@@ -16,9 +19,16 @@ import java.util.*
 
 class MainViewModel(
     private val dao: TransactionDao,
-    private val currencyRepository: CurrencyRepository
+    private val currencyRepository: CurrencyRepository,
+    private val jarConfigRepository: JarConfigRepository
 ) : ViewModel() {
 
+    init {
+        viewModelScope.launch {
+            jarConfigRepository.initializeDefaultsIfEmpty()
+        }
+    }
+
     val transactions = dao.getAll().stateIn(
         scope = viewModelScope,
         started = SharingStarted.WhileSubscribed(5000),
@@ -40,6 +50,39 @@ class MainViewModel(
         initialValue = "..."
     )
 
+    // Real Jars Data Integrator
+    val jars = combine(
+        jarConfigRepository.getAllJarConfigsFlow(),
+        transactions
+    ) { configs, txs ->
+        if (configs.isEmpty()) {
+            emptyList()
+        } else {
+            configs.map { config ->
+                val balance = txs.filter { it.jarId == config.id }.sumOf { it.amount }
+                // Simple level/goal logic for MVP
+                val level = (balance / 1000).toInt().coerceAtLeast(1)
+                val goal = 5000.0 // Hardcoded goal for now (Issue #67)
+
+                Jar(
+                    id = config.id,
+                    name = config.name,
+                    current = balance,
+                    goal = goal,
+                    level = level,
+                    icon = ManageJarsViewModel.getIconFromName(config.iconName),
+                    color = ManageJarsViewModel.getColorFromName(config.colorName),
+                    shadowColor = ManageJarsViewModel.getColorFromName(config.colorName), // Reuse same color for shadow/bar for now
+                    barColor = ManageJarsViewModel.getColorFromName(config.colorName)
+                )
+            }
+        }
+    }.stateIn(
+        scope = viewModelScope,
+        started = SharingStarted.WhileSubscribed(5000),
+        initialValue = emptyList()
+    )
+
     fun saveTransaction(amount: Double, jarId: String, walletId: String, note: String, date: String? = null) {
         viewModelScope.launch {
             val transaction = Transaction(
@@ -81,12 +124,13 @@ class MainViewModel(
 
     class Factory(
         private val dao: TransactionDao,
-        private val currencyRepository: CurrencyRepository
+        private val currencyRepository: CurrencyRepository,
+        private val jarConfigRepository: JarConfigRepository
     ) : ViewModelProvider.Factory {
         @Suppress("UNCHECKED_CAST")
         override fun <T : ViewModel> create(modelClass: Class<T>): T {
             if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
-                return MainViewModel(dao, currencyRepository) as T
+                return MainViewModel(dao, currencyRepository, jarConfigRepository) as T
             }
             throw IllegalArgumentException("Unknown ViewModel class")
         }
diff --git a/app/src/main/java/com/oatrice/jarwise/ui/managejars/ManageJarsScreen.kt b/app/src/main/java/com/oatrice/jarwise/ui/managejars/ManageJarsScreen.kt
new file mode 100644
index 0000000..aae5db5
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/ui/managejars/ManageJarsScreen.kt
@@ -0,0 +1,301 @@
+package com.oatrice.jarwise.ui.managejars
+
+import androidx.compose.animation.*
+import androidx.compose.foundation.background
+import androidx.compose.foundation.clickable
+import androidx.compose.foundation.layout.*
+import androidx.compose.foundation.lazy.LazyColumn
+import androidx.compose.foundation.lazy.itemsIndexed
+import androidx.compose.foundation.shape.CircleShape
+import androidx.compose.foundation.shape.RoundedCornerShape
+import androidx.compose.material.icons.Icons
+import androidx.compose.material.icons.rounded.*
+import androidx.compose.material3.*
+import androidx.compose.runtime.*
+import androidx.compose.ui.Alignment
+import androidx.compose.ui.Modifier
+import androidx.compose.ui.draw.clip
+import androidx.compose.ui.graphics.Color
+import androidx.compose.ui.text.font.FontWeight
+import androidx.compose.ui.unit.dp
+import androidx.compose.ui.unit.sp
+import com.oatrice.jarwise.ui.theme.*
+
+@OptIn(ExperimentalMaterial3Api::class)
+@Composable
+fun ManageJarsScreen(
+    viewModel: ManageJarsViewModel,
+    onBack: () -> Unit
+) {
+    val jars by viewModel.jars.collectAsState()
+    val selectedJarId by viewModel.selectedJarId.collectAsState()
+    val totalPercentage by viewModel.totalPercentage.collectAsState()
+    val isValid by viewModel.isValid.collectAsState()
+    val showResetDialog by viewModel.showResetDialog.collectAsState()
+
+    Scaffold(
+        topBar = {
+            TopAppBar(
+                title = { Text("Manage Jars", fontWeight = FontWeight.Bold) },
+                navigationIcon = {
+                    IconButton(onClick = onBack) {
+                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
+                    }
+                },
+                actions = {
+                    TextButton(onClick = { viewModel.showResetConfirmation() }) {
+                        Icon(Icons.Rounded.Refresh, contentDescription = "Reset", modifier = Modifier.size(18.dp))
+                        Spacer(modifier = Modifier.width(4.dp))
+                        Text("Reset")
+                    }
+                    Button(
+                        onClick = { viewModel.save(onBack) },
+                        enabled = isValid,
+                        colors = ButtonDefaults.buttonColors(
+                            containerColor = if (isValid) Green50
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
