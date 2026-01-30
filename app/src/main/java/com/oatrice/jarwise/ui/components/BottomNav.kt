package com.oatrice.jarwise.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.oatrice.jarwise.ui.theme.Blue400
import com.oatrice.jarwise.ui.theme.Blue500
import com.oatrice.jarwise.ui.theme.Gray500
import com.oatrice.jarwise.ui.theme.Gray800
import com.oatrice.jarwise.ui.theme.Gray900

enum class NavPage {
    DASHBOARD, HISTORY, ADD, BUDGET, PROFILE
}

/**
 * Floating Bottom Navigation Bar
 * Matches the premium glassmorphism design from Web
 */
@Composable
fun BottomNav(
    activePage: NavPage = NavPage.DASHBOARD,
    visible: Boolean = true,
    onNavigate: (NavPage) -> Unit
) {
    // Animate visibility
    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 250.dp, // Increased from 120.dp to ensure fully hidden
        animationSpec = spring(stiffness = 200f, dampingRatio = 0.7f),
        label = "BottomNavOffset"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars) // Handle system navigation bar
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
            .offset(y = offsetY),
        contentAlignment = Alignment.Center
    ) {
        // Nav bar container with glassmorphism
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(Gray900.copy(alpha = 0.9f))
                .border(1.dp, Gray800, RoundedCornerShape(32.dp))
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dashboard
            NavItem(
                icon = Icons.Rounded.Dashboard,
                isActive = activePage == NavPage.DASHBOARD,
                onClick = { onNavigate(NavPage.DASHBOARD) }
            )
            
            // History
            NavItem(
                icon = Icons.Rounded.History,
                isActive = activePage == NavPage.HISTORY,
                onClick = { onNavigate(NavPage.HISTORY) }
            )
            
            // Add Button (Center, elevated)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .offset(y = (-8).dp)
                    .shadow(8.dp, CircleShape, ambientColor = Blue500.copy(alpha = 0.4f))
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Blue500, Color(0xFF6366F1))
                        )
                    )
                    .border(4.dp, Color(0xFF0A0A0A), CircleShape)
                    .clickable { onNavigate(NavPage.ADD) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "Add",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            // Budget/Wallet
            NavItem(
                icon = Icons.Rounded.AccountBalanceWallet,
                isActive = activePage == NavPage.BUDGET,
                onClick = { onNavigate(NavPage.BUDGET) }
            )
            
            // Profile
            NavItem(
                icon = Icons.Rounded.Person,
                isActive = activePage == NavPage.PROFILE,
                onClick = { onNavigate(NavPage.PROFILE) }
            )
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) Blue400 else Gray500,
            modifier = Modifier.size(24.dp)
        )
    }
}
