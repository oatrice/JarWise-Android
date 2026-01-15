package com.oatrice.jarwise.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oatrice.jarwise.model.Jar
import com.oatrice.jarwise.ui.theme.*

@Composable
fun JarCard(jar: Jar, isPriority: Boolean) {
    var animatedProgress by remember { mutableStateOf(0f) }
    val progress = (jar.current / jar.goal).toFloat().coerceIn(0f, 1f)
    
    LaunchedEffect(Unit) {
        animatedProgress = progress
    }

    val widthProgress by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(durationMillis = 1000)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp) // Approximate height
            .clip(RoundedCornerShape(32.dp))
            .background(Gray900.copy(alpha = 0.5f))
            .border(1.dp, Gray800, RoundedCornerShape(32.dp))
    ) {
        // Pseudo-glows (Simplified)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 24.dp, y = (-24).dp)
                .size(128.dp)
                .clip(CircleShape)
                .background(jar.shadowColor.copy(alpha = 0.2f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-24).dp, y = 24.dp)
                .size(96.dp)
                .clip(CircleShape)
                .background(jar.shadowColor.copy(alpha = 0.1f))
        )

        // Content
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(jar.color.copy(alpha = 0.2f)) // Assuming color is background
                        .border(1.dp, jar.color.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = jar.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = { /* Open details */ },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Gray800.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NorthEast,
                        contentDescription = "Details",
                        tint = Gray400,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Middle
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = jar.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Gray100,
                            letterSpacing = 0.5.sp
                        )
                    )
                    
                    if (isPriority) {
                        Surface(
                            shape = RoundedCornerShape(100),
                            color = Yellow400.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Yellow400.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = Yellow400,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = "PRIORITY",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Yellow200,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "$${String.format("%,.0f", jar.current)}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White
                        )
                    )
                    Text(
                        text = "/ $${String.format("%,.0f", jar.goal)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Gray500,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            // Bottom (Progress)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Level ${jar.level}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Gray500)
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = jar.barColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(100))
                        .background(Gray800.copy(alpha = 0.8f))
                        .border(1.dp, Gray700.copy(alpha = 0.3f), CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(widthProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(100))
                            .background(jar.barColor)
                    )
                }
            }
        }
    }
}
