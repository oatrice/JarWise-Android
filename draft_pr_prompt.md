# PR Draft Prompt

You are an AI assistant helping to create a Pull Request description.
    
TASK: [Feature] Transaction Linking & Transfers
ISSUE: {
  "title": "[Feature] Transaction Linking & Transfers",
  "number": 71
}

GIT CONTEXT:
COMMITS:
0efd5de ✨ feat(transactions): add transfer functionality and improve transaction flow
f4d1f2d ✨ feat(transactions): add transaction type support and improve navigation
d0077c3 🐛 fix(transactions): optimize linked transaction lookups and fix total spent calculation
1fd26e3 ✨ feat(ui): enhance transaction display for transfers
856c916 ✨ feat(transactions): add transfer handling and domain module
832844f ✨ feat(transactions): add transfer functionality
0829f3d 🚀 feat(migration): implement money manager data migration

STATS:
.luma_state.json                                   |  18 +-
 CHANGELOG.md                                       |  11 +
 README.md                                          |   3 +-
 app/build.gradle.kts                               |   2 +-
 .../com.oatrice.jarwise.data.AppDatabase/7.json    | 294 +++++++++++++++++++++
 .../java/com/oatrice/jarwise/JarWiseApplication.kt |   4 +-
 .../main/java/com/oatrice/jarwise/MainActivity.kt  |  52 +++-
 .../java/com/oatrice/jarwise/data/AppDatabase.kt   |   8 +-
 .../com/oatrice/jarwise/data/GeneratedMockData.kt  |   6 +-
 .../java/com/oatrice/jarwise/data/Transaction.kt   |   3 +-
 .../com/oatrice/jarwise/data/TransactionDao.kt     |  11 +-
 .../data/repository/TransactionRepository.kt       |  81 ++++++
 .../main/java/com/oatrice/jarwise/di/DataModule.kt |   3 +-
 .../java/com/oatrice/jarwise/di/DomainModule.kt    |  10 +
 .../com/oatrice/jarwise/di/RepositoryModule.kt     |   1 +
 .../java/com/oatrice/jarwise/di/ViewModelModule.kt |   2 +-
 .../domain/use_case/CreateTransferUseCase.kt       |  87 ++++++
 .../domain/use_case/UnlinkTransactionsUseCase.kt   |  17 ++
 .../com/oatrice/jarwise/ui/AddTransactionScreen.kt | 150 ++++++++---
 .../java/com/oatrice/jarwise/ui/DashboardScreen.kt |  18 +-
 .../java/com/oatrice/jarwise/ui/MainViewModel.kt   |  40 ++-
 .../oatrice/jarwise/ui/TransactionHistoryScreen.kt |  17 +-
 .../jarwise/ui/components/TransactionCard.kt       |  50 +++-
 .../java/com/oatrice/jarwise/utils/Constants.kt    |   5 +
 .../jarwise/utils/TransactionGroupingUtils.kt      |  11 +-
 .../domain/use_case/CreateTransferUseCaseTest.kt   |  84 ++++++
 code_review.md                                     | 205 +++++++-------
 draft_pr_body.md                                   | 180 +++++--------
 draft_pr_prompt.md                                 |   5 +-
 29 files changed, 1060 insertions(+), 318 deletions(-)

