package com.oatrice.jarwise.ui

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Schedule
import com.oatrice.jarwise.data.repository.SlipRepository

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SlipImportScreen(
    recentImages: List<Uri>,
    buckets: List<SlipRepository.ImageBucket>,
    selectedBucketId: String?,
    isScanning: Boolean = false,
    scanStats: String = "",
    onBack: () -> Unit,
    onPermissionResult: () -> Unit = {},
    onBucketSelected: (String?) -> Unit = {}
) {
    // Determine permission based on Android version
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    
    val permissionState = rememberPermissionState(permission)
    var showAlbumDialog by remember { mutableStateOf(false) }

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
                onClick = { showAlbumDialog = true },
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
            
            if (permissionState.status.isGranted) {
                // Permission Granted Content
                if (recentImages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                         Text("No images found.", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 100.dp),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(recentImages) { uri ->
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = null,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .fillMaxWidth(),
                                contentScale = ContentScale.Crop
                            )
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
                        Button(onClick = { permissionState.launchPermissionRequest() }) {
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
