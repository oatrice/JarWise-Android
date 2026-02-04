# PR Draft Prompt

You are an AI assistant helping to create a Pull Request description.
    
TASK: [Feature] Migrate Data from Money Manager App (.mmbak)
ISSUE: {
  "title": "[Feature] Migrate Data from Money Manager App (.mmbak)",
  "number": 65
}

GIT CONTEXT:
COMMITS:
fcf4a61 feat: [Feature] Migrate Data from Money Manager App (.mm...
5ab9549 feat: [Feature] Migrate Data from Money Manager App (.mm...
2f6074b ✨ feat(import): add money manager import and security improvements
a9932b2 🔒 fix(security): enforce network security configuration and improve resource handling
683562b ✨ feat(ui): add dashboard navigation from migration screen
1d7e03f 🐛 fix(api): improve migration error handling
acbfa21 ✨ feat(logging): enhance API and migration logging
a022bc3 ✨ feat(migration): add money manager data migration feature

STATS:
.luma_state.json                                   |  16 +-
 CHANGELOG.md                                       |   8 +
 README.md                                          |   4 +-
 app/build.gradle.kts                               |   8 +-
 app/src/main/AndroidManifest.xml                   |   1 +
 .../java/com/oatrice/jarwise/JarWiseApplication.kt |   4 +-
 .../main/java/com/oatrice/jarwise/MainActivity.kt  |  16 +
 .../com/oatrice/jarwise/data/api/MigrationApi.kt   |  17 +
 .../jarwise/data/api/model/MigrationModels.kt      |  22 +
 .../jarwise/data/repository/MigrationRepository.kt |  64 ++
 .../java/com/oatrice/jarwise/di/NetworkModule.kt   |  36 +
 .../com/oatrice/jarwise/di/RepositoryModule.kt     |   1 +
 .../java/com/oatrice/jarwise/di/ViewModelModule.kt |   2 +
 .../java/com/oatrice/jarwise/ui/SettingsScreen.kt  |  15 +
 .../jarwise/ui/migration/MigrationScreen.kt        | 230 ++++++
 .../jarwise/ui/migration/MigrationViewModel.kt     |  99 +++
 .../java/com/oatrice/jarwise/utils/AppLogger.kt    |  28 +
 app/src/main/res/xml/network_security_config.xml   |  12 +
 code_review.md                                     | 132 +++-
 draft_pr_prompt.md                                 | 857 ++++++++++-----------
 gradle/libs.versions.toml                          |   8 +
 21 files changed, 1127 insertions(+), 453 deletions(-)

KEY FILE DIFFS:
diff --git a/app/build.gradle.kts b/app/build.gradle.kts
index f505630..5a147d9 100644
--- a/app/build.gradle.kts
+++ b/app/build.gradle.kts
@@ -15,7 +15,7 @@ android {
         minSdk = 24
         targetSdk = 34
         versionCode = 1
-        versionName = "1.7.0"
+        versionName = "1.8.0"
 
         testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
         vectorDrawables {
@@ -147,6 +147,12 @@ dependencies {
     implementation(libs.google.auth.library.oauth2.http)
     implementation(libs.kotlinx.coroutines.play.services)
     implementation(libs.google.http.client.android)
+
+    // Networking
+    implementation(libs.retrofit)
+    implementation(libs.converter.gson)
+    implementation(libs.okhttp)
+    implementation(libs.logging.interceptor)
 }
 
 ksp {
diff --git a/app/src/main/AndroidManifest.xml b/app/src/main/AndroidManifest.xml
index b2516b3..cb80f01 100644
--- a/app/src/main/AndroidManifest.xml
+++ b/app/src/main/AndroidManifest.xml
@@ -18,6 +18,7 @@
         android:roundIcon="@mipmap/ic_launcher_round"
         android:supportsRtl="true"
         android:theme="@style/Theme.JarWise"
+        android:networkSecurityConfig="@xml/network_security_config"
         tools:targetApi="31">
         <activity
             android:name=".MainActivity"
diff --git a/app/src/main/java/com/oatrice/jarwise/JarWiseApplication.kt b/app/src/main/java/com/oatrice/jarwise/JarWiseApplication.kt
index dba61fc..d8973a6 100644
--- a/app/src/main/java/com/oatrice/jarwise/JarWiseApplication.kt
+++ b/app/src/main/java/com/oatrice/jarwise/JarWiseApplication.kt
@@ -3,6 +3,7 @@ package com.oatrice.jarwise
 import android.app.Application
 import com.oatrice.jarwise.di.appModule
 import com.oatrice.jarwise.di.dataModule
+import com.oatrice.jarwise.di.networkModule
 import com.oatrice.jarwise.di.repositoryModule
 import com.oatrice.jarwise.di.viewModelModule
 import com.oatrice.jarwise.di.authModule
@@ -25,7 +26,8 @@ class JarWiseApplication : Application() {
                     dataModule,
                     repositoryModule,
                     viewModelModule,
-                    authModule
+                    authModule,
+                    networkModule
                 )
             }
             
diff --git a/app/src/main/java/com/oatrice/jarwise/MainActivity.kt b/app/src/main/java/com/oatrice/jarwise/MainActivity.kt
index b7e516e..a58d644 100644
--- a/app/src/main/java/com/oatrice/jarwise/MainActivity.kt
+++ b/app/src/main/java/com/oatrice/jarwise/MainActivity.kt
@@ -35,6 +35,7 @@ sealed class Screen {
     data object Settings : Screen()
     data object ManageJars : Screen()
     data object ManageWallets : Screen()
+    data object Migration : Screen()
     data object Login : Screen()
 }
 
@@ -103,8 +104,23 @@ class MainActivity : ComponentActivity() {
                         is Screen.Settings -> SettingsScreen(
                              onBack = { currentScreen = Screen.Dashboard },
                              onNavigateToManageWallets = { currentScreen = Screen.ManageWallets },
+                             onNavigateToMigration = { currentScreen = Screen.Migration },
                              viewModel = viewModel
                         )
+                        is Screen.Migration -> {
+                           val migrationViewModel: com.oatrice.jarwise.ui.migration.MigrationViewModel = org.koin.androidx.compose.koinViewModel()
+                           com.oatrice.jarwise.ui.migration.MigrationScreen(
+                               onBack = { 
+                                   migrationViewModel.resetState()
+                                   currentScreen = Screen.Settings 
+                               },
+                               onGoToDashboard = {
+                                    migrationViewModel.resetState()
+                                    currentScreen = Screen.Dashboard
+                               },
+                               viewModel = migrationViewModel
+                           )
+                        }
                         is Screen.TransactionHistory -> TransactionHistoryScreen(
                             transactions = transactions,
                             selectedCurrency = selectedCurrency,
diff --git a/app/src/main/java/com/oatrice/jarwise/data/api/MigrationApi.kt b/app/src/main/java/com/oatrice/jarwise/data/api/MigrationApi.kt
new file mode 100644
index 0000000..198961d
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/data/api/MigrationApi.kt
@@ -0,0 +1,17 @@
+package com.oatrice.jarwise.data.api
+
+import com.oatrice.jarwise.data.api.model.MigrationResponse
+import okhttp3.MultipartBody
+import retrofit2.Response
+import retrofit2.http.Multipart
+import retrofit2.http.POST
+import retrofit2.http.Part
+
+interface MigrationApi {
+    @Multipart
+    @POST("api/v1/migrations/money-manager")
+    suspend fun uploadMigrationFiles(
+        @Part mmbakFile: MultipartBody.Part,
+        @Part xlsFile: MultipartBody.Part
+    ): Response<MigrationResponse>
+}
diff --git a/app/src/main/java/com/oatrice/jarwise/data/api/model/MigrationModels.kt b/app/src/main/java/com/oatrice/jarwise/data/api/model/MigrationModels.kt
new file mode 100644
index 0000000..36c49bc
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/data/api/model/MigrationModels.kt
@@ -0,0 +1,22 @@
+package com.oatrice.jarwise.data.api.model
+
+import com.google.gson.annotations.SerializedName
+
+data class MigrationResponse(
+    @SerializedName("job_id")
+    val jobId: String,
+    @SerializedName("status")
+    val status: String,
+    @SerializedName("message")
+    val message: String
+)
+
+data class MigrationStatusResponse(
+    @SerializedName("job_id")
+    val jobId: String,
+    @SerializedName("status")
+    val status: String,
+    @SerializedName("message")
+    val message: String,
+    // Add other fields as necessary based on backend response
+)
diff --git a/app/src/main/java/com/oatrice/jarwise/data/repository/MigrationRepository.kt b/app/src/main/java/com/oatrice/jarwise/data/repository/MigrationRepository.kt
new file mode 100644
index 0000000..50c9957
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/data/repository/MigrationRepository.kt
@@ -0,0 +1,64 @@
+package com.oatrice.jarwise.data.repository
+
+import android.content.Context
+import android.net.Uri
+import com.oatrice.jarwise.data.api.MigrationApi
+import com.oatrice.jarwise.data.api.model.MigrationResponse
+import kotlinx.coroutines.Dispatchers
+import kotlinx.coroutines.withContext
+import okhttp3.MediaType.Companion.toMediaTypeOrNull
+import okhttp3.MultipartBody
+import okhttp3.RequestBody.Companion.asRequestBody
+import java.io.File
+import java.io.FileOutputStream
+
+class MigrationRepository(
+    private val api: MigrationApi,
+    private val context: Context
+) {
+
+    suspend fun uploadMigrationFiles(mmbakUri: Uri, xlsUri: Uri): Result<MigrationResponse> {
+        return withContext(Dispatchers.IO) {
+            try {
+                val mmbakFile = getFileFromUri(context, mmbakUri, "backup.mmbak")
+                val xlsFile = getFileFromUri(context, xlsUri, "backup.xls")
+
+                if (mmbakFile == null || xlsFile == null) {
+                    return@withContext Result.failure(Exception("Failed to read files"))
+                }
+
+                val mmbakRequestBody = mmbakFile.asRequestBody("application/octet-stream".toMediaTypeOrNull())
+                val xlsRequestBody = xlsFile.asRequestBody("application/vnd.ms-excel".toMediaTypeOrNull())
+
+                val mmbakPart = MultipartBody.Part.createFormData("mmbak_file", mmbakFile.name, mmbakRequestBody)
+                val xlsPart = MultipartBody.Part.createFormData("xls_file", xlsFile.name, xlsRequestBody)
+
+                val response = api.uploadMigrationFiles(mmbakPart, xlsPart)
+
+                if (response.isSuccessful && response.body() != null) {
+                    Result.success(response.body()!!)
+                } else {
+                    Result.failure(Exception("Upload failed: ${response.code()} ${response.message()}"))
+                }
+            } catch (e: Exception) {
+                Result.failure(e)
+            }
+        }
+    }
+
+    private fun getFileFromUri(context: Context, uri: Uri, fileName: String): File? {
+        return try {
+            val contentResolver = context.contentResolver
+            contentResolver.openInputStream(uri)?.use { inputStream ->
+                val tempFile = File(context.cacheDir, fileName)
+                FileOutputStream(tempFile).use { outputStream ->
+                    inputStream.copyTo(outputStream)
+                }
+                tempFile
+            }
+        } catch (e: Exception) {
+            e.printStackTrace()
+            null
+        }
+    }
+}
diff --git a/app/src/main/java/com/oatrice/jarwise/di/NetworkModule.kt b/app/src/main/java/com/oatrice/jarwise/di/NetworkModule.kt
new file mode 100644
index 0000000..82706e2
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/di/NetworkModule.kt
@@ -0,0 +1,36 @@
+package com.oatrice.jarwise.di
+
+import com.oatrice.jarwise.data.api.MigrationApi
+import okhttp3.OkHttpClient
+import okhttp3.logging.HttpLoggingInterceptor
+import org.koin.dsl.module
+import retrofit2.Retrofit
+import retrofit2.converter.gson.GsonConverterFactory
+import java.util.concurrent.TimeUnit
+
+val networkModule = module {
+    single {
+        val logger = get<com.oatrice.jarwise.utils.AppLogger>()
+        val logging = HttpLoggingInterceptor { message ->
+            logger.d("API", message)
+        }.apply {
+            level = HttpLoggingInterceptor.Level.BODY
+        }
+        OkHttpClient.Builder()
+            .addInterceptor(logging)
+            .connectTimeout(30, TimeUnit.SECONDS)
+            .readTimeout(30, TimeUnit.SECONDS)
+            .writeTimeout(30, TimeUnit.SECONDS)
+            .build()
+    }
+
+    single {
+        Retrofit.Builder()
+            .baseUrl("http://10.0.2.2:8080/") // Emulator localhost
+            .client(get())
+            .addConverterFactory(GsonConverterFactory.create())
+            .build()
+    }
+
+    single { get<Retrofit>().create(MigrationApi::class.java) }
+}
diff --git a/app/src/main/java/com/oatrice/jarwise/di/RepositoryModule.kt b/app/src/main/java/com/oatrice/jarwise/di/RepositoryModule.kt
index 8fed8ab..0daa853 100644
--- a/app/src/main/java/com/oatrice/jarwise/di/RepositoryModule.kt
+++ b/app/src/main/java/com/oatrice/jarwise/di/RepositoryModule.kt
@@ -10,4 +10,5 @@ val repositoryModule = module {
     single { JarConfigRepository(get()) }
     single { WalletRepository(get()) }
     single { SlipRepository(androidContext()) }
+    single { MigrationRepository(get(), androidContext()) }
 }
diff --git a/app/src/main/java/com/oatrice/jarwise/di/ViewModelModule.kt b/app/src/main/java/com/oatrice/jarwise/di/ViewModelModule.kt
index 26230db..37a89ce 100644
--- a/app/src/main/java/com/oatrice/jarwise/di/ViewModelModule.kt
+++ b/app/src/main/java/com/oatrice/jarwise/di/ViewModelModule.kt
@@ -6,6 +6,7 @@ import com.oatrice.jarwise.ui.managejars.ManageJarsViewModel
 import com.oatrice.jarwise.ui.managewallets.ManageWalletsViewModel
 import com.oatrice.jarwise.ui.login.LoginViewModel
 import com.oatrice.jarwise.ui.settings.SettingsViewModel
+import com.oatrice.jarwise.ui.migration.MigrationViewModel
 import org.koin.androidx.viewmodel.dsl.viewModel
 import org.koin.dsl.module
 
@@ -16,4 +17,5 @@ val viewModelModule = module {
     viewModel { ManageWalletsViewModel(get()) }
     viewModel { LoginViewModel(get(), get()) }
     viewModel { SettingsViewModel(get(), get()) }
+    viewModel { MigrationViewModel(get(), get()) }
 }
diff --git a/app/src/main/java/com/oatrice/jarwise/ui/SettingsScreen.kt b/app/src/main/java/com/oatrice/jarwise/ui/SettingsScreen.kt
index 3ecfa96..d88770a 100644
--- a/app/src/main/java/com/oatrice/jarwise/ui/SettingsScreen.kt
+++ b/app/src/main/java/com/oatrice/jarwise/ui/SettingsScreen.kt
@@ -32,6 +32,7 @@ import org.koin.androidx.compose.koinViewModel
 fun SettingsScreen(
     onBack: () -> Unit,
     onNavigateToManageWallets: () -> Unit = {},
+    onNavigateToMigration: () -> Unit = {},
     viewModel: MainViewModel,
     settingsViewModel: SettingsViewModel = koinViewModel()
 ) {
@@ -353,9 +354,23 @@ fun SettingsScreen(
                 ) {
                     Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null)
                     Text("Manage Wallets (Sub-accounts)")
+                    }
+            }
+            
+            OutlinedButton(
+                onClick = onNavigateToMigration,
+                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
+            ) {
+                 Row(
+                    horizontalArrangement = Arrangement.spacedBy(8.dp),
+                    verticalAlignment = Alignment.CenterVertically
+                ) {
+                    Icon(Icons.Rounded.CloudDone, contentDescription = null) // Using CloudDone as a placeholder if Import icon not available
+                    Text("Migrate from Money Manager")
                 }
             }
 
+
             Text(
                 text = "Currency",
                 style = MaterialTheme.typography.titleMedium,
diff --git a/app/src/main/java/com/oatrice/jarwise/ui/migration/MigrationScreen.kt b/app/src/main/java/com/oatrice/jarwise/ui/migration/MigrationScreen.kt
new file mode 100644
index 0000000..1eefe59
--- /dev/null
+++ b/app/src/main/java/com/oatrice/jarwise/ui/migration/MigrationScreen.kt
@@ -0,0 +1,230 @@
+package com.oatrice.jarwise.ui.migration
+
+import android.net.Uri
+import androidx.activity.compose.rememberLauncherForActivityResult
+import androidx.activity.result.contract.ActivityResultContracts
+import androidx.compose.foundation.background
+import androidx.compose.foundation.layout.*
+import androidx.compose.foundation.rememberScrollState
+import androidx.compose.foundation.shape.RoundedCornerShape
+import androidx.compose.foundation.verticalScroll
+import androidx.compose.material.icons.Icons
+import androidx.compose.material.icons.filled.ArrowBack
+import androidx.compose.material.icons.filled.CheckCircle
+import androidx.compose.material.icons.filled.Error
+import androidx.compose.material.icons.filled.UploadFile
+import androidx.compose.material3.*
+import androidx.compose.runtime.Composable
+import androidx.compose.runtime.collectAsState
+import androidx.compose.runtime.getValue
+import androidx.compose.ui.Alignment
+import androidx.compose.ui.Modifier
+import androidx.compose.ui.graphics.Color
+import androidx.compose.ui.platform.LocalContext
+import androidx.compose.ui.text.font.FontWeight
+import androidx.compose.ui.text.style.TextAlign
+import androidx.compose.ui.unit.dp
+import androidx.documentfile.provider.DocumentFile
+
+@OptIn(ExperimentalMaterial3Api::class)
+@Composable
+fun MigrationScreen(
+    onBack: () -> Unit,
+    onGoToDashboard: () -> Unit = {},
+    viewModel: MigrationViewModel
+) {
+    val uiState by viewModel.uiState.collectAsState()
+    val mmbakFileName by viewModel.mmbakFileName.collectAsState()
+    val xlsFileName by viewModel.xlsFileName.collectAsState()
+    val context = LocalContext.current
+
+    val mmbakLauncher = rememberLauncherForActivityResult(
+        contract = ActivityResultContracts.OpenDocument()
+    ) { uri: Uri? ->
+        uri?.let {
+            val name = DocumentFile.fromSingleUri(context, it)?.name
+            viewModel.setMmbakFile(it, name)
+        }
+    }
+
+    val xlsLauncher = rememberLauncherForActivityResult(
+        contract = ActivityResultContracts.OpenDocument()
+    ) { uri: Uri? ->
+        uri?.let {
+            val name = DocumentFile.fromSingleUri(context, it)?.name
+            viewModel.setXlsFile(it, name)
+        }
+    }
+
+    Scaffold(
+        topBar = {
+            TopAppBar(
+                title = { Text("Migrate from Money Manager") },
+                navigationIcon = {
+                    IconButton(onClick = onBack) {
+                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
+                    }
+                }
+            )
+        }
+    ) { padding ->
+        Column(
+            modifier = Modifier
+                .fillMaxSize()
+                .padding(padding)
+                .padding(16.dp)
+                .verticalScroll(rememberScrollState()),
+            horizontalAlignment = Alignment.CenterHorizontally,
+            verticalArrangement = Arrangement.spacedBy(16.dp)
+        ) {
+            
+            Card(
+                colors = CardDefaults.cardColors(
+                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
+                ),
+                shape = RoundedCornerShape(12.dp)
+            ) {
+                 Column(modifier = Modifier.padding(16.dp)) {
+                     Text(
+                        text = "Instructions",
+                        style = MaterialTheme.typography.titleMedium,
+                        fontWeight = FontWeight.Bold,
+                        color = MaterialTheme.colorScheme.primary
+                    )
+                    Spacer(modifier = Modifier.height(8.dp))
+                    Text(
+                        text = "1. Export your data from Money Manager app as .mmbak (Backup) and .xls (Excel).\n" +
+                                "2. Select both files below.\n" +
+                                "3. Click 'Start Migration' to import your history.",
+                        style = MaterialTheme.typography.bodyMedium
+                    )
+                 }
+            }
+
+            Spacer(modifier = Modifier.height(8.dp))
+
+            // Mmbak File Picker
+            FilePickerItem(
+                label = "Select .mmbak File (Backup)",
+                fileName = mmbakFileName,
+                onPick = { mmbakLauncher.launch(arrayOf("application/octet-stream", "*/*")) }
+            )
+
+            // XLS File Picker
+            FilePickerItem(
+                label = "Select .xls File (Excel)",
+                fileName = xlsFileName,
+                onPick = { xlsLauncher.launch(arrayOf("application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "*/*")) }
+            )
+
+            Spacer(modifier = Modifier.height(24.dp))
+
+            // Action Button & Status
+            when (val state = uiState) {
+                is MigrationUiState.Idle, is MigrationUiState.Error -> {
+                    Button(
+                        onClick = { viewModel.startMigration() },
+                        enabled = mmbakFileName != null && xlsFileName != null,
+                        modifier = Modifier.fillMaxWidth().height(50.dp)
+                    ) {
+                        Icon(Icons.Default.UploadFile, contentDescription = null)
+                        Spacer(modifier = Modifier.width(8.dp))
+                        Text("Start Migration")
+                    }
+                    
+                    if (state is MigrationUiState.Error) {
+                        Spacer(modifier = Modifier.height(16.dp))
+                        Card(
+                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
+                        ) {
+                             Row(
+                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
+                                verticalAlignment = Alignment.CenterVertically
+                            ) {
+                                Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.onErrorContainer)
+                                Spacer(modifier = Modifier.width(8.dp))
+                                Text(state.message, color = 
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