KEY FILE DIFFS:
diff --git a/app/build.gradle.kts b/app/build.gradle.kts
index 5a147d9..1ac022a 100644
--- a/app/build.gradle.kts
+++ b/app/build.gradle.kts
@@ -15,7 +15,7 @@ android {
         minSdk = 24
         targetSdk = 34
         versionCode = 1
-        versionName = "1.8.0"
+        versionName = "1.9.0"
 
         testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
         vectorDrawables {
diff --git a/app/src/main/java/com/oatrice/jarwise/JarWiseApplication.kt b/app/src/main/java/com/oatrice/jarwise/JarWiseApplication.kt
index d8973a6..1f6b354 100644
--- a/app/src/main/java/com/oatrice/jarwise/JarWiseApplication.kt
+++ b/app/src/main/java/com/oatrice/jarwise/JarWiseApplication.kt
@@ -7,6 +7,7 @@ import com.oatrice.jarwise.di.networkModule
 import com.oatrice.jarwise.di.repositoryModule
 import com.oatrice.jarwise.di.viewModelModule
 import com.oatrice.jarwise.di.authModule
+import com.oatrice.jarwise.di.domainModule
 import org.koin.android.ext.koin.androidContext
 import org.koin.android.ext.koin.androidLogger
 import org.koin.core.context.GlobalContext.startKoin
@@ -27,7 +28,8 @@ class JarWiseApplication : Application() {
                     repositoryModule,
                     viewModelModule,
                     authModule,
-                    networkModule
+                    networkModule,
+                    domainModule
                 )
             }
             
diff --git a/app/src/main/java/com/oatrice/jarwise/MainActivity.kt b/app/src/main/java/com/oatrice/jarwise/MainActivity.kt
index a58d644..2a0dc92 100644
--- a/app/src/main/java/com/oatrice/jarwise/MainActivity.kt
+++ b/app/src/main/java/com/oatrice/jarwise/MainActivity.kt
@@ -62,6 +62,9 @@ class MainActivity : ComponentActivity() {
                 val currentUser by authService.currentUser.collectAsState()
                 val initialScreen = if (currentUser != null) Screen.Dashboard else Screen.Login
                 var currentScreen by remember { mutableStateOf<Screen>(initialScreen) }
+                // Track previous screen for ManageWallets (Dashboard vs Settings)
+                var previousScreen by remember { mutableStateOf<Screen?>(null) }
+                
                 val transactions by viewModel.transactions.collectAsState()
                 val formattedTotalBalance by viewModel.formattedTotalBalance.collectAsState()
                 val selectedCurrency by viewModel.selectedCurrency.collectAsState()
@@ -71,9 +74,16 @@ class MainActivity : ComponentActivity() {
                     when (page) {
                         com.oatrice.jarwise.ui.components.NavPage.DASHBOARD -> currentScreen = Screen.Dashboard
                         com.oatrice.jarwise.ui.components.NavPage.HISTORY -> currentScreen = Screen.TransactionHistory
-                        com.oatrice.jarwise.ui.components.NavPage.ADD -> currentScreen = Screen.AddTransaction
-                        // Add other destinations here when ready (WALLET, PROFILE)
-                        else -> {}
+                        com.oatrice.jarwise.ui.components.NavPage.ADD -> {
+                            previousScreen = currentScreen
+                            currentScreen = Screen.AddTransaction
+                        }
+                        com.oatrice.jarwise.ui.components.NavPage.BUDGET -> {
+                            // Wallets update via StateFlow automatically
+                            previousScreen = currentScreen // likely Dashboard or whatever tab
+                            currentScreen = Screen.ManageWallets
+                        }
+                        com.oatrice.jarwise.ui.components.NavPage.PROFILE -> currentScreen = Screen.Settings
                     }
                 }
 
@@ -92,7 +102,10 @@ class MainActivity : ComponentActivity() {
                                 onNavigateToHistory = { currentScreen = Screen.TransactionHistory },
                                 onNavigateToScan = { currentScreen = Screen.Scan },
                                 onNavigateToImport = { currentScreen = Screen.SlipImport },
-                                onNavigateToAdd = { currentScreen = Screen.AddTransaction },
+                                onNavigateToAdd = { 
+                                    previousScreen = Screen.Dashboard
+                                    currentScreen = Screen.AddTransaction 
+                                },
                                 onNavigateToSettings = { currentScreen = Screen.Settings },
                                 onNavigateToManageJars = {
                                     manageJarsViewModel.revertUnsavedChanges()
@@ -103,7 +116,10 @@ class MainActivity : ComponentActivity() {
                         }
                         is Screen.Settings -> SettingsScreen(
                              onBack = { currentScreen = Screen.Dashboard },
-                             onNavigateToManageWallets = { currentScreen = Screen.ManageWallets },
+                             onNavigateToManageWallets = { 
+                                 previousScreen = Screen.Settings
+                                 currentScreen = Screen.ManageWallets 
+                             },
                              onNavigateToMigration = { currentScreen = Screen.Migration },
                              viewModel = viewModel
                         )
@@ -155,7 +171,7 @@ class MainActivity : ComponentActivity() {
                                     val date = parsedSlip.date?.let {
                                         slipDateFormat.format(it)
                                     }
-                                    viewModel.saveTransaction(amount, jarId, "wallet-bank", note, date)
+                                    viewModel.saveTransaction(amount, jarId, "wallet-bank", note, date, "expense")
                                     android.widget.Toast.makeText(applicationContext, "Slip saved successfully", android.widget.Toast.LENGTH_SHORT).show()
                                 },
                                 onSaveDraft = { _, parsedSlip, jarId ->
@@ -164,16 +180,25 @@ class MainActivity : ComponentActivity() {
                                     val date = parsedSlip.date?.let {
                                         slipDateFormat.format(it)
                                     }
-                                    viewModel.saveDraft(amount, jarId, "wallet-bank", note, date)
+                                    viewModel.saveDraft(amount, jarId, "wallet-bank", note, date, "expense")
                                     android.widget.Toast.makeText(applicationContext, "Draft saved!", android.widget.Toast.LENGTH_SHORT).show()
                                 }
                             )
                         }
                         is Screen.AddTransaction -> AddTransactionScreen(
-                            onBack = { currentScreen = Screen.Dashboard },
-                            onSave = { amount, jarId, walletId, note, date ->
-                                viewModel.saveTransaction(amount, jarId, walletId, note, date)
-                                currentScreen = Screen.Dashboard
+                            onBack = { 
+                                currentScreen = previousScreen ?: Screen.Dashboard 
+                                previousScreen = null
+                            },
+                            onSave = { amount, jarId, walletId, note, date, type ->
+                                viewModel.saveTransaction(amount, jarId, walletId, note, date, type)
+                                currentScreen = previousScreen ?: Screen.Dashboard
+                                previousScreen = null
+                            },
+                            onSaveTransfer = { amount, fromWalletId, toWalletId, note, date ->
+                                viewModel.saveTransfer(amount, fromWalletId, toWalletId, note, date)
+                                currentScreen = previousScreen ?: Screen.Dashboard
+                                previousScreen = null
                             }
                         )
                         is Screen.ManageJars -> ManageJarsScreen(
@@ -181,7 +206,10 @@ class MainActivity : ComponentActivity() {
                             onBack = { currentScreen = Screen.Dashboard }
                         )
                         is Screen.ManageWallets -> com.oatrice.jarwise.ui.managewallets.ManageWalletsScreen(
-                            onNavigateBack = { currentScreen = Screen.Settings },
+                            onNavigateBack = { 
+                                currentScreen = previousScreen ?: Screen.Dashboard
+                                previousScreen = null // clear after use
+                            },
                             viewModel = manageWalletsViewModel
                         )
                         is Screen.Login -> com.oatrice.jarwise.ui.login.LoginScreen(
diff --git a/app/src/main/java/com/oatrice/jarwise/data/AppDatabase.kt b/app/src/main/java/com/oatrice/jarwise/data/AppDatabase.kt
index 145e165..652f7a4 100644
--- a/app/src/main/java/com/oatrice/jarwise/data/AppDatabase.kt
+++ b/app/src/main/java/com/oatrice/jarwise/data/AppDatabase.kt
@@ -5,7 +5,7 @@ import androidx.room.RoomDatabase
 import androidx.room.migration.Migration
 import androidx.sqlite.db.SupportSQLiteDatabase
 
-@Database(entities = [Transaction::class, JarConfig::class, Allocation::class, WalletEntity::class], version = 6, exportSchema = true)
+@Database(entities = [Transaction::class, JarConfig::class, Allocation::class, WalletEntity::class], version = 7, exportSchema = true)
 abstract class AppDatabase : RoomDatabase() {
     abstract fun transactionDao(): TransactionDao
     abstract fun jarConfigDao(): JarConfigDao
@@ -99,6 +99,12 @@ abstract class AppDatabase : RoomDatabase() {
             }
         }
 
+        val MIGRATION_6_7 = object : Migration(6, 7) {
+            override fun migrate(db: SupportSQLiteDatabase) {
+                db.execSQL("ALTER TABLE transactions ADD COLUMN linkedTransactionId TEXT")
+            }
+        }
+
         val SEED_CALLBACK = object : RoomDatabase.Callback() {
             override fun onCreate(db: SupportSQLiteDatabase) {
                 super.onCreate(db)
diff --git a/app/src/main/java/com/oatrice/jarwise/data/GeneratedMockData.kt b/app/src/main/java/com/oatrice/jarwise/data/GeneratedMockData.kt
index 8c56eb4..2c96ade 100644
--- a/app/src/main/java/com/oatrice/jarwise/data/GeneratedMockData.kt
+++ b/app/src/main/java/com/oatrice/jarwise/data/GeneratedMockData.kt
@@ -123,7 +123,7 @@ object GeneratedMockData {
         Transaction(
             id = "1",
             merchant = "Spotify Premium",
-            amount = -12.99,
+            amount = 12.99,
             category = "Play",
             date = "Today, 10:43 AM",
             icon = Icons.Rounded.Headphones,
@@ -133,7 +133,7 @@ object GeneratedMockData {
         Transaction(
             id = "2",
             merchant = "Whole Foods Market",
-            amount = -142.5,
+            amount = 142.5,
             category = "Necessities",
             date = "Yesterday, 6:30 PM",
             icon = Icons.Rounded.ShoppingBag,
@@ -143,7 +143,7 @@ object GeneratedMockData {
         Transaction(
             id = "3",
             merchant = "Udemy Course",
-            amount = -24.99,
+            amount = 24.99,
             category = "Education",
             date = "Dec 28, 2025",
             icon = Icons.Rounded.School,
diff --git a/app/src/main/java/com/oatrice/jarwise/data/Transaction.kt b/app/src/main/java/com/oatrice/jarwise/data/Transaction.kt
index c29ffa8..462cb00 100644
--- a/app/src/main/java/com/oatrice/jarwise/data/Transaction.kt
+++ b/app/src/main/java/com/oatrice/jarwise/data/Transaction.kt
@@ -12,6 +12,7 @@ data class Transaction(
     val walletId: String = "wallet-cash", // Default to cash
     val date: String, // ISO 8601 string
     val type: String = "expense", // "income" | "expense"
-    val status: String = "completed" // "draft" | "completed"
+    val status: String = "completed", // "draft" | "completed"
+    val linkedTransactionId: String? = null // ID of the related transaction (e.g. for transfers)
 )
 
diff --git a/app/src/main/java/com/oatrice/jarwise/data/TransactionDao.kt b/app/src/main/java/com/oatrice/jarwise/data/TransactionDao.kt
index 7bf879f..90d4c58 100644
--- a/app/src/main/java/com/oatrice/jarwise/data/TransactionDao.kt
+++ b/app/src/main/java/com/oatrice/jarwise/data/TransactionDao.kt
@@ -18,7 +18,7 @@ interface TransactionDao {
     fun getDraftCount(): Flow<Int>
 
     @Insert
-    suspend fun insert(transaction: Transaction)
+    suspend fun insert(transaction: Transaction): Long
 
     @Update
     suspend fun update(transaction: Transaction)
@@ -26,6 +26,15 @@ interface TransactionDao {
     @Query("UPDATE transactions SET status = :status WHERE id = :id")
     suspend fun updateStatus(id: Long, status: String)
     
+    @androidx.room.Delete
+    suspend fun delete(transaction: Transaction)
+
+    @Query("UPDATE transactions SET linkedTransactionId = NULL WHERE id = :id")
+    suspend fun unlinkTransaction(id: Long)
+
+    @Query("UPDATE transactions SET linkedTransactionId = NULL WHERE linkedTransactionId = :idStr")
+    suspend fun unlinkRelatedTransaction(idStr: String)
+
     @Query("DELETE FROM transactions")
     suspend fun deleteAll()
 }
diff --git a/app/src/main/java/com/oatrice/jarwise/data/repository/TransactionRepository.kt b/app/src/main/java/com/oatrice/jarwise/data/repository/TransactionRepository.kt
new file mode 100644
index 0000000..7b7900e
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/data/repository/TransactionRepository.kt
@@ -0,0 +1,81 @@
+package com.oatrice.jarwise.data.repository
+
+import androidx.room.withTransaction
+import com.oatrice.jarwise.data.AppDatabase
+import com.oatrice.jarwise.data.Transaction
+import com.oatrice.jarwise.data.TransactionDao
+import kotlinx.coroutines.flow.Flow
+import javax.inject.Inject
+
+interface TransactionRepository {
+    fun getAllTransactions(): Flow<List<Transaction>>
+    suspend fun insertTransaction(transaction: Transaction)
+    suspend fun updateTransaction(transaction: Transaction)
+    suspend fun deleteTransaction(transaction: Transaction)
+    suspend fun createTransfer(expenseTransaction: Transaction, incomeTransaction: Transaction)
+    suspend fun unlinkTransaction(transactionId: Long)
+}
+
+class TransactionRepositoryImpl @Inject constructor(
+    private val db: AppDatabase,
+    private val transactionDao: TransactionDao
+) : TransactionRepository {
+
+    override fun getAllTransactions(): Flow<List<Transaction>> {
+        return transactionDao.getAll()
+    }
+
+    override suspend fun insertTransaction(transaction: Transaction) {
+        transactionDao.insert(transaction)
+    }
+
+    override suspend fun updateTransaction(transaction: Transaction) {
+        transactionDao.update(transaction)
+    }
+
+    override suspend fun deleteTransaction(transaction: Transaction) {
+        transactionDao.delete(transaction)
+    }
+
+    override suspend fun createTransfer(expenseTransaction: Transaction, incomeTransaction: Transaction) {
+        db.withTransaction {
+            // 1. Insert Expense
+            val expenseId = transactionDao.insert(expenseTransaction)
+            
+            // 2. Insert Income, linked to Expense
+            val incomeWithLink = incomeTransaction.copy(linkedTransactionId = expenseId.toString())
+            val incomeId = transactionDao.insert(incomeWithLink)
+            
+            // 3. Update Expense, linked to Income
+            val expenseWithLink = expenseTransaction.copy(id = expenseId, linkedTransactionId = incomeId.toString())
+            transactionDao.update(expenseWithLink)
+        }
+    }
+
+    override suspend fun unlinkTransaction(transactionId: Long) {
+        db.withTransaction {
+            // Logic to find and unlink should be handled by UseCase or here if we want to be atomic on ID
+            // Ideally, we fetch the transaction, find the linked one, and set both to null.
+            // But since this is a repository method, let's assume we just update the specific one or let UseCase handle the logic.
+            // Given the complexity of "Unlinking logic: ... remove current and reciprocal link",
+            // it's safer to have a specific method in DAO to nullify linkedTransactionId for a given ID?
+            // "UPDATE transactions SET linkedTransactionId = NULL WHERE id = :id OR linkedTransactionId = :id"
+            // Wait, that's dangerous if IDs overlap (unlikely with UUIDs but here we use Long auto-inc? No, Transaction has Long ID).
+            // Transaction entity uses `val id: Long = 0`.
+            // But `linkedTransactionId` is String? Let me check Transaction.kt again.
+            // Yes, `linkedTransactionId: String?`. This seems like a mismatch if ID is Long.
+            // Ah, the plan said "val linkedTransactionId: String?".
+            // But Transaction.kt has "@PrimaryKey(autoGenerate = true) val id: Long = 0".
+            // So linkedTransactionId should probably be Long? Or we convert Long to String.
+            // Let's assume we store Long as String or change linkedTransactionId to Long.
+            // A String is more flexible if we change IDs later (UUID), but for now it's Long.
+            // Let's stick to String to match the prompt/plan, but we must be careful.
+            
+            // To implement Unlink:
+            // We need to find the transaction with this ID, get its linked ID.
+            // Then update both to null.
+            transactionDao.unlinkTransaction(transactionId)
+            transactionDao.unlinkRelatedTransaction(transactionId.toString())
+        }
+    }
+}
diff --git a/app/src/main/java/com/oatrice/jarwise/di/DataModule.kt b/app/src/main/java/com/oatrice/jarwise/di/DataModule.kt
index fc2c58a..232de89 100644
--- a/app/src/main/java/com/oatrice/jarwise/di/DataModule.kt
+++ b/app/src/main/java/com/oatrice/jarwise/di/DataModule.kt
@@ -19,7 +19,8 @@ val dataModule = module {
                 AppDatabase.MIGRATION_2_3,
                 AppDatabase.MIGRATION_3_4,
                 AppDatabase.MIGRATION_4_5,
-                AppDatabase.MIGRATION_5_6
+                AppDatabase.MIGRATION_5_6,
+                AppDatabase.MIGRATION_6_7
             )
             .addCallback(AppDatabase.SEED_CALLBACK)
             .fallbackToDestructiveMigration()
diff --git a/app/src/main/java/com/oatrice/jarwise/di/DomainModule.kt b/app/src/main/java/com/oatrice/jarwise/di/DomainModule.kt
new file mode 100644
index 0000000..f80d9f5
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/di/DomainModule.kt
@@ -0,0 +1,10 @@
+package com.oatrice.jarwise.di
+
+import com.oatrice.jarwise.domain.use_case.CreateTransferUseCase
+import com.oatrice.jarwise.domain.use_case.UnlinkTransactionsUseCase
+import org.koin.dsl.module
+
+val domainModule = module {
+    factory { CreateTransferUseCase(get()) }
+    factory { UnlinkTransactionsUseCase(get()) }
+}
diff --git a/app/src/main/java/com/oatrice/jarwise/di/RepositoryModule.kt b/app/src/main/java/com/oatrice/jarwise/di/RepositoryModule.kt
index 0daa853..f2ba044 100644
--- a/app/src/main/java/com/oatrice/jarwise/di/RepositoryModule.kt
+++ b/app/src/main/java/com/oatrice/jarwise/di/RepositoryModule.kt
@@ -11,4 +11,5 @@ val repositoryModule = module {
     single { WalletRepository(get()) }
     single { SlipRepository(androidContext()) }
     single { MigrationRepository(get(), androidContext()) }
+    single<TransactionRepository> { TransactionRepositoryImpl(get(), get()) }
 }
diff --git a/app/src/main/java/com/oatrice/jarwise/di/ViewModelModule.kt b/app/src/main/java/com/oatrice/jarwise/di/ViewModelModule.kt
index 37a89ce..621c409 100644
-
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
