# PR Draft Prompt

You are an AI assistant helping to create a Pull Request description.
    
TASK: [Web | Android] Support Hierarchical Wallets (Sub-accounts)
ISSUE: {
  "title": "[Web | Android] Support Hierarchical Wallets (Sub-accounts)",
  "number": 69
}

GIT CONTEXT:
COMMITS:
e0b9588 feat: [Web | Android] Support Hierarchical Wallets (Sub-...
8c6a50d feat: [Web | Android] Support Hierarchical Wallets (Sub-...
8f64d86 ✨ feat(wallet): adds hierarchical wallet management and fixes UI/database issues
fb71ef8 🐛 fix(test): correct viewmodel instantiation in tests
1dfa403 ✨ feat(wallet): add wallet initialization and deletion confirmation
3c31136 ✨ feat(wallets): add wallet management functionality
d053dea 🐛 ui: remove fixed height constraint on wallet dropdown menu
2a53cf1 ✨ feat(ui): implement wallet hierarchy level calculation
dbe380c ✨ feat(ui): add wallet management screen
757d190 🐛 fix(database): add fallback to destructive migration
d114676 ✨ feat(wallets): implement hierarchical wallet management

STATS:
.luma_state.json                                   |  17 +-
 CHANGELOG.md                                       |  10 +
 app/build.gradle.kts                               |   3 +-
 .../com.oatrice.jarwise.data.AppDatabase/6.json    | 288 ++++++++
 .../main/java/com/oatrice/jarwise/MainActivity.kt  |  22 +-
 .../java/com/oatrice/jarwise/data/AppDatabase.kt   |  22 +-
 .../com/oatrice/jarwise/data/GeneratedMockData.kt  |  39 +
 .../java/com/oatrice/jarwise/data/WalletDao.kt     |  26 +
 .../java/com/oatrice/jarwise/data/WalletEntity.kt  |  15 +
 .../jarwise/data/repository/WalletRepository.kt    |  91 +++
 .../main/java/com/oatrice/jarwise/model/Models.kt  |  10 +
 .../java/com/oatrice/jarwise/ui/SettingsScreen.kt  |  21 +
 .../ui/managewallets/AddEditWalletDialog.kt        | 194 +++++
 .../ui/managewallets/ManageWalletsScreen.kt        | 293 ++++++++
 .../ui/managewallets/ManageWalletsViewModel.kt     | 149 ++++
 .../ui/managewallets/ManageWalletsViewModelTest.kt | 228 ++++++
 code_review.md                                     | 197 +++--
 draft_pr_body.md                                   | 182 +++--
 draft_pr_prompt.md                                 | 800 +++++++++++----------
 draft_pr_prompt.txt                                |  68 --
 gradle/libs.versions.toml                          |   1 +
 21 files changed, 2054 insertions(+), 622 deletions(-)

KEY FILE DIFFS:
diff --git a/app/build.gradle.kts b/app/build.gradle.kts
index 37c08db..0dbdff7 100644
--- a/app/build.gradle.kts
+++ b/app/build.gradle.kts
@@ -13,7 +13,7 @@ android {
         minSdk = 24
         targetSdk = 34
         versionCode = 1
-        versionName = "1.4.0"
+        versionName = "1.5.0"
 
         testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
         vectorDrawables {
@@ -84,6 +84,7 @@ dependencies {
 
     implementation(libs.androidx.core.ktx)
     implementation(libs.androidx.lifecycle.runtime.ktx)
+    implementation(libs.androidx.lifecycle.viewmodel.compose)
     implementation(libs.androidx.room.runtime)
     implementation(libs.androidx.room.ktx)
     ksp(libs.androidx.room.compiler)
diff --git a/app/src/main/java/com/oatrice/jarwise/MainActivity.kt b/app/src/main/java/com/oatrice/jarwise/MainActivity.kt
index 085ad31..cfbd53c 100644
--- a/app/src/main/java/com/oatrice/jarwise/MainActivity.kt
+++ b/app/src/main/java/com/oatrice/jarwise/MainActivity.kt
@@ -20,6 +20,7 @@ import com.oatrice.jarwise.data.repository.UserPreferencesRepository
 import com.oatrice.jarwise.data.service.SlipDetectorServiceImpl
 import com.oatrice.jarwise.ui.managejars.ManageJarsScreen
 import com.oatrice.jarwise.ui.managejars.ManageJarsViewModel
+import com.oatrice.jarwise.ui.managewallets.ManageWalletsViewModel
 import com.oatrice.jarwise.ui.AddTransactionScreen
 import com.oatrice.jarwise.ui.DashboardScreen
 import com.oatrice.jarwise.ui.MainViewModel
@@ -39,6 +40,7 @@ sealed class Screen {
     data object SlipImport : Screen()
     data object Settings : Screen()
     data object ManageJars : Screen()
+    data object ManageWallets : Screen()
 }
 
 class MainActivity : ComponentActivity() {
@@ -53,9 +55,11 @@ class MainActivity : ComponentActivity() {
                 AppDatabase.MIGRATION_1_2, 
                 AppDatabase.MIGRATION_2_3, 
                 AppDatabase.MIGRATION_3_4,
-                AppDatabase.MIGRATION_4_5
+                AppDatabase.MIGRATION_4_5,
+                AppDatabase.MIGRATION_5_6
             )
             .addCallback(AppDatabase.SEED_CALLBACK)
+            .fallbackToDestructiveMigration()
             .build()
         
         val userPreferencesRepository = UserPreferencesRepository(applicationContext)
@@ -63,6 +67,7 @@ class MainActivity : ComponentActivity() {
         
         // JarConfig Repository
         val jarConfigRepository = JarConfigRepository(db.jarConfigDao())
+        val walletRepository = com.oatrice.jarwise.data.repository.WalletRepository(db.walletDao())
         
         
         val viewModel: MainViewModel by viewModels { 
@@ -83,6 +88,10 @@ class MainActivity : ComponentActivity() {
             ManageJarsViewModel.Factory(db.allocationDao())
         }
 
+        val manageWalletsViewModel: ManageWalletsViewModel by viewModels {
+            ManageWalletsViewModel.Factory(walletRepository)
+        }
+
         enableEdgeToEdge()
         setContent {
             JarWiseTheme {
@@ -125,6 +134,7 @@ class MainActivity : ComponentActivity() {
                         }
                         is Screen.Settings -> SettingsScreen(
                              onBack = { currentScreen = Screen.Dashboard },
+                             onNavigateToManageWallets = { currentScreen = Screen.ManageWallets },
                              viewModel = viewModel
                         )
                         is Screen.TransactionHistory -> TransactionHistoryScreen(
@@ -190,6 +200,16 @@ class MainActivity : ComponentActivity() {
                             viewModel = manageJarsViewModel,
                             onBack = { currentScreen = Screen.Dashboard }
                         )
+                        is Screen.ManageWallets -> com.oatrice.jarwise.ui.managewallets.ManageWalletsScreen(
+                            onNavigateBack = { currentScreen = Screen.Settings },
+                            // viewModel = manageWalletsViewModel // Explicitly pass or let it use default if we change Screen signature
+                            // Current Screen composable likely uses default viewModel() which won't work with Factory unless provided via LocalViewModelStoreOwner or passed directly.
+                            // Assuming ManageWalletsScreen instantiates VM internally with koin/hilt or we need to pass it. 
+                            // Looking at ManageWalletsScreen.kt (Step 441 in history), it uses `viewModel: ManageWalletsViewModel = viewModel()`.
+                            // Without Hilt, `viewModel()` won't pick up the Factory associated with MainActivity unless we pass the *instance* or change how it's retrieved.
+                            // Correct approach for simple DI: Pass the viewModel instance created in MainActivity.
+                            viewModel = manageWalletsViewModel 
+                        )
                     }
                 }
             }
diff --git a/app/src/main/java/com/oatrice/jarwise/data/AppDatabase.kt b/app/src/main/java/com/oatrice/jarwise/data/AppDatabase.kt
index ba179e6..145e165 100644
--- a/app/src/main/java/com/oatrice/jarwise/data/AppDatabase.kt
+++ b/app/src/main/java/com/oatrice/jarwise/data/AppDatabase.kt
@@ -5,11 +5,12 @@ import androidx.room.RoomDatabase
 import androidx.room.migration.Migration
 import androidx.sqlite.db.SupportSQLiteDatabase
 
-@Database(entities = [Transaction::class, JarConfig::class, Allocation::class], version = 5, exportSchema = true)
+@Database(entities = [Transaction::class, JarConfig::class, Allocation::class, WalletEntity::class], version = 6, exportSchema = true)
 abstract class AppDatabase : RoomDatabase() {
     abstract fun transactionDao(): TransactionDao
     abstract fun jarConfigDao(): JarConfigDao
     abstract fun allocationDao(): AllocationDao
+    abstract fun walletDao(): WalletDao
 
     companion object {
         val MIGRATION_1_2 = object : Migration(1, 2) {
@@ -64,8 +65,6 @@ abstract class AppDatabase : RoomDatabase() {
                 db.execSQL("CREATE INDEX IF NOT EXISTS `index_allocations_parentId` ON `allocations` (`parentId`)")
 
                 // Migrate data from jar_configs to allocations (as system default jars)
-                // Note: jar_configs.id is String '1'-'6', we need to map to Long id
-                // We'll migrate them as new allocations with level=0, parentId=NULL
                 db.execSQL("""
                     INSERT INTO allocations (userId, name, level, targetPercent, icon, color, sortOrder, isSystemDefault, isActive)
                     SELECT 
@@ -83,6 +82,23 @@ abstract class AppDatabase : RoomDatabase() {
             }
         }
 
+        // Migration for Wallets (New Version 6)
+        val MIGRATION_5_6 = object : Migration(5, 6) {
+            override fun migrate(db: SupportSQLiteDatabase) {
+                db.execSQL("""
+                    CREATE TABLE IF NOT EXISTS wallets (
+                        id TEXT PRIMARY KEY NOT NULL,
+                        name TEXT NOT NULL,
+                        balance REAL NOT NULL,
+                        colorArgb INTEGER NOT NULL,
+                        iconName TEXT NOT NULL,
+                        parentId TEXT,
+                        level INTEGER NOT NULL
+                    )
+                """.trimIndent())
+            }
+        }
+
         val SEED_CALLBACK = object : RoomDatabase.Callback() {
             override fun onCreate(db: SupportSQLiteDatabase) {
                 super.onCreate(db)
diff --git a/app/src/main/java/com/oatrice/jarwise/data/GeneratedMockData.kt b/app/src/main/java/com/oatrice/jarwise/data/GeneratedMockData.kt
index 1bc9a66..8c56eb4 100644
--- a/app/src/main/java/com/oatrice/jarwise/data/GeneratedMockData.kt
+++ b/app/src/main/java/com/oatrice/jarwise/data/GeneratedMockData.kt
@@ -11,6 +11,45 @@ import com.oatrice.jarwise.ui.theme.*
 // Generated at: 2026-01-31T11:24:59.073Z
 
 object GeneratedMockData {
+    val wallets = listOf(
+        com.oatrice.jarwise.model.Wallet(
+            id = "101",
+            name = "Bank Account",
+            balance = 15430.00,
+            color = Blue500,
+            icon = Icons.Rounded.AccountBalance,
+            parentId = null,
+            level = 0
+        ),
+        com.oatrice.jarwise.model.Wallet(
+            id = "102",
+            name = "K-Bank Savings",
+            balance = 12000.00,
+            color = Green500,
+            icon = Icons.Rounded.Savings,
+            parentId = "101",
+            level = 1
+        ),
+        com.oatrice.jarwise.model.Wallet(
+            id = "103",
+            name = "SCB Checking",
+            balance = 3430.00,
+            color = Purple500,
+            icon = Icons.Rounded.CreditCard,
+            parentId = "101",
+            level = 1
+        ),
+        com.oatrice.jarwise.model.Wallet(
+            id = "104",
+            name = "Cash Wallet",
+            balance = 1250.00,
+            color = Yellow500,
+            icon = Icons.Rounded.Wallet,
+            parentId = null,
+            level = 0
+        )
+    )
+
     val jars = listOf(
         Jar(
             id = "1",
diff --git a/app/src/main/java/com/oatrice/jarwise/data/WalletDao.kt b/app/src/main/java/com/oatrice/jarwise/data/WalletDao.kt
new file mode 100644
index 0000000..d4dfd06
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/data/WalletDao.kt
@@ -0,0 +1,26 @@
+package com.oatrice.jarwise.data
+
+import androidx.room.Dao
+import androidx.room.Insert
+import androidx.room.OnConflictStrategy
+import androidx.room.Query
+import androidx.room.Update
+import kotlinx.coroutines.flow.Flow
+
+@Dao
+interface WalletDao {
+    @Query("SELECT * FROM wallets")
+    fun getAllWallets(): Flow<List<WalletEntity>>
+
+    @Insert(onConflict = OnConflictStrategy.REPLACE)
+    suspend fun insertWallet(wallet: WalletEntity)
+
+    @Update
+    suspend fun updateWallet(wallet: WalletEntity)
+
+    @Query("DELETE FROM wallets WHERE id = :id")
+    suspend fun deleteWallet(id: String)
+    
+    @Query("DELETE FROM wallets")
+    suspend fun clearAll()
+}
diff --git a/app/src/main/java/com/oatrice/jarwise/data/WalletEntity.kt b/app/src/main/java/com/oatrice/jarwise/data/WalletEntity.kt
new file mode 100644
index 0000000..3166420
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/data/WalletEntity.kt
@@ -0,0 +1,15 @@
+package com.oatrice.jarwise.data
+
+import androidx.room.Entity
+import androidx.room.PrimaryKey
+
+@Entity(tableName = "wallets")
+data class WalletEntity(
+    @PrimaryKey val id: String,
+    val name: String,
+    val balance: Double,
+    val colorArgb: Int, // Store Color.toArgb()
+    val iconName: String, // Store icon name (e.g. "AccountBalance")
+    val parentId: String?,
+    val level: Int
+)
diff --git a/app/src/main/java/com/oatrice/jarwise/data/repository/WalletRepository.kt b/app/src/main/java/com/oatrice/jarwise/data/repository/WalletRepository.kt
new file mode 100644
index 0000000..c1efddf
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/data/repository/WalletRepository.kt
@@ -0,0 +1,91 @@
+package com.oatrice.jarwise.data.repository
+
+import androidx.compose.ui.graphics.Color
+import androidx.compose.ui.graphics.vector.ImageVector
+import androidx.compose.material.icons.Icons
+import androidx.compose.material.icons.filled.AccountBalanceWallet
+import androidx.compose.material.icons.filled.AttachMoney
+import androidx.compose.material.icons.filled.CreditCard
+import androidx.compose.material.icons.filled.AccountBalance
+import com.oatrice.jarwise.data.WalletDao
+import com.oatrice.jarwise.data.WalletEntity
+import com.oatrice.jarwise.model.Wallet
+import kotlinx.coroutines.flow.Flow
+import kotlinx.coroutines.flow.map
+import kotlinx.coroutines.flow.first
+
+class WalletRepository(private val walletDao: WalletDao) {
+
+    val wallets: Flow<List<Wallet>> = walletDao.getAllWallets().map { entities ->
+        entities.map { it.toWallet() }
+    }
+
+    suspend fun insertWallet(wallet: Wallet) {
+        walletDao.insertWallet(wallet.toEntity())
+    }
+
+    suspend fun updateWallet(wallet: Wallet) {
+        walletDao.updateWallet(wallet.toEntity())
+    }
+
+    suspend fun deleteWallet(id: String) {
+        walletDao.deleteWallet(id)
+    }
+
+    // Mapper Functions
+    private fun WalletEntity.toWallet(): Wallet {
+        return Wallet(
+            id = id,
+            name = name,
+            balance = balance,
+            color = Color(colorArgb),
+            icon = getIconByName(iconName),
+            parentId = parentId,
+            level = level
+        )
+    }
+
+    private fun Wallet.toEntity(): WalletEntity {
+        return WalletEntity(
+            id = id,
+            name = name,
+            balance = balance,
+            colorArgb = color.value.toLong().toInt(), // Convert ULong Color to Int
+            iconName = getIconName(icon),
+            parentId = parentId,
+            level = level
+        )
+    }
+
+    // Helper to map String -> ImageVector (Basic implementation)
+    // In a real app, this should be consistent with how icons are selected/stored
+    private fun getIconByName(name: String): ImageVector {
+        return when (name) {
+            "AccountBalanceWallet" -> Icons.Default.AccountBalanceWallet
+            "AttachMoney" -> Icons.Default.AttachMoney
+            "CreditCard" -> Icons.Default.CreditCard
+            "AccountBalance" -> Icons.Default.AccountBalance
+            else -> Icons.Default.AccountBalanceWallet // Default
+        }
+    }
+
+    private fun getIconName(icon: ImageVector): String {
+        return icon.name.substringAfterLast(".") // Extract simple name
+    }
+
+    /**
+     * Initialize default wallets if database is empty
+     */
+    suspend fun initializeDefaultsIfEmpty() {
+        val currentWallets = walletDao.getAllWallets().first()
+        if (currentWallets.isEmpty()) { 
+             val defaults = listOf(
+                 Wallet(id = "wallet-cash", name = "Cash", balance = 0.0, color = Color(0xFF22C55E), icon = Icons.Default.AccountBalanceWallet),
+                 Wallet(id = "wallet-bank", name = "Bank Account", balance = 0.0, color = Color(0xFF3B82F6), icon = Icons.Default.AccountBalance),
+                 Wallet(id = "wallet-credit", name = "Credit Card", balance = 0.0, color = Color(0xFFA855F7), icon = Icons.Default.CreditCard)
+             )
+             
+             defaults.forEach { insertWallet(it) }
+        }
+    }
+}
diff --git a/app/src/main/java/com/oatrice/jarwise/model/Models.kt b/app/src/main/java/com/oatrice/jarwise/model/Models.kt
index 7d7a717..071bd59 100644
--- a/app/src/main/java/com/oatrice/jarwise/model/Models.kt
+++ b/app/src/main/java/com/oatrice/jarwise/model/Models.kt
@@ -25,3 +25,13 @@ data class Transaction(
     val color: Color, // Icon background tint
     val iconTint: Color // Icon foreground color
 )
+
+data class Wallet(
+    val id: String,
+    val name: String,
+    val balance: Double,
+    val color: Color,
+    val icon: ImageVector,
+    val parentId: String? = null,
+    val level: Int = 0
+)
diff --git a/app/src/main/java/com/oatrice/jarwise/ui/SettingsScreen.kt b/app/src/main/java/com/oatrice/jarwise/ui/SettingsScreen.kt
index 84d1e03..ab24913 100644
--- a/app/src/main/java/com/oatrice/jarwise/ui/SettingsScreen.kt
+++ b/app/src/main/java/com/oatrice/jarwise/ui/SettingsScreen.kt
@@ -3,6 +3,7 @@ package com.oatrice.jarwise.ui
 import androidx.compose.foundation.layout.*
 import androidx.compose.material.icons.Icons
 import androidx.compose.material.icons.automirrored.rounded.ArrowBack
+import androidx.compose.material.icons.rounded.AccountBalanceWallet
 import androidx.compose.material3.*
 import androidx.compose.runtime.*
 import androidx.compose.ui.Alignment
@@ -14,6 +15,7 @@ import com.oatrice.jarwise.utils.TransactionDisplayUtils
 @Composable
 fun SettingsScreen(
     onBack: () -> Unit,
+    onNavigateToManageWallets: () -> Unit = {},
     viewModel: MainViewModel
 ) {
     val selectedCurrency by viewModel.selectedCurrency.collectAsState()
@@ -43,6 +45,25 @@ fun SettingsScreen(
             verticalArrangement = Arrangement.Top,
             horizontalAlignment = Alignment.Start
         ) {
+            Text(
+                text = "General",
+                style = MaterialTheme.typography.titleMedium,
+                modifier = Modifier.padding(bottom = 8.dp)
+            )
+
+            OutlinedButton(
+                onClick = onNavigateToManageWallets,
+                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
+            ) {
+                Row(
+                    horizontalArrangement = Arrangement.spacedBy(8.dp),
+                    verticalAlignment = Alignment.CenterVertically
+                ) {
+                    Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null)
+                    Text("Manage Wallets (Sub-accounts)")
+                }
+            }
+
             Text(
                 text = "Currency",
                 style = MaterialTheme.typography.titleMedium,
diff --git a/app/src/main/java/com/oatrice/jarwise/ui/managewallets/AddEditWalletDialog.kt b/app/src/main/java/com/oatrice/jarwise/ui/managewallets/AddEditWalletDialog.kt
new file mode 100644
index 0000000..3711a83
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/ui/managewallets/AddEditWalletDialog.kt
@@ -0,0 +1,194 @@
+package com.oatrice.jarwise.ui.managewallets
+
+import androidx.compose.foundation.background
+import androidx.compose.foundation.clickable
+import androidx.compose.foundation.layout.*
+import androidx.compose.foundation.rememberScrollState
+import androidx.compose.foundation.verticalScroll
+import androidx.compose.foundation.shape.RoundedCornerShape
+import androidx.compose.material.icons.Icons
+import androidx.compose.material.icons.rounded.ArrowDropDown
+import androidx.compose.material.icons.rounded.Check
+import androidx.compose.material.icons.rounded.Close
+import androidx.compose.material.icons.rounded.Wallet
+import androidx.compose.material3.*
+import androidx.compose.runtime.*
+import androidx.compose.ui.Alignment
+import androidx.compose.ui.Modifier
+import androidx.compose.ui.graphics.Color
+import androidx.compose.ui.unit.dp
+import androidx.compose.ui.window.Dialog
+import com.oatrice.jarwise.model.Wallet
+import com.oatrice.jarwise.ui.theme.Blue500
+import com.oatrice.jarwise.ui.theme.Gray100
+import com.oatrice.jarwise.ui.theme.Gray400
+import com.oatrice.jarwise.ui.theme.Gray700
+import com.oatrice.jarwise.ui.theme.Gray800
+import com.oatrice.jarwise.ui.theme.Gray900
+
+@OptIn(ExperimentalMaterial3Api::class)
+@Composable
+fun AddEditWalletDialog(
+    onDismiss: () -> Unit,
+    onSave: (name: String, parentId: String?) -> Unit,
+    allWallets: List<Wallet>,
+    editingWallet: Wallet? = null // Null = Add Mode
+) {
+    var name by remember { mutableStateOf(editingWallet?.name ?: "") }
+    var parentId by remember { mutableStateOf(editingWallet?.parentId) }
+    var expanded by remember { mutableStateOf(false) }
+
+    // Logic to filter available parents:
+    // 1. Cannot be self
+    // 2. Cannot be a descendant (Circular Dependency)
+    val availableParents = remember(allWallets, editingWallet) {
+        if (editingWallet == null) {
+            allWallets // Adding new: can pick anyone
+        } else {
+            allWallets.filter { candidate ->
+                if (candidate.id == editingWallet.id) return@filter false // Cannot be self
+                // Check descendant
+                !isDescendant(allWallets, candidate.id, editingWallet.id)
+            }
+        }
+    }
+
+    val selectedParentName = availableParents.find { it.id == parentId }?.name ?: "No Parent (Top Level)"
+
+    Dialog(onDismissRequest = onDismiss) {
+        Card(
+            shape = RoundedCornerShape(16.dp),
+            colors = 
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
