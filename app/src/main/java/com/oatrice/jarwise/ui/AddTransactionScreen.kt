package com.oatrice.jarwise.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oatrice.jarwise.ui.theme.JarWiseTheme

// Mock Jars Data
data class Jar(val id: String, val name: String, val icon: String, val color: Color)

val JARS = listOf(
    Jar("necessities", "Necessities", "🏠", Color(0xFF3B82F6)), // Blue
    Jar("education", "Education", "📚", Color(0xFF22C55E)),   // Green
    Jar("savings", "Savings", "🐷", Color(0xFFEAB308)),     // Yellow
    Jar("play", "Play", "🎮", Color(0xFFEC4899)),        // Pink
    Jar("investment", "Investment", "📈", Color(0xFFA855F7)), // Purple
    Jar("give", "Give", "🎁", Color(0xFFEF4444))          // Red
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onBack: () -> Unit,
    onSave: (Double, String, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedJarId by remember { mutableStateOf(JARS[0].id) }

    Scaffold(
        containerColor = Color(0xFF0F172A), // Slate 900 (match Web)
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Transaction",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A).copy(alpha = 0.8f),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val amountVal = amount.toDoubleOrNull()
                    if (amountVal != null && amountVal > 0) {
                        onSave(amountVal, selectedJarId, note)
                    }
                },
                containerColor = if (amount.toDoubleOrNull() ?: 0.0 > 0.0)
                    Color(0xFF2563EB) else Color.Gray, // Blue 600
                contentColor = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save Transaction", fontWeight = FontWeight.Bold)
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Amount Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Amount", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                
                Box(contentAlignment = Alignment.CenterStart) {
                    Text(
                        "$",
                        color = Color.Gray,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amount = it },
                        modifier = Modifier
                            .fillMaxWidth(),
                        textStyle = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        placeholder = { Text("0.00", color = Color.Gray) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.5f),
                            unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.5f)
                        ),
                        contentPadding = PaddingValues(start = 40.dp, top = 24.dp, bottom = 24.dp)
                    )
                }
            }

            // Jar Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select Jar", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(280.dp) // Fixed height enough for 3 rows
                ) {
                    items(JARS) { jar ->
                        JarSelectionCard(
                            jar = jar,
                            isSelected = selectedJarId == jar.id,
                            onClick = { selectedJarId = jar.id }
                        )
                    }
                }
            }

            // Note Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Note (Optional)", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("What's this for?", color = Color.Gray) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFA855F7), // Purple
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f),
                        unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun JarSelectionCard(jar: Jar, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color(0xFF1F2937) else Color(0xFF1E293B).copy(alpha = 0.3f)
    val borderColor = if (isSelected) Color(0xFF3B82F6).copy(alpha = 0.5f) else Color(0xFF1F2937)
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth().height(80.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = jar.icon,
                style = MaterialTheme.typography.headlineSmall
            )
            Column {
                Text(
                    text = jar.name,
                    color = if (isSelected) Color.White else Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(jar.color.copy(alpha = 0.1f))
            )
        }
    }
}
