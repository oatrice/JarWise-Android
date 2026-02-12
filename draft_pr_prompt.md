# PR Draft Prompt

You are an AI assistant helping to create a Pull Request description.
    
TASK: [Feature] Report Filter: Multi-Select Categories & Accounts
ISSUE: {
  "title": "[Feature] Report Filter: Multi-Select Categories & Accounts",
  "number": 68,
  "body": "# \ud83c\udfaf Objective\nEnable users to filter reports and charts by selecting specific categories (Jars) and accounts (Wallets) via multi-select checkboxes.\n\n## \ud83d\udcdd Specifications\n\n### UI Components\n- [ ] **Filter Panel**: Collapsible sidebar or modal with checkbox tree\n- [ ] **Category Checkboxes**: Select/deselect individual Jars (including sub-jars if HIER-01 is done)\n- [ ] **Account Checkboxes**: Select/deselect individual Wallets (including sub-wallets if HIER-01 is done)\n- [ ] **Select All / Clear All**: Quick actions\n- [ ] **Remember Selection**: Persist filter state per session or per report type\n\n### Behavior\n- [ ] **Real-time Update**: Charts/reports update as checkboxes change (or Apply button)\n- [ ] **Count Display**: Show number of transactions matching current filter\n- [ ] **Visual Indicator**: Badge showing active filter count\n\n## \ud83d\udd17 References\n- Depends on #67 (Hierarchical Accounts & Categories) for sub-item support\n- Related to #59 (Reports & Data Export)\n- Feature ID: `REPORT-02`\n\n## \ud83c\udfd7\ufe0f Technical Notes\n- Use bitmasking or array-based filtering on transaction queries\n- Consider performance with large transaction sets (pagination/lazy load)"
}

GIT CONTEXT:
COMMITS:
3158de2 feat: add transaction filtering and bump version to 1.10.0
7cd2c58 feat(db): add database schema v8 with transactions, jars and allocations
38b945f refactor(room): Migrate from KSP to KAPT for Room compiler
b514b4f refactor(build): Update Kotlin and AGP configurations
4e6ef2f chore(deps): Update Gradle and Compose versions
51a3fe6 Add wallet count query
2e95fce Remove JarConfigViewModel dep
69e9f07 Add JarConfigSource interface

STATS:
CHANGELOG.md                                       |   7 +
 app/build.gradle.kts                               |  42 +--
 .../com.oatrice.jarwise.data.AppDatabase/8.json    | 343 +++++++++++++++++++++
 .../java/com/oatrice/jarwise/data/AppDatabase.kt   |  18 +-
 .../com/oatrice/jarwise/data/GeneratedMockData.kt  |  47 +--
 .../com/oatrice/jarwise/data/SubTransaction.kt     |  25 ++
 .../com/oatrice/jarwise/data/SubTransactionDao.kt  |  22 ++
 .../java/com/oatrice/jarwise/data/WalletDao.kt     |   5 +-
 .../jarwise/data/repository/JarConfigRepository.kt |   6 +-
 .../jarwise/data/repository/JarConfigSource.kt     |   8 +
 .../jarwise/data/repository/WalletRepository.kt    |  11 +-
 .../jarwise/data/repository/WalletSource.kt        |   8 +
 .../main/java/com/oatrice/jarwise/di/DataModule.kt |   4 +-
 .../com/oatrice/jarwise/di/RepositoryModule.kt     |   2 +
 .../java/com/oatrice/jarwise/di/ViewModelModule.kt |   2 +
 .../oatrice/jarwise/ui/TransactionHistoryScreen.kt | 102 +++++-
 .../jarwise/ui/reportfilter/ReportFilterSheet.kt   | 184 +++++++++++
 .../jarwise/ui/reportfilter/ReportFilterUiState.kt |  13 +
 .../ui/reportfilter/ReportFilterViewModel.kt       |  89 ++++++
 .../ui/reportfilter/ReportFilterViewModelTest.kt   | 166 ++++++++++
 .../com/oatrice/jarwise/util/MainDispatcherRule.kt |  23 ++
 build.gradle.kts                                   |  14 +-
 code_review.md                                     | 129 +-------
 gradle/libs.versions.toml                          |  10 +-
 gradle/wrapper/gradle-wrapper.properties           |   2 +-
 scripts/build_android.sh                           |   6 +-
 26 files changed, 1077 insertions(+), 211 deletions(-)

