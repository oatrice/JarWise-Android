package com.oatrice.jarwise.ui.components

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.oatrice.jarwise.ui.ActionButton
import com.oatrice.jarwise.ui.theme.*

@Composable
fun DashboardTopBar(
    visible: Boolean,
    onScanClick: () -> Unit,
    onImportClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else (-100).dp,
        animationSpec = spring(stiffness = 200f, dampingRatio = 0.7f),
        label = "TopBarOffset"
    )

    Box(
        modifier = Modifier
            .offset(y = offsetY)
            .fillMaxWidth()
            .background(Gray950.copy(alpha = 0.95f))
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .statusBarsPadding() // Handle Safe Area
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Profile
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(GradientBlueToCyan))
                        .padding(2.dp)
                ) {
                    AsyncImage(
                        model = "https://ui-avatars.com/api/?name=User&background=0D0D0D&color=fff",
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Gray950)
                            .border(2.dp, Gray950, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Column {
                    Text(
                        text = "Welcome back",
                        style = MaterialTheme.typography.labelMedium.copy(color = Gray400)
                    )
                    Text(
                        text = "Oatrice",
                        style = MaterialTheme.typography.titleMedium.copy(color = Gray100)
                    )
                }
            }

            // Actions (Scan, Import, More)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionButton(
                    icon = Icons.Rounded.QrCodeScanner,
                    contentDescription = "Scan",
                    onClick = onScanClick
                )
                ActionButton(
                    icon = Icons.Rounded.CloudUpload,
                    contentDescription = "Import Slip",
                    onClick = onImportClick
                )

                // Overflow Menu
                Box {
                    var showMenu by remember { mutableStateOf(false) }

                    ActionButton(
                        icon = Icons.Rounded.MoreVert,
                        contentDescription = "More",
                        onClick = { showMenu = true }
                    )

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Gray900)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Notifications", color = Gray100) },
                            onClick = {
                                showMenu = false
                                /* TODO: Handle notifications */
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Notifications,
                                    contentDescription = null,
                                    tint = Gray100
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Settings", color = Gray100) },
                            onClick = {
                                showMenu = false
                                onSettingsClick()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Settings,
                                    contentDescription = null,
                                    tint = Gray100
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
fun DashboardTopBarPreview() {
    JarWiseTheme {
        Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            DashboardTopBar(
                visible = true,
                onScanClick = {},
                onImportClick = {},
                onSettingsClick = {}
            )
        }
    }
}
