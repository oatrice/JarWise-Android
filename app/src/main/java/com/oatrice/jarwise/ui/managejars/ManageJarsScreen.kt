package com.oatrice.jarwise.ui.managejars

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oatrice.jarwise.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageJarsScreen(
    viewModel: ManageJarsViewModel,
    onBack: () -> Unit
) {
    val jars by viewModel.jars.collectAsState()
    val selectedJarId by viewModel.selectedJarId.collectAsState()
    val totalPercentage by viewModel.totalPercentage.collectAsState()
    val isValid by viewModel.isValid.collectAsState()
    val showResetDialog by viewModel.showResetDialog.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Jars", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.showResetConfirmation() }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Reset", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset")
                    }
                    Button(
                        onClick = { viewModel.save(onBack) },
                        enabled = isValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isValid) Green500 else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(Icons.Rounded.Save, contentDescription = "Save", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Total Allocation Indicator
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isValid) Green500.copy(alpha = 0.1f) else Red500.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Allocation", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "$totalPercentage%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (isValid) Green400 else Red400
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (totalPercentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (isValid) Green500 else Red500,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    if (!isValid) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            if (totalPercentage > 100) "Over by ${totalPercentage - 100}%"
                            else "${100 - totalPercentage}% remaining",
                            color = Red400,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Jar List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(jars, key = { _, jar -> jar.id }) { _, jar ->
                    // Indentation based on level
                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (jar.level > 0) {
                            Spacer(modifier = Modifier.width((jar.level * 24).dp)) // Indent
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            JarEditCard(
                                jar = jar,
                                isExpanded = selectedJarId == jar.id,
                                onClick = { viewModel.selectJar(jar.id) },
                                onNameChange = { viewModel.updateJar(jar.id, name = it) },
                                onPercentageChange = { viewModel.updateJar(jar.id, percentage = it) },
                                onColorChange = { viewModel.updateJar(jar.id, colorName = it) },
                                onIconChange = { viewModel.updateJar(jar.id, iconName = it) },
                                onDelete = { viewModel.deleteJar(jar.id) }
                            )
                        }
                    }
                }
                
                // Add New Jar Button
                item {
                    Button(
                        onClick = { viewModel.addJar() },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add New Jar", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideResetConfirmation() },
            title = { Text("Reset to Default?") },
            text = { Text("This will restore all jars to their original names and percentages.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.resetToDefaults() },
                    colors = ButtonDefaults.buttonColors(containerColor = Red500)
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideResetConfirmation() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun JarEditCard(
    jar: EditableJar,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onPercentageChange: (Int) -> Unit,
    onColorChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ... same header code ...
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(jar.color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(jar.icon, contentDescription = null, tint = jar.color, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(jar.name, fontWeight = FontWeight.SemiBold)
                    // Hide allocation subtitle for categories if needed, but keeping for now
                    if (jar.level == 0) {
                        Text("${jar.percentage}% allocation", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    } else {
                        Text("Category", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    }
                }
                if (jar.level == 0) {
                     Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(jar.color.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("${jar.percentage}%", color = jar.color, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Expanded Edit Panel
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                    // Name Input
                    Text("Name", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = jar.name,
                        onValueChange = onNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Percentage Slider (Only for Top Level Jars)
                    if (jar.level == 0) {
                        Text("Percentage: ${jar.percentage}%", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Slider(
                            value = jar.percentage.toFloat(),
                            onValueChange = { onPercentageChange(it.toInt()) },
                            valueRange = 0f..100f,
                            steps = 99,
                            colors = SliderDefaults.colors(thumbColor = jar.color, activeTrackColor = jar.color)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Color Picker
                    // ... (keep color picker) ...
                    Text("Color", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ManageJarsViewModel.AVAILABLE_COLORS.forEach { colorName ->
                            val color = ManageJarsViewModel.getColorFromName(colorName)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { onColorChange(colorName) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (jar.colorName == colorName) {
                                    Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Icon Picker
                     // ... (keep icon picker) ...
                    Text("Icon", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ManageJarsViewModel.AVAILABLE_ICONS.forEach { iconName ->
                            val icon = ManageJarsViewModel.getIconFromName(iconName)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (jar.iconName == iconName)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { onIconChange(iconName) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    tint = if (jar.iconName == iconName) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Delete Button (New)
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = Red500),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.Delete, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete", color = Color.White)
                    }
                }
            }
        }
    }
}
