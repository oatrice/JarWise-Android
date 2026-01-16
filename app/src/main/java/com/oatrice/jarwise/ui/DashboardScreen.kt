package com.oatrice.jarwise.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.oatrice.jarwise.data.GeneratedMockData
import com.oatrice.jarwise.ui.components.JarCard
import com.oatrice.jarwise.ui.components.TransactionCard
import com.oatrice.jarwise.ui.theme.*

/**
 * The primary dashboard screen of the JarWise application.
 * Matches the "MagicPatterns" aesthetic from the Web Mobile version.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val totalBalance = GeneratedMockData.jars.sumOf { it.current }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Gray950, // Dark background
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp) // Extra padding for Floating Nav
            ) {
                // HEADER SECTION (Sticky-ish visuals, but scrolls with content for Balance)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        // Top Bar: User & Actions
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

                            // Actions (Scan, Search, Bell)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                ActionButton(icon = Icons.Rounded.QrCodeScanner, contentDescription = "Scan")
                                ActionButton(icon = Icons.Rounded.Search, contentDescription = "Search")
                                Box {
                                    ActionButton(icon = Icons.Rounded.Notifications, contentDescription = "Notifications")
                                    // Notification Dot
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Red500)
                                            .align(Alignment.TopEnd)
                                            .offset(x = (-2).dp, y = 2.dp)
                                            .border(2.dp, Gray900, CircleShape)
                                    )
                                }
                            }
                        }

                        // Balance & Streak (Scrolls with content)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "Total Balance",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = Gray400,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "$${String.format("%,.0f", totalBalance)}",
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 36.sp
                                    )
                                )
                            }

                            // Streak Pill
                            Surface(
                                shape = RoundedCornerShape(100),
                                color = Orange500.copy(alpha = 0.1f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Orange500.copy(alpha = 0.2f)),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.LocalFireDepartment,
                                        contentDescription = "Streak",
                                        tint = Orange500,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "5 Day Streak!",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            color = Orange400,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // JARS SECTION
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Your Jars", style = MaterialTheme.typography.titleMedium.copy(color = Gray100))
                        TextButton(onClick = { /* View All */ }) {
                            Text("View All", color = Blue500)
                        }
                    }
                }
                
                itemsIndexed(GeneratedMockData.jars) { index, jar ->
                    JarCard(jar = jar, isPriority = index == 0)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // TRANSACTIONS SECTION
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Recent Activity", style = MaterialTheme.typography.titleMedium.copy(color = Gray100))
                    }
                }

                itemsIndexed(GeneratedMockData.transactions) { _, transaction ->
                    TransactionCard(transaction = transaction)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // Floating Bottom Navigation (Capsule Style)
        FloatingBottomNavigation(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp))
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit = {}
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .background(Gray900, CircleShape)
            .border(1.dp, Gray800, CircleShape)
    ) {
        Icon(icon, contentDescription, tint = Gray400, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun FloatingBottomNavigation(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // The Glassmorphic Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(72.dp),
            shape = RoundedCornerShape(100),
            color = Gray900.copy(alpha = 0.9f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NavIcon(Icons.Rounded.Dashboard, "Home", isSelected = true)
                NavIcon(Icons.Rounded.History, "History")
                // Spacer for the center button
                Spacer(modifier = Modifier.width(48.dp))
                NavIcon(Icons.Rounded.AccountBalanceWallet, "Wallet")
                NavIcon(Icons.Rounded.Person, "Profile")
            }
        }

        // The Floating Add Button (Popped Out)
        // Positioned relative to the bar
        Surface(
            shape = CircleShape,
            color = Color.Transparent,
            modifier = Modifier
                .offset(y = (-4).dp)
                .size(64.dp)
                .background(Brush.linearGradient(listOf(Blue500, Color(0xFF4F46E5))), CircleShape)
                .border(6.dp, Gray950, CircleShape) // Thicker border for "cutout" effect
                .clickable { /* Add Action */ },
            shadowElevation = 10.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Add, "Add", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
fun NavIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    isSelected: Boolean = false
) {
    IconButton(onClick = {}) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = if (isSelected) Blue400 else Gray500,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    JarWiseTheme {
        DashboardScreen()
    }
}
