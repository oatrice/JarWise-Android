package com.oatrice.jarwise.ui

import android.Manifest
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.oatrice.jarwise.data.model.DetectedSlip
import com.oatrice.jarwise.data.model.ParsedSlip
import com.oatrice.jarwise.data.repository.SlipRepository
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.tooling.preview.Preview
import com.oatrice.jarwise.data.service.SlipDetectionResult
import android.content.res.Configuration
import com.oatrice.jarwise.ui.theme.JarWiseTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SlipImportScreen(
    recentImages: List<DetectedSlip>,
    buckets: List<SlipRepository.ImageBucket>,
    selectedBucketId: String?,
    isScanning: Boolean = false,
    scanStats: String = "",
    onBack: () -> Unit,
    onPermissionResult: () -> Unit = {},
    onBucketSelected: (String?) -> Unit = {},
    onConfirmSlip: (DetectedSlip, ParsedSlip) -> Unit = { _, _ -> }
) {
    // Determine permission based on Android version
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    
    val permissionState = rememberPermissionState(permission)
    var showAlbumDialog by remember { mutableStateOf(false) }
    var selectedSlip by remember { mutableStateOf<DetectedSlip?>(null) }

    // Request permission on launch if not granted
    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }

    // Trigger refresh when permission is granted
    LaunchedEffect(permissionState.status) {
        if (permissionState.status.isGranted) {
            onPermissionResult()
        }
    }

    if (showAlbumDialog) {
        AlbumSelectionDialog(
            buckets = buckets,
            onDismissRequest = { showAlbumDialog = false },
            onBucketSelected = { 
                onBucketSelected(it)
                showAlbumDialog = false
            }
        )
    }

    if (selectedSlip != null) {
        SlipEditDialog(
            slip = selectedSlip!!,
            onDismiss = { selectedSlip = null },
            onConfirm = { updatedParsedSlip ->
                onConfirmSlip(selectedSlip!!, updatedParsedSlip)
                selectedSlip = null
            }
        )
    }

    SlipImportScreenContent(
        recentImages = recentImages,
        buckets = buckets,
        selectedBucketId = selectedBucketId,
        isScanning = isScanning,
        scanStats = scanStats,
        hasPermission = permissionState.status.isGranted,
        onBack = onBack,
        onShowAlbumDialog = { showAlbumDialog = true },
        onSlipSelected = { selectedSlip = it },
        onRequestPermission = { permissionState.launchPermissionRequest() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlipImportScreenContent(
    recentImages: List<DetectedSlip>,
    buckets: List<SlipRepository.ImageBucket>,
    selectedBucketId: String?,
    isScanning: Boolean,
    scanStats: String,
    hasPermission: Boolean,
    onBack: () -> Unit,
    onShowAlbumDialog: () -> Unit,
    onSlipSelected: (DetectedSlip) -> Unit,
    onRequestPermission: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Import Slips")
                        if (scanStats.isNotEmpty()) {
                             Text(scanStats, style = MaterialTheme.typography.bodySmall)
                        } else {
                            val currentBucket = buckets.find { it.id == selectedBucketId }
                            Text(
                                text = currentBucket?.displayName ?: "Recent Images",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 16.dp).size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onShowAlbumDialog,
                icon = { Icon(Icons.Default.List, contentDescription = null) },
                text = { Text("Albums") },
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            
            if (hasPermission) {
                // Permission Granted Content
                if (recentImages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                         Text("No images found with current filter.", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 100.dp),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(recentImages) { slip ->
                            Box(modifier = Modifier.clickable { onSlipSelected(slip) }) {
                                Image(
                                    painter = rememberAsyncImagePainter(slip.uri),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .fillMaxWidth(),
                                    contentScale = ContentScale.Crop
                                )
                                // Overlay for confidence/type if needed
                                if (slip.result.isSlip) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                        modifier = Modifier.align(Alignment.BottomEnd)
                                    ) {
                                        Text(
                                            text = if (slip.result.parsedData?.amount != null) "฿" else "?",
                                            modifier = Modifier.padding(4.dp),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Permission Denied Content
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Permission required to load images.")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRequestPermission) {
                            Text("Grant Permission")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumSelectionDialog(
    buckets: List<SlipRepository.ImageBucket>,
    onDismissRequest: () -> Unit,
    onBucketSelected: (String?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Select Album") },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                item {
                    ListItem(
                        headlineContent = { Text("Recent Images") },
                        leadingContent = { Icon(Icons.Default.Schedule, null) },
                        modifier = Modifier.clickable { onBucketSelected(null) }
                    )
                }
                items(buckets) { bucket ->
                    ListItem(
                        headlineContent = { Text(bucket.displayName) },
                        supportingContent = { Text("${bucket.count} items") },
                        leadingContent = {
                            Image(
                                painter = rememberAsyncImagePainter(bucket.coverUri),
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                contentScale = ContentScale.Crop
                            )
                        },
                        modifier = Modifier.clickable { onBucketSelected(bucket.id) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlipEditDialog(
    slip: DetectedSlip,
    onDismiss: () -> Unit,
    onConfirm: (ParsedSlip) -> Unit
) {
    var amount by remember { mutableStateOf(slip.result.parsedData?.amount?.toString() ?: "") }
    var bankName by remember { mutableStateOf(slip.result.parsedData?.bankName ?: "") }
    
    // Simple date handling for now - just string
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
    var dateStr by remember { 
        mutableStateOf(
            if (slip.result.parsedData?.date != null) dateFormat.format(slip.result.parsedData?.date!!) else ""
        ) 
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                    Text("Review Slip", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = {
                        val parsedAmount = amount.toDoubleOrNull()
                        // Re-parse date is tricky without proper picker, 
                        // just passing original back if string matches, or null for now if changed
                        // Implementing full date parsing logic here is overkill for this step, 
                        // assuming user just confirms mostly.
                        val parsedDate = slip.result.parsedData?.date // Simplified: keep original for now if not editing logic complex
                        
                        onConfirm(
                            ParsedSlip(
                                amount = parsedAmount,
                                bankName = bankName,
                                date = parsedDate,
                                rawText = slip.result.parsedData?.rawText ?: ""
                            )
                        )
                    }) {
                        Icon(Icons.Default.Check, "Confirm")
                    }
                }

                // Image Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .padding(horizontal = 16.dp)
                ) {
                   Image(
                        painter = rememberAsyncImagePainter(slip.uri),
                        contentDescription = "Slip Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Editable Fields
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("Bank") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = dateStr,
                        onValueChange = { dateStr = it },
                        label = { Text("Date (read-only for now)") },
                        enabled = false, // Disable date editing for simplicity in this iteration
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Confidence: ${(slip.result.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

// Previews

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
fun PreviewSlipImportScreen() {
    val mockBuckets = listOf(
        SlipRepository.ImageBucket("1", "Camera", Uri.EMPTY, 10),
        SlipRepository.ImageBucket("2", "Screenshots", Uri.EMPTY, 5)
    )
    
    val mockSlips = listOf(
        DetectedSlip(
            uri = Uri.EMPTY, 
            result = SlipDetectionResult(
                isSlip = true, 
                confidence = 0.9f, 
                parsedData = ParsedSlip(amount = 500.00, bankName = "KBank")
            )
        ),
        DetectedSlip(
            uri = Uri.EMPTY, 
            result = SlipDetectionResult(isSlip = false)
        )
    )

    JarWiseTheme {
        SlipImportScreenContent(
            recentImages = mockSlips,
            buckets = mockBuckets,
            selectedBucketId = null,
            isScanning = false,
            scanStats = "",
            hasPermission = true, // Mock permission enabled
            onBack = {},
            onShowAlbumDialog = {},
            onSlipSelected = {},
            onRequestPermission = {}
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
fun PreviewSlipEditDialog() {
    val mockSlip = DetectedSlip(
        uri = Uri.EMPTY,
        result = SlipDetectionResult(
            isSlip = true,
            confidence = 0.95f,
            parsedData = ParsedSlip(
                amount = 1250.00,
                bankName = "SCB",
                date = java.util.Date(),
                rawText = "Transfer Successful\nAmount 1,250.00 THB\nFrom SCB"
            )
        )
    )

    JarWiseTheme {
        SlipEditDialog(
            slip = mockSlip,
            onDismiss = {},
            onConfirm = {}
        )
    }
}
