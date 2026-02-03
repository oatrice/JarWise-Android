# PR Draft Prompt

You are an AI assistant helping to create a Pull Request description.
    
TASK: [Web | Android] Google Login & Cloud Backup
ISSUE: {
  "title": "[Web | Android] Google Login & Cloud Backup",
  "number": 32
}

GIT CONTEXT:
COMMITS:
c718489 ✨ feat(auth): add google sign-in and cloud backup features
33a6505 🐛 fix(manage-jars): revert unsaved changes on navigation
9264c15 🐛 fix(ui): improve reset and date formatting
d836cdd ✨ feat(settings): add backup checking and restore functionality
655fdc0 🔄 feat(ui): add app restart after data deletion
8785155 ✨ feat(settings): add local data deletion option on logout
530a181 🐛 fix(backup): improve restore process with temp file and cleanup
e0dc9d3 ✨ feat(backup): Add backup restore UI and flow
c282075 🐛 fix(db): clean up WAL/SHM files after backup restore
9f47a81 🐛 fix(backup): add debug logging for restored data
1db2f90 ✨ feat(auth): implement google sign-in flow in settings
77e4b45 ✨ feat(login): add guest login option and settings navigation
728b22c ✨ feat(backup): Add backup restore functionality to login flow
76557c6 🔐 feat(auth): implement initial screen routing based on auth state
b0d87ac 🐛 ui(backup): Pause auto-backup during ManageJarsScreen
f998aff 🐛 fix(backup): Add auto-backup pause/resume functionality
b4448d6 🔄 refactor(backup): separate manual and auto backup triggers
6685f7c ✨ feat(settings): add settings screen and view model
deb23ec ✨ feat(backup): Add folder creation for Google Drive backups
c980b93 ✨ feat(backup): enhance backup file naming and logging
21e5b4f 🐛 auth(ui): improve error handling and UI state updates
0007de5 ✨ feat(logging): Add comprehensive logging to backup operations
692bb78 ✨ feat(backup): add automatic database backup to Google Drive
ba3c82a ✨ feat(ui): improve login success flow
5fc3f4a ✨ feat(ui): improve login screen composition and state handling
152c351 ✨ feat(auth): implement Google Sign-In integration
c37bcca ✨ feat(auth): add google authentication integration

STATS:
.idea/appInsightsSettings.xml                      |   8 +-
 .luma_state.json                                   |  16 +-
 CHANGELOG.md                                       |  15 +
 README.md                                          |   5 +-
 app/build.gradle.kts                               |  12 +-
 app/google-services.json                           |  39 +++
 .../java/com/oatrice/jarwise/JarWiseApplication.kt |  21 +-
 .../main/java/com/oatrice/jarwise/MainActivity.kt  |  37 ++-
 .../com/oatrice/jarwise/data/auth/AuthService.kt   |   9 +
 .../java/com/oatrice/jarwise/data/auth/AuthUser.kt |   8 +
 .../oatrice/jarwise/data/auth/GoogleAuthService.kt |  90 ++++++
 .../oatrice/jarwise/data/auth/MockAuthService.kt   |  25 ++
 .../oatrice/jarwise/data/backup/BackupManager.kt   | 180 ++++++++++++
 .../jarwise/data/backup/CloudStorageService.kt     |  16 ++
 .../jarwise/data/backup/GoogleDriveService.kt      | 132 +++++++++
 .../main/java/com/oatrice/jarwise/di/AppModule.kt  |   5 +-
 .../main/java/com/oatrice/jarwise/di/AuthModule.kt |  10 +
 .../main/java/com/oatrice/jarwise/di/DataModule.kt |  14 +
 .../java/com/oatrice/jarwise/di/ViewModelModule.kt |   8 +-
 .../java/com/oatrice/jarwise/ui/MainViewModel.kt   |   4 +-
 .../java/com/oatrice/jarwise/ui/SettingsScreen.kt  | 311 ++++++++++++++++++++-
 .../com/oatrice/jarwise/ui/login/LoginScreen.kt    | 238 ++++++++++++++++
 .../com/oatrice/jarwise/ui/login/LoginUiState.kt   |  13 +
 .../com/oatrice/jarwise/ui/login/LoginViewModel.kt | 126 +++++++++
 .../jarwise/ui/managejars/ManageJarsScreen.kt      |  19 +-
 .../jarwise/ui/managejars/ManageJarsViewModel.kt   | 190 +++++++++----
 .../jarwise/ui/settings/SettingsViewModel.kt       | 116 ++++++++
 .../java/com/oatrice/jarwise/utils/AppLogger.kt    |  18 ++
 .../oatrice/jarwise/data/auth/AuthServiceTest.kt   |  41 +++
 .../jarwise/data/backup/BackupManagerTest.kt       | 184 ++++++++++++
 .../oatrice/jarwise/ui/login/LoginViewModelTest.kt |  55 ++++
 .../ui/managejars/ManageJarsViewModelTest.kt       |  59 +++-
 build.gradle.kts                                   |   1 +
 code_review.md                                     |  26 +-
 gradle/libs.versions.toml                          |  15 +
 35 files changed, 1943 insertions(+), 123 deletions(-)

