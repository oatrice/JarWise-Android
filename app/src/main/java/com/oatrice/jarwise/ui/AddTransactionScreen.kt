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

import com.oatrice.jarwise.utils.JARS_METADATA
import com.oatrice.jarwise.utils.JarMetadata
import com.oatrice.jarwise.utils.TransactionValidator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onBack: () -> Unit,
    onSave: (Double, String, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedJarId by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf<String?>(null) }
    var jarError by remember { mutableStateOf<String?>(null) }

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
                    val result = TransactionValidator.validateTransaction(amount, selectedJarId)
                    if (result.isValid) {
                        onSave(amount.toDouble(), selectedJarId, note)
                    } else {
                        amountError = result.errors["amount"]
                        jarError = result.errors["jarId"]
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
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { newText: String -> 
                        if (newText.all { char -> char.isDigit() || char == '.' }) {
                            amount = newText
                            amountError = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    isError = amountError != null,
                    supportingText = {
                        if (amountError != null) {
                            Text(text = amountError!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    prefix = {
                        Text(
                            "$",
                            color = Color.Gray,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (amountError != null) MaterialTheme.colorScheme.error else Color(0xFF3B82F6),
                        unfocusedBorderColor = if (amountError != null) MaterialTheme.colorScheme.error else Color(0xFF334155),
                        focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.5f),
                        unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.5f),
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        errorLabelColor = MaterialTheme.colorScheme.error
                    )
                )
            }

            // Jar Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                   Text("Select Jar", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                    if (jarError != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = jarError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    } 
                }
                

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(280.dp) // Fixed height enough for 3 rows
                ) {
                    items(JARS_METADATA) { jar ->
                        JarSelectionCard(
                            jar = jar,
                            isSelected = selectedJarId == jar.id,
                            isError = jarError != null && selectedJarId.isEmpty(),
                            onClick = { 
                                selectedJarId = jar.id
                                jarError = null
                            }
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
fun JarSelectionCard(jar: JarMetadata, isSelected: Boolean, isError: Boolean = false, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color(0xFF1F2937) else Color(0xFF1E293B).copy(alpha = 0.3f)
    val borderColor = when {
        isSelected -> Color(0xFF3B82F6).copy(alpha = 0.5f)
        isError -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
        else -> Color(0xFF1F2937)
    }
    
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
