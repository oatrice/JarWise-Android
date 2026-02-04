package com.oatrice.jarwise.ui.migration

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MigrationScreen(
    onBack: () -> Unit,
    viewModel: MigrationViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val mmbakFileName by viewModel.mmbakFileName.collectAsState()
    val xlsFileName by viewModel.xlsFileName.collectAsState()
    val context = LocalContext.current

    val mmbakLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val name = DocumentFile.fromSingleUri(context, it)?.name
            viewModel.setMmbakFile(it, name)
        }
    }

    val xlsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val name = DocumentFile.fromSingleUri(context, it)?.name
            viewModel.setXlsFile(it, name)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Migrate from Money Manager") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                 Column(modifier = Modifier.padding(16.dp)) {
                     Text(
                        text = "Instructions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Export your data from Money Manager app as .mmbak (Backup) and .xls (Excel).\n" +
                                "2. Select both files below.\n" +
                                "3. Click 'Start Migration' to import your history.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                 }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mmbak File Picker
            FilePickerItem(
                label = "Select .mmbak File (Backup)",
                fileName = mmbakFileName,
                onPick = { mmbakLauncher.launch(arrayOf("application/octet-stream", "*/*")) }
            )

            // XLS File Picker
            FilePickerItem(
                label = "Select .xls File (Excel)",
                fileName = xlsFileName,
                onPick = { xlsLauncher.launch(arrayOf("application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "*/*")) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Button & Status
            when (val state = uiState) {
                is MigrationUiState.Idle, is MigrationUiState.Error -> {
                    Button(
                        onClick = { viewModel.startMigration() },
                        enabled = mmbakFileName != null && xlsFileName != null,
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Migration")
                    }
                    
                    if (state is MigrationUiState.Error) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                             Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.onErrorContainer)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(state.message, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }
                is MigrationUiState.Uploading -> {
                    CircularProgressIndicator()
                    Text("Uploading and processing...", style = MaterialTheme.typography.bodyLarge)
                }
                is MigrationUiState.Success -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDF7ED)) // Light Green
                    ) {
                         Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CheckCircle, 
                                contentDescription = "Success", 
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Migration Successful!", 
                                style = MaterialTheme.typography.titleLarge, 
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                state.message, 
                                textAlign = TextAlign.Center,
                                color = Color(0xFF1B5E20)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = onBack) {
                                Text("Go to Dashboard")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilePickerItem(
    label: String,
    fileName: String?,
    onPick: () -> Unit
) {
    OutlinedCard(
        onClick = onPick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = fileName ?: "No file selected",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (fileName != null) FontWeight.Medium else FontWeight.Normal,
                    color = if (fileName != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (fileName != null) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
            } else {
                 Icon(Icons.Default.UploadFile, contentDescription = "Select", tint = MaterialTheme.colorScheme.outline)
            }
        }
    }
}