KEY FILE DIFFS:
diff --git a/app/build.gradle.kts b/app/build.gradle.kts
index 1ac022a..0af0e7f 100644
--- a/app/build.gradle.kts
+++ b/app/build.gradle.kts
@@ -1,32 +1,28 @@
+import org.jetbrains.kotlin.gradle.dsl.JvmTarget
+
 plugins {
     alias(libs.plugins.android.application)
-    alias(libs.plugins.jetbrains.kotlin.android)
-    alias(libs.plugins.ksp)
+    alias(libs.plugins.compose.compiler)
+    alias(libs.plugins.android.legacy.kapt)
     alias(libs.plugins.google.services)
 }
 
 android {
     namespace = "com.oatrice.jarwise"
     compileSdk = 34
-    buildToolsVersion = "34.0.0"
 
     defaultConfig {
         applicationId = "com.oatrice.jarwise"
         minSdk = 24
         targetSdk = 34
         versionCode = 1
-        versionName = "1.9.0"
+        versionName = "1.10.0"
 
         testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
         vectorDrawables {
             useSupportLibrary = true
         }
 
-        javaCompileOptions {
-            annotationProcessorOptions {
-                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
-            }
-        }
     }
 
     buildTypes {
@@ -42,15 +38,9 @@ android {
         sourceCompatibility = JavaVersion.VERSION_17
         targetCompatibility = JavaVersion.VERSION_17
     }
-    kotlinOptions {
-        jvmTarget = "17"
-    }
     buildFeatures {
         compose = true
     }
-    composeOptions {
-        kotlinCompilerExtensionVersion = "1.5.14"
-    }
     packaging {
         resources {
             excludes += "/META-INF/{AL2.0,LGPL2.1}"
@@ -71,18 +61,26 @@ android {
     }
 
     sourceSets {
+        val roomSchemas = file("$projectDir/schemas").path
         getByName("test") {
-            assets.srcDirs("$projectDir/schemas")
+            assets.directories.add(roomSchemas)
         }
         getByName("androidTest") {
-            assets.srcDirs("$projectDir/schemas")
+            assets.directories.add(roomSchemas)
         }
         getByName("release") {
-            assets.srcDirs("$projectDir/schemas")
+            assets.directories.add(roomSchemas)
         }
     }
 }
 
+// Kotlin compiler options moved to the new DSL.
+kotlin {
+    compilerOptions {
+        jvmTarget.set(JvmTarget.JVM_17)
+    }
+}
+
 dependencies {
 
     implementation(libs.androidx.core.ktx)
@@ -90,7 +88,7 @@ dependencies {
     implementation(libs.androidx.lifecycle.viewmodel.compose)
     implementation(libs.androidx.room.runtime)
     implementation(libs.androidx.room.ktx)
-    ksp(libs.androidx.room.compiler)
+    kapt(libs.androidx.room.compiler)
     implementation(libs.androidx.activity.compose)
     implementation(platform(libs.androidx.compose.bom))
     implementation(libs.androidx.ui)
@@ -155,6 +153,8 @@ dependencies {
     implementation(libs.logging.interceptor)
 }
 
-ksp {
-    arg("room.schemaLocation", "$projectDir/schemas")
+kapt {
+    arguments {
+        arg("room.schemaLocation", "$projectDir/schemas")
+    }
 }
diff --git a/app/src/main/java/com/oatrice/jarwise/data/AppDatabase.kt b/app/src/main/java/com/oatrice/jarwise/data/AppDatabase.kt
index 652f7a4..527bb97 100644
--- a/app/src/main/java/com/oatrice/jarwise/data/AppDatabase.kt
+++ b/app/src/main/java/com/oatrice/jarwise/data/AppDatabase.kt
@@ -5,12 +5,13 @@ import androidx.room.RoomDatabase
 import androidx.room.migration.Migration
 import androidx.sqlite.db.SupportSQLiteDatabase
 
-@Database(entities = [Transaction::class, JarConfig::class, Allocation::class, WalletEntity::class], version = 7, exportSchema = true)
+@Database(entities = [Transaction::class, JarConfig::class, Allocation::class, WalletEntity::class, SubTransaction::class], version = 8, exportSchema = true)
 abstract class AppDatabase : RoomDatabase() {
     abstract fun transactionDao(): TransactionDao
     abstract fun jarConfigDao(): JarConfigDao
     abstract fun allocationDao(): AllocationDao
     abstract fun walletDao(): WalletDao
+    abstract fun subTransactionDao(): SubTransactionDao
 
     companion object {
         val MIGRATION_1_2 = object : Migration(1, 2) {
@@ -105,6 +106,21 @@ abstract class AppDatabase : RoomDatabase() {
             }
         }
 
+        val MIGRATION_7_8 = object : Migration(7, 8) {
+            override fun migrate(db: SupportSQLiteDatabase) {
+                db.execSQL("""
+                    CREATE TABLE IF NOT EXISTS `sub_transactions` (
+                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
+                        `parentId` INTEGER NOT NULL, 
+                        `description` TEXT NOT NULL, 
+                        `amount` REAL NOT NULL, 
+                        FOREIGN KEY(`parentId`) REFERENCES `transactions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
+                    )
+                """.trimIndent())
+                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sub_transactions_parentId` ON `sub_transactions` (`parentId`)")
+            }
+        }
+
         val SEED_CALLBACK = object : RoomDatabase.Callback() {
             override fun onCreate(db: SupportSQLiteDatabase) {
                 super.onCreate(db)
diff --git a/app/src/main/java/com/oatrice/jarwise/data/GeneratedMockData.kt b/app/src/main/java/com/oatrice/jarwise/data/GeneratedMockData.kt
index 2c96ade..46c9e65 100644
--- a/app/src/main/java/com/oatrice/jarwise/data/GeneratedMockData.kt
+++ b/app/src/main/java/com/oatrice/jarwise/data/GeneratedMockData.kt
@@ -8,48 +8,9 @@ import com.oatrice.jarwise.ui.theme.*
 
 // WARNING: This file is auto-generated. Do not edit directly.
 // Generated from: shared-spec/data/mockData.json
-// Generated at: 2026-01-31T11:24:59.073Z
+// Generated at: 2026-02-06T07:25:19.853Z
 
 object GeneratedMockData {
-    val wallets = listOf(
-        com.oatrice.jarwise.model.Wallet(
-            id = "101",
-            name = "Bank Account",
-            balance = 15430.00,
-            color = Blue500,
-            icon = Icons.Rounded.AccountBalance,
-            parentId = null,
-            level = 0
-        ),
-        com.oatrice.jarwise.model.Wallet(
-            id = "102",
-            name = "K-Bank Savings",
-            balance = 12000.00,
-            color = Green500,
-            icon = Icons.Rounded.Savings,
-            parentId = "101",
-            level = 1
-        ),
-        com.oatrice.jarwise.model.Wallet(
-            id = "103",
-            name = "SCB Checking",
-            balance = 3430.00,
-            color = Purple500,
-            icon = Icons.Rounded.CreditCard,
-            parentId = "101",
-            level = 1
-        ),
-        com.oatrice.jarwise.model.Wallet(
-            id = "104",
-            name = "Cash Wallet",
-            balance = 1250.00,
-            color = Yellow500,
-            icon = Icons.Rounded.Wallet,
-            parentId = null,
-            level = 0
-        )
-    )
-
     val jars = listOf(
         Jar(
             id = "1",
@@ -123,7 +84,7 @@ object GeneratedMockData {
         Transaction(
             id = "1",
             merchant = "Spotify Premium",
-            amount = 12.99,
+            amount = -12.99,
             category = "Play",
             date = "Today, 10:43 AM",
             icon = Icons.Rounded.Headphones,
@@ -133,7 +94,7 @@ object GeneratedMockData {
         Transaction(
             id = "2",
             merchant = "Whole Foods Market",
-            amount = 142.5,
+            amount = -142.5,
             category = "Necessities",
             date = "Yesterday, 6:30 PM",
             icon = Icons.Rounded.ShoppingBag,
@@ -143,7 +104,7 @@ object GeneratedMockData {
         Transaction(
             id = "3",
             merchant = "Udemy Course",
-            amount = 24.99,
+            amount = -24.99,
             category = "Education",
             date = "Dec 28, 2025",
             icon = Icons.Rounded.School,
diff --git a/app/src/main/java/com/oatrice/jarwise/data/SubTransaction.kt b/app/src/main/java/com/oatrice/jarwise/data/SubTransaction.kt
new file mode 100644
index 0000000..fadac6c
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/data/SubTransaction.kt
@@ -0,0 +1,25 @@
+package com.oatrice.jarwise.data
+
+import androidx.room.Entity
+import androidx.room.ForeignKey
+import androidx.room.Index
+import androidx.room.PrimaryKey
+
+@Entity(
+    tableName = "sub_transactions",
+    foreignKeys = [
+        ForeignKey(
+            entity = Transaction::class,
+            parentColumns = ["id"],
+            childColumns = ["parentId"],
+            onDelete = ForeignKey.CASCADE
+        )
+    ],
+    indices = [Index(value = ["parentId"])]
+)
+data class SubTransaction(
+    @PrimaryKey(autoGenerate = true) val id: Long = 0,
+    val parentId: Long,
+    val description: String,
+    val amount: Double
+)
diff --git a/app/src/main/java/com/oatrice/jarwise/data/SubTransactionDao.kt b/app/src/main/java/com/oatrice/jarwise/data/SubTransactionDao.kt
new file mode 100644
index 0000000..5894cac
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/data/SubTransactionDao.kt
@@ -0,0 +1,22 @@
+package com.oatrice.jarwise.data
+
+import androidx.room.Dao
+import androidx.room.Delete
+import androidx.room.Insert
+import androidx.room.Query
+import kotlinx.coroutines.flow.Flow
+
+@Dao
+interface SubTransactionDao {
+    @Query("SELECT * FROM sub_transactions WHERE parentId = :parentId")
+    fun getByParentId(parentId: Long): Flow<List<SubTransaction>>
+
+    @Insert
+    suspend fun insert(subTransaction: SubTransaction): Long
+
+    @Delete
+    suspend fun delete(subTransaction: SubTransaction)
+
+    @Query("DELETE FROM sub_transactions WHERE parentId = :parentId")
+    suspend fun deleteAllByParentId(parentId: Long)
+}
diff --git a/app/src/main/java/com/oatrice/jarwise/data/WalletDao.kt b/app/src/main/java/com/oatrice/jarwise/data/WalletDao.kt
index d4dfd06..df1f98d 100644
--- a/app/src/main/java/com/oatrice/jarwise/data/WalletDao.kt
+++ b/app/src/main/java/com/oatrice/jarwise/data/WalletDao.kt
@@ -20,7 +20,10 @@ interface WalletDao {
 
     @Query("DELETE FROM wallets WHERE id = :id")
     suspend fun deleteWallet(id: String)
-    
+
+    @Query("SELECT COUNT(*) FROM wallets")
+    suspend fun count(): Int
+
     @Query("DELETE FROM wallets")
     suspend fun clearAll()
 }
diff --git a/app/src/main/java/com/oatrice/jarwise/data/repository/JarConfigRepository.kt b/app/src/main/java/com/oatrice/jarwise/data/repository/JarConfigRepository.kt
index 206d273..bc145ed 100644
--- a/app/src/main/java/com/oatrice/jarwise/data/repository/JarConfigRepository.kt
+++ b/app/src/main/java/com/oatrice/jarwise/data/repository/JarConfigRepository.kt
@@ -7,7 +7,7 @@ import kotlinx.coroutines.flow.Flow
 /**
  * Repository for managing jar configurations
  */
-class JarConfigRepository(private val jarConfigDao: JarConfigDao) {
+class JarConfigRepository(private val jarConfigDao: JarConfigDao) : JarConfigSource {
     
     /**
      * Get all jar configs as Flow (reactive updates)
@@ -17,7 +17,7 @@ class JarConfigRepository(private val jarConfigDao: JarConfigDao) {
     /**
      * Get all jar configs (one-shot)
      */
-    suspend fun getAllJarConfigs(): List<JarConfig> = jarConfigDao.getAll()
+    override suspend fun getAllJarConfigs(): List<JarConfig> = jarConfigDao.getAll()
     
     /**
      * Get jar config by ID
@@ -45,7 +45,7 @@ class JarConfigRepository(private val jarConfigDao: JarConfigDao) {
     /**
      * Initialize default jars if database is empty
      */
-    suspend fun initializeDefaultsIfEmpty() {
+    override suspend fun initializeDefaultsIfEmpty() {
         if (jarConfigDao.count() == 0) {
             jarConfigDao.insertAll(JarConfig.DEFAULTS)
         }
diff --git a/app/src/main/java/com/oatrice/jarwise/data/repository/JarConfigSource.kt b/app/src/main/java/com/oatrice/jarwise/data/repository/JarConfigSource.kt
new file mode 100644
index 0000000..3c55259
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/data/repository/JarConfigSource.kt
@@ -0,0 +1,8 @@
+package com.oatrice.jarwise.data.repository
+
+import com.oatrice.jarwise.data.JarConfig
+
+interface JarConfigSource {
+    suspend fun getAllJarConfigs(): List<JarConfig>
+    suspend fun initializeDefaultsIfEmpty()
+}
diff --git a/app/src/main/java/com/oatrice/jarwise/data/repository/WalletRepository.kt b/app/src/main/java/com/oatrice/jarwise/data/repository/WalletRepository.kt
index b2d3799..b1bbb5c 100644
--- a/app/src/main/java/com/oatrice/jarwise/data/repository/WalletRepository.kt
+++ b/app/src/main/java/com/oatrice/jarwise/data/repository/WalletRepository.kt
@@ -14,7 +14,7 @@ import kotlinx.coroutines.flow.Flow
 import kotlinx.coroutines.flow.map
 import kotlinx.coroutines.flow.first
 
-open class WalletRepository(private val walletDao: WalletDao) {
+open class WalletRepository(private val walletDao: WalletDao) : WalletSource {
 
     open val wallets: Flow<List<Wallet>> = walletDao.getAllWallets().map { entities ->
         entities.map { it.toWallet() }
@@ -76,9 +76,8 @@ open class WalletRepository(private val walletDao: WalletDao) {
     /**
      * Initialize default wallets if database is empty
      */
-    suspend open fun initializeDefaultsIfEmpty() {
-        val currentWallets = walletDao.getAllWallets().first()
-        if (currentWallets.isEmpty()) { 
+    override suspend fun initializeDefaultsIfEmpty() {
+        if (walletDao.count() == 0) {
              val defaults = listOf(
                  Wallet(id = "wallet-cash", name = "Cash", balance = 0.0, color = Color(0xFF22C55E), icon = Icons.Default.AccountBalanceWallet),
                  Wallet(id = "wallet-bank", name = "Bank Account", balance = 0.0, color = Color(0xFF3B82F6), icon = Icons.Default.AccountBalance),
@@ -88,4 +87,8 @@ open class WalletRepository(private val walletDao: WalletDao) {
              defaults.forEach { insertWallet(it) }
         }
     }
+
+    override suspend fun getAllWallets(): List<Wallet> {
+        return wallets.first()
+    }
 }
diff --git a/app/src/main/java/com/oatrice/jarwise/data/repository/WalletSource.kt b/app/src/main/java/com/oatrice/jarwise/data/repository/WalletSource.kt
new file mode 100644
index 0000000..950770d
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/data/repository/WalletSource.kt
@@ -0,0 +1,8 @@
+package com.oatrice.jarwise.data.repository
+
+import com.oatrice.jarwise.model.Wallet
+
+interface WalletSource {
+    suspend fun getAllWallets(): List<Wallet>
+    suspend fun initializeDefaultsIfEmpty()
+}
diff --git a/app/src/main/java/com/oatrice/jarwise/di/DataModule.kt b/app/src/main/java/com/oatrice/jarwise/di/DataModule.kt
index 232de89..a7080b6 100644
--- a/app/src/main/java/com/oatrice/jarwise/di/DataModule.kt
+++ b/app/src/main/java/com/oatrice/jarwise/di/DataModule.kt
@@ -20,7 +20,8 @@ val dataModule = module {
                 AppDatabase.MIGRATION_3_4,
                 AppDatabase.MIGRATION_4_5,
                 AppDatabase.MIGRATION_5_6,
-                AppDatabase.MIGRATION_6_7
+                AppDatabase.MIGRATION_6_7,
+                AppDatabase.MIGRATION_7_8
             )
             .addCallback(AppDatabase.SEED_CALLBACK)
             .fallbackToDestructiveMigration()
@@ -32,6 +33,7 @@ val dataModule = module {
     single { get<AppDatabase>().jarConfigDao() }
     single { get<AppDatabase>().allocationDao() }
     single { get<AppDatabase>().walletDao() }
+    single { get<AppDatabase>().subTransactionDao() }
 
     // Services
     single<SlipDetectorService> { SlipDetectorServiceImpl(androidContext()) }
diff --git a/app/src/main/java/com/oatrice/jarwise/di/RepositoryModule.kt b/app/src/main/java/com/oatrice/jarwise/di/RepositoryModule.kt
index f2ba044..1945bc4 100644
--- a/app/src/main/java/com/oatrice/jarwise/di/RepositoryModule.kt
+++ b/app/src/main/java/com/oatrice/jarwise/di/RepositoryModule.kt
@@ -8,7 +8,9 @@ val repositoryModule = module {
     single { UserPreferencesRepository(androidContext()) }
     single { CurrencyRepository(get()) }
     single { JarConfigRepository(get()) }
+    single<JarConfigSource> { get<JarConfigRepository>() }
     single { WalletRepository(get()) }
+    single<WalletSource> { get<WalletRepository>() }
     single { SlipRepository(androidContext()) }
     single { MigrationRepository(get(), androidContext()) }
     single<TransactionRepository> { TransactionRepositoryImpl(get(), get()) }
diff --git a/app/src/main/java/com/oatrice/jarwise/di/ViewModelModule.kt b/app/src/main/java/com/oatrice/jarwise/di/ViewModelModule.kt
index 621c409..8672890 100644
--- a/app/src/main/java/com/oatrice/jarwise/di/ViewModelModule.kt
+++ b/app/src/main/java/com/oatrice/jarwise/di/ViewModelModule.kt
@@ -7,6 +7,7 @@ import com.oatrice.jarwise.ui.managewallets.ManageWalletsViewModel
 import com.oatrice.jarwise.ui.login.LoginViewModel
 import com.oatrice.jarwise.ui.settings.SettingsViewModel
 import com.oatrice.jarwise.ui.migration.MigrationViewModel
+import com.oatrice.jarwise.ui.reportfilter.ReportFilterViewModel
 import org.koin.androidx.viewmodel.dsl.viewModel
 import org.koin.dsl.module
 
@@ -18,4 +19,5 @@ val viewModelModule = module {
     viewModel { LoginViewModel(get(), get()) }
     viewModel { SettingsViewModel(get(), get()) }
     viewModel { MigrationViewModel(get(), get()) }
+    viewModel { ReportFilterViewModel(get()) }
 }
diff --git a/app/src/main/java/com/oatrice/jarwise/ui/TransactionHistoryScreen.kt b/app/src/main/java/com/oatrice/jarwise/ui/TransactionHistoryScreen.kt
index ef530b4..90b95f7 100644
--- a/app/src/main/java/com/oatrice/jarwise/ui/TransactionHistoryScreen.kt
+++ b/app/src/main/java/com/oatrice/jarwise/ui/TransactionHistoryScreen.kt
@@ -16,6 +16,7 @@ import androidx.compose.foundation.shape.RoundedCornerShape
 import androidx.compose.material.icons.Icons
 import androidx.compose.material.icons.rounded.ArrowBack
 import androidx.compose.material.icons.rounded.CalendarMonth
+import androidx.compose.material.icons.rounded.FilterList
 import androidx.compose.material.icons.rounded.Search
 import androidx.compose.material3.ExperimentalMaterial3Api
 import androidx.compose.material3.Icon
@@ -26,6 +27,7 @@ import androidx.compose.material3.Text
 import androidx.compose.material3.TopAppBar
 import androidx.compose.material3.TopAppBarDefaults
 import androidx.compose.runtime.Composable
+import androidx.compose.runtime.LaunchedEffect
 import androidx.compose.ui.Alignment
 import androidx.compose.ui.Modifier
 import androidx.compose.ui.graphics.Color
@@ -49,11 +51,17 @@ import com.oatrice.jarwise.ui.components.BottomNav
 import com.oatrice.jarwise.ui.components.NavPage
 import androidx.compose.foundation.lazy.rememberLazyListState
 import androidx.compose.runtime.derivedStateOf
+import androidx.compose.runtime.getValue
+import androidx.compose.runtime.mutableStateOf
 import androidx.compose.runtime.remember
+import androidx.compose.runtime.setValue
 import androidx.compose.foundation.layout.Box
 import androidx.compose.material3.MediumTopAppBar
 import androidx.compose.ui.input.nestedscroll.nestedScroll
 import kotlin.math.abs
+import com.oatrice.jarwise.ui.reportfilter.ReportFilterSheet
+import com.oatrice.jarwise.ui.reportfilter.ReportFilterViewModel
+import org.koin.androidx.compose.koinViewModel
 
 /**
  * Transaction History Screen
@@ -67,9 +75,34 @@ fun TransactionHistoryScreen(
     onBack: () -> Unit,
     onNavigate: (NavPage) -> Unit = {}
 ) {
+    val reportFilterViewModel: ReportFilterViewModel = koinViewModel()
+    var showFilters by remember { mutableStateOf(false) }
+    var activeJarFilters by remember { mutableStateOf<Set<String>>(emptySet()) }
+    var activeWalletFilters by remember { mutableStateOf<Set<String>>(emptySet()) }
+
+    val hasActiveFilters = activeJarFilters.isNotEmpty() || activeWalletFilters.isNotEmpty()
+
+    LaunchedEffect(showFilters) {
+        if (showFilters) {
+            reportFilterViewModel.setSelections(activeJarFilters, activeWalletFilters)
+        }
+    }
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
6. IMPORTANT: Always use FULL URLs for links to issues and other PRs (e.g., https://github.com/owner/repo/issues/123), do NOT use short syntax (e.g., #123) to ensuring proper linking across platforms.