KEY FILE DIFFS:
diff --git a/.idea/appInsightsSettings.xml b/.idea/appInsightsSettings.xml
index 371f2e2..2f0ada9 100644
--- a/.idea/appInsightsSettings.xml
+++ b/.idea/appInsightsSettings.xml
@@ -8,10 +8,10 @@
             <InsightsFilterSettings>
               <option name="connection">
                 <ConnectionSetting>
-                  <option name="appId" value="PLACEHOLDER" />
-                  <option name="mobileSdkAppId" value="" />
-                  <option name="projectId" value="" />
-                  <option name="projectNumber" value="" />
+                  <option name="appId" value="com.oatrice.jarwise" />
+                  <option name="mobileSdkAppId" value="1:310508345743:android:a4949aec7042700eec13fa" />
+                  <option name="projectId" value="jarwise-197b3" />
+                  <option name="projectNumber" value="310508345743" />
                 </ConnectionSetting>
               </option>
               <option name="signal" value="SIGNAL_UNSPECIFIED" />
diff --git a/app/build.gradle.kts b/app/build.gradle.kts
index 645f57b..f505630 100644
--- a/app/build.gradle.kts
+++ b/app/build.gradle.kts
@@ -2,18 +2,20 @@ plugins {
     alias(libs.plugins.android.application)
     alias(libs.plugins.jetbrains.kotlin.android)
     alias(libs.plugins.ksp)
+    alias(libs.plugins.google.services)
 }
 
 android {
     namespace = "com.oatrice.jarwise"
     compileSdk = 34
+    buildToolsVersion = "34.0.0"
 
     defaultConfig {
         applicationId = "com.oatrice.jarwise"
         minSdk = 24
         targetSdk = 34
         versionCode = 1
-        versionName = "1.6.0"
+        versionName = "1.7.0"
 
         testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
         vectorDrawables {
@@ -52,6 +54,7 @@ android {
     packaging {
         resources {
             excludes += "/META-INF/{AL2.0,LGPL2.1}"
+            excludes += "META-INF/DEPENDENCIES"
         }
     }
 
@@ -137,6 +140,13 @@ dependencies {
     implementation("io.insert-koin:koin-android:3.5.3")
     implementation("io.insert-koin:koin-androidx-compose:3.5.3")
 
+    // Google Auth & Drive
+    implementation(libs.play.services.auth)
+    implementation(libs.google.api.client.android)
+    implementation(libs.google.api.services.drive)
+    implementation(libs.google.auth.library.oauth2.http)
+    implementation(libs.kotlinx.coroutines.play.services)
+    implementation(libs.google.http.client.android)
 }
 
 ksp {
diff --git a/app/src/main/java/com/oatrice/jarwise/JarWiseApplication.kt b/app/src/main/java/com/oatrice/jarwise/JarWiseApplication.kt
index af6811e..dba61fc 100644
--- a/app/src/main/java/com/oatrice/jarwise/JarWiseApplication.kt
+++ b/app/src/main/java/com/oatrice/jarwise/JarWiseApplication.kt
@@ -5,6 +5,7 @@ import com.oatrice.jarwise.di.appModule
 import com.oatrice.jarwise.di.dataModule
 import com.oatrice.jarwise.di.repositoryModule
 import com.oatrice.jarwise.di.viewModelModule
+import com.oatrice.jarwise.di.authModule
 import org.koin.android.ext.koin.androidContext
 import org.koin.android.ext.koin.androidLogger
 import org.koin.core.context.GlobalContext.startKoin
@@ -16,16 +17,32 @@ class JarWiseApplication : Application() {
         super.onCreate()
 
         if (GlobalContext.getOrNull() == null) {
-            startKoin {
+            val koinApp = startKoin {
                 androidLogger()
                 androidContext(this@JarWiseApplication)
                 modules(
                     appModule,
                     dataModule,
                     repositoryModule,
-                    viewModelModule
+                    viewModelModule,
+                    authModule
                 )
             }
+            
+            // Setup Auto Backup
+            val koin = koinApp.koin
+            val db = koin.get<com.oatrice.jarwise.data.AppDatabase>()
+            val backupManager = koin.get<com.oatrice.jarwise.data.backup.BackupManager>()
+            
+            db.invalidationTracker.addObserver(
+                object : androidx.room.InvalidationTracker.Observer(
+                    "transactions", "allocations", "wallets"
+                ) {
+                    override fun onInvalidated(tables: Set<String>) {
+                        backupManager.triggerAutoBackup()
+                    }
+                }
+            )
         }
     }
 }
diff --git a/app/src/main/java/com/oatrice/jarwise/MainActivity.kt b/app/src/main/java/com/oatrice/jarwise/MainActivity.kt
index 00208cf..142ade8 100644
--- a/app/src/main/java/com/oatrice/jarwise/MainActivity.kt
+++ b/app/src/main/java/com/oatrice/jarwise/MainActivity.kt
@@ -14,6 +14,7 @@ import com.oatrice.jarwise.ui.managejars.ManageJarsScreen
 import com.oatrice.jarwise.ui.managejars.ManageJarsViewModel
 import com.oatrice.jarwise.ui.managewallets.ManageWalletsViewModel
 import org.koin.androidx.viewmodel.ext.android.viewModel
+import org.koin.android.ext.android.inject
 import com.oatrice.jarwise.ui.AddTransactionScreen
 import com.oatrice.jarwise.ui.DashboardScreen
 import com.oatrice.jarwise.ui.MainViewModel
@@ -34,6 +35,11 @@ sealed class Screen {
     data object Settings : Screen()
     data object ManageJars : Screen()
     data object ManageWallets : Screen()
+    data object Login : Screen()
+}
+
+private val slipDateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
+    timeZone = java.util.TimeZone.getTimeZone("UTC")
 }
 
 class MainActivity : ComponentActivity() {
@@ -44,11 +50,16 @@ class MainActivity : ComponentActivity() {
         val slipViewModel: SlipViewModel by viewModel()
         val manageJarsViewModel: ManageJarsViewModel by viewModel()
         val manageWalletsViewModel: ManageWalletsViewModel by viewModel()
+        
+        // Inject AuthService to check login status
+        val authService: com.oatrice.jarwise.data.auth.AuthService by inject()
 
         enableEdgeToEdge()
         setContent {
             JarWiseTheme {
-                var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
+                // Determine initial screen based on auth state
+                val initialScreen = if (authService.currentUser.value != null) Screen.Dashboard else Screen.Login
+                var currentScreen by remember { mutableStateOf<Screen>(initialScreen) }
                 val transactions by viewModel.transactions.collectAsState()
                 val formattedTotalBalance by viewModel.formattedTotalBalance.collectAsState()
                 val selectedCurrency by viewModel.selectedCurrency.collectAsState()
@@ -81,7 +92,10 @@ class MainActivity : ComponentActivity() {
                                 onNavigateToImport = { currentScreen = Screen.SlipImport },
                                 onNavigateToAdd = { currentScreen = Screen.AddTransaction },
                                 onNavigateToSettings = { currentScreen = Screen.Settings },
-                                onNavigateToManageJars = { currentScreen = Screen.ManageJars },
+                                onNavigateToManageJars = {
+                                    manageJarsViewModel.revertUnsavedChanges()
+                                    currentScreen = Screen.ManageJars
+                                },
                                 onNavigate = handleNavigation
                             )
                         }
@@ -122,9 +136,7 @@ class MainActivity : ComponentActivity() {
                                     val amount = parsedSlip.amount ?: 0.0
                                     val note = "Slip: ${parsedSlip.bankName ?: "Unknown"}"
                                     val date = parsedSlip.date?.let {
-                                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
-                                        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
-                                        sdf.format(it)
+                                        slipDateFormat.format(it)
                                     }
                                     viewModel.saveTransaction(amount, jarId, "wallet-bank", note, date)
                                     android.widget.Toast.makeText(applicationContext, "Slip saved successfully", android.widget.Toast.LENGTH_SHORT).show()
@@ -133,9 +145,7 @@ class MainActivity : ComponentActivity() {
                                     val amount = parsedSlip.amount ?: 0.0
                                     val note = "Slip: ${parsedSlip.bankName ?: "Unknown"}"
                                     val date = parsedSlip.date?.let {
-                                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
-                                        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
-                                        sdf.format(it)
+                                        slipDateFormat.format(it)
                                     }
                                     viewModel.saveDraft(amount, jarId, "wallet-bank", note, date)
                                     android.widget.Toast.makeText(applicationContext, "Draft saved!", android.widget.Toast.LENGTH_SHORT).show()
@@ -155,13 +165,10 @@ class MainActivity : ComponentActivity() {
                         )
                         is Screen.ManageWallets -> com.oatrice.jarwise.ui.managewallets.ManageWalletsScreen(
                             onNavigateBack = { currentScreen = Screen.Settings },
-                            // viewModel = manageWalletsViewModel // Explicitly pass or let it use default if we change Screen signature
-                            // Current Screen composable likely uses default viewModel() which won't work with Factory unless provided via LocalViewModelStoreOwner or passed directly.
-                            // Assuming ManageWalletsScreen instantiates VM internally with koin/hilt or we need to pass it. 
-                            // Looking at ManageWalletsScreen.kt (Step 441 in history), it uses `viewModel: ManageWalletsViewModel = viewModel()`.
-                            // Without Hilt, `viewModel()` won't pick up the Factory associated with MainActivity unless we pass the *instance* or change how it's retrieved.
-                            // Correct approach for simple DI: Pass the viewModel instance created in MainActivity.
-                            viewModel = manageWalletsViewModel 
+                            viewModel = manageWalletsViewModel
+                        )
+                        is Screen.Login -> com.oatrice.jarwise.ui.login.LoginScreen(
+                            onLoginSuccess = { currentScreen = Screen.Dashboard }
                         )
                     }
                 }
diff --git a/app/src/main/java/com/oatrice/jarwise/data/auth/AuthService.kt b/app/src/main/java/com/oatrice/jarwise/data/auth/AuthService.kt
new file mode 100644
index 0000000..3edc576
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/data/auth/AuthService.kt
@@ -0,0 +1,9 @@
+package com.oatrice.jarwise.data.auth
+
+import kotlinx.coroutines.flow.StateFlow
+
+interface AuthService {
+    val currentUser: StateFlow<AuthUser?>
+    suspend fun signIn(): Result<AuthUser>
+    suspend fun signOut()
+}
diff --git a/app/src/main/java/com/oatrice/jarwise/data/auth/AuthUser.kt b/app/src/main/java/com/oatrice/jarwise/data/auth/AuthUser.kt
new file mode 100644
index 0000000..f8a4a17
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/data/auth/AuthUser.kt
@@ -0,0 +1,8 @@
+package com.oatrice.jarwise.data.auth
+
+data class AuthUser(
+    val id: String,
+    val name: String,
+    val email: String,
+    val photoUrl: String? = null
+)
diff --git a/app/src/main/java/com/oatrice/jarwise/data/auth/GoogleAuthService.kt b/app/src/main/java/com/oatrice/jarwise/data/auth/GoogleAuthService.kt
new file mode 100644
index 0000000..8e8afb5
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/data/auth/GoogleAuthService.kt
@@ -0,0 +1,90 @@
+package com.oatrice.jarwise.data.auth
+
+import android.content.Context
+import com.google.android.gms.auth.api.signin.GoogleSignIn
+import com.google.android.gms.auth.api.signin.GoogleSignInOptions
+import kotlinx.coroutines.flow.MutableStateFlow
+import kotlinx.coroutines.flow.StateFlow
+import kotlinx.coroutines.flow.asStateFlow
+import kotlinx.coroutines.tasks.await
+
+class GoogleAuthService(private val context: Context) : AuthService {
+    private val _currentUser = MutableStateFlow<AuthUser?>(null)
+    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()
+
+    init {
+        // Check for existing signed-in user
+        val account = GoogleSignIn.getLastSignedInAccount(context)
+        if (account != null) {
+            _currentUser.value = AuthUser(
+                id = account.id ?: "",
+                name = account.displayName ?: "",
+                email = account.email ?: "",
+                photoUrl = account.photoUrl?.toString()
+            )
+        }
+    }
+
+    override suspend fun signIn(): Result<AuthUser> {
+        // In real implementation, this needs Activity context or result launcher.
+        // For Service, we usually just return state or handle via repository.
+        // This is a simplified interface for now.
+        // Real Google Sign-In requires Activity interaction (startActivityForResult).
+        // So this method might need to be "handleSignInResult" or similar, 
+        // OR we inject a launcher helper. 
+        // For now, let's keep it consistent with interface but note limitations.
+        return Result.failure(Exception("Google Sign-In requires Activity interaction"))
+    }
+    
+    fun getSignInIntent(): android.content.Intent {
+        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
+            .requestEmail()
+            .requestProfile()
+            .requestScopes(com.google.android.gms.common.api.Scope(com.google.api.services.drive.DriveScopes.DRIVE_FILE), com.google.android.gms.common.api.Scope(com.google.api.services.drive.DriveScopes.DRIVE_APPDATA))
+            .build()
+        val client = GoogleSignIn.getClient(context, gso)
+        return client.signInIntent
+    }
+
+    suspend fun handleSignInResult(intent: android.content.Intent?): Result<AuthUser> {
+        return try {
+            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
+            val account = task.await()
+            val user = AuthUser(
+                id = account.id ?: "",
+                name = account.displayName ?: "",
+                email = account.email ?: "",
+                photoUrl = account.photoUrl?.toString()
+            )
+            _currentUser.value = user
+            android.util.Log.d("GoogleAuthService", "Sign-in successful: ${user.email} (${user.name})")
+            Result.success(user)
+        } catch (e: Exception) {
+            if (e is com.google.android.gms.common.api.ApiException) {
+                android.util.Log.e("GoogleAuthService", "Sign-in failed code: ${e.statusCode}, message: ${e.message}")
+                val message = when (e.statusCode) {
+                    com.google.android.gms.common.api.CommonStatusCodes.NETWORK_ERROR -> "Network error. Please check your connection."
+                    com.google.android.gms.common.api.CommonStatusCodes.INVALID_ACCOUNT -> "Invalid account."
+                    else -> "Sign-in failed: ${e.message}"
+                }
+                Result.failure<AuthUser>(Exception(message))
+            } else {
+                android.util.Log.e("GoogleAuthService", "Sign-in failed", e)
+                Result.failure<AuthUser>(e)
+            }
+        }
+    }
+
+    override suspend fun signOut() {
+        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
+        val client = GoogleSignIn.getClient(context, gso)
+        try {
+            client.signOut().await()
+            _currentUser.value = null
+        } catch (e: Exception) {
+            e.printStackTrace()
+            // Even if Google sign out fails, clear local state
+            _currentUser.value = null
+        }
+    }
+}
diff --git a/app/src/main/java/com/oatrice/jarwise/data/auth/MockAuthService.kt b/app/src/main/java/com/oatrice/jarwise/data/auth/MockAuthService.kt
new file mode 100644
index 0000000..0884b92
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/data/auth/MockAuthService.kt
@@ -0,0 +1,25 @@
+package com.oatrice.jarwise.data.auth
+
+import kotlinx.coroutines.flow.MutableStateFlow
+import kotlinx.coroutines.flow.StateFlow
+import kotlinx.coroutines.flow.asStateFlow
+
+class MockAuthService : AuthService {
+    private val _currentUser = MutableStateFlow<AuthUser?>(null)
+    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()
+
+    override suspend fun signIn(): Result<AuthUser> {
+        val user = AuthUser(
+            id = "mock_id_123",
+            name = "Mock User",
+            email = "mock@example.com",
+            photoUrl = "https://example.com/photo.jpg"
+        )
+        _currentUser.value = user
+        return Result.success(user)
+    }
+
+    override suspend fun signOut() {
+        _currentUser.value = null
+    }
+}
diff --git a/app/src/main/java/com/oatrice/jarwise/data/backup/BackupManager.kt b/app/src/main/java/com/oatrice/jarwise/data/backup/BackupManager.kt
new file mode 100644
index 0000000..ba4d342
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/data/backup/BackupManager.kt
@@ -0,0 +1,180 @@
+package com.oatrice.jarwise.data.backup
+
+import kotlinx.coroutines.CoroutineScope
+import kotlinx.coroutines.Job
+import kotlinx.coroutines.delay
+import kotlinx.coroutines.launch
+import java.io.File
+
+import kotlinx.coroutines.flow.MutableStateFlow
+import kotlinx.coroutines.flow.StateFlow
+import kotlinx.coroutines.flow.asStateFlow
+
+sealed class SyncStatus {
+    data object Idle : SyncStatus()
+    data object Syncing : SyncStatus()
+    data class Success(val lastSyncedTime: Long) : SyncStatus()
+    data class Error(val message: String) : SyncStatus()
+}
+
+class BackupManager(
+    private val cloudStorageService: CloudStorageService,
+    private val externalScope: CoroutineScope,
+    private val dbFileProvider: () -> File,
+    private val logger: com.oatrice.jarwise.utils.AppLogger
+) {
+    private var backupJob: Job? = null
+    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
+    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()
+    
+    companion object {
+        private const val DEBOUNCE_DELAY_MS = 10000L
+    }
+
+    private var isAutoBackupPaused = false
+    private var isBackupPending = false
+
+    fun setAutoBackupPaused(paused: Boolean) {
+        if (isAutoBackupPaused == paused) return
+        isAutoBackupPaused = paused
+        if (!paused && isBackupPending) {
+            isBackupPending = false
+            triggerAutoBackup()
+        }
+    }
+
+    fun triggerAutoBackup() {
+        if (isAutoBackupPaused) {
+            isBackupPending = true
+            return
+        }
+        
+        // Cancel previous job if it exists (debounce reset)
+        backupJob?.cancel()
+        
+        backupJob = externalScope.launch {
+            delay(DEBOUNCE_DELAY_MS)
+            performBackup()
+        }
+    }
+
+    fun triggerManualBackup() {
+        // Cancel any pending auto backup to avoid double upload if user clicks just after edit
+        backupJob?.cancel()
+        isBackupPending = false
+        
+        externalScope.launch {
+            performBackup()
+        }
+    }
+
+    private suspend fun performBackup() {
+        val file = dbFileProvider()
+        logger.d("BackupManager", "Start backup: ${file.name}")
+        _s
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
