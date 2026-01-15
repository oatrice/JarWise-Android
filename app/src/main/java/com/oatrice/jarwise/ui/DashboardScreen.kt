package com.oatrice.jarwise.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Search
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
import com.oatrice.jarwise.data.MockJars
import com.oatrice.jarwise.data.MockTransactions
import com.oatrice.jarwise.ui.components.JarCard
import com.oatrice.jarwise.ui.components.TransactionCard
import com.oatrice.jarwise.ui.theme.*

/**
 * The primary dashboard screen of the JarWise application.
 *
 * This composable serves as the main landing page for users, featuring:
 * - A personalized header with the user's avatar and greeting.
 * - Quick actions for searching and notifications.
 * - A summary of the total balance across all jars.
 * - An engagement streak indicator.
 * - A scrollable list of savings "Jars" (using [JarCard]).
 * - A list of recent financial activities (using [TransactionCard]).
 * - A Floating Action Button (FAB) to create new jars or entries.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val totalBalance = MockJars.sumOf { it.current }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Add new wrapper */ },
                containerColor = Blue500,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp) // Extra bottom padding for FAB
        ) {
            // HEADER SECTION
            item {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    // Top Bar
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
                                    .padding(2.dp) // Border thickness
                            ) {
                                AsyncImage(
                                    model = "https://ui-avatars.com/api/?name=User&background=0D0D0D&color=fff",
                                    contentDescription = "User Avatar",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(Gray950) // Inner border
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

                        // Actions
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            IconButton(
                                onClick = { },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Gray900, CircleShape)
                                    .border(1.dp, Gray800, CircleShape)
                            ) {
                                Icon(Icons.Rounded.Search, null, tint = Gray400, modifier = Modifier.size(20.dp))
                            }
                            Box {
                                IconButton(
                                    onClick = { },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Gray900, CircleShape)
                                        .border(1.dp, Gray800, CircleShape)
                                ) {
                                    Icon(Icons.Rounded.Notifications, null, tint = Gray400, modifier = Modifier.size(20.dp))
                                }
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

                    // Balance & Streak
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
                            shadowElevation = 0.dp // Custom shadow in CSS, could use modifier.shadow
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
            
            itemsIndexed(MockJars) { index, jar ->
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

            itemsIndexed(MockTransactions) { _, transaction ->
                TransactionCard(transaction = transaction)
                Spacer(modifier = Modifier.height(12.dp))
            }
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
