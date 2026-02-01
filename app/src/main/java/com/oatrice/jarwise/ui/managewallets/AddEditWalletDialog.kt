package com.oatrice.jarwise.ui.managewallets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.oatrice.jarwise.model.Wallet
import com.oatrice.jarwise.ui.theme.Blue500
import com.oatrice.jarwise.ui.theme.Gray100
import com.oatrice.jarwise.ui.theme.Gray400
import com.oatrice.jarwise.ui.theme.Gray700
import com.oatrice.jarwise.ui.theme.Gray800
import com.oatrice.jarwise.ui.theme.Gray900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditWalletDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, parentId: String?) -> Unit,
    allWallets: List<Wallet>,
    editingWallet: Wallet? = null // Null = Add Mode
) {
    var name by remember { mutableStateOf(editingWallet?.name ?: "") }
    var parentId by remember { mutableStateOf(editingWallet?.parentId) }
    var expanded by remember { mutableStateOf(false) }

    // Logic to filter available parents:
    // 1. Cannot be self
    // 2. Cannot be a descendant (Circular Dependency)
    val availableParents = remember(allWallets, editingWallet) {
        if (editingWallet == null) {
            allWallets // Adding new: can pick anyone
        } else {
            allWallets.filter { candidate ->
                if (candidate.id == editingWallet.id) return@filter false // Cannot be self
                // Check descendant
                !isDescendant(allWallets, candidate.id, editingWallet.id)
            }
        }
    }

    val selectedParentName = availableParents.find { it.id == parentId }?.name ?: "No Parent (Top Level)"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Gray900),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (editingWallet == null) "Add Wallet" else "Edit Wallet",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )

                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Wallet Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Gray800,
                        unfocusedContainerColor = Gray800,
                        cursorColor = Blue500,
                        focusedBorderColor = Blue500,
                        unfocusedBorderColor = Gray700,
                        focusedLabelColor = Blue500,
                        unfocusedLabelColor = Gray400
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Parent Selector
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedParentName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Parent Wallet") },
                        trailingIcon = {
                            Icon(Icons.Rounded.ArrowDropDown, null, tint = Color.White)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Gray800,
                            unfocusedContainerColor = Gray800,
                            focusedBorderColor = Gray700,
                            unfocusedBorderColor = Gray700,
                            focusedLabelColor = Gray400,
                            unfocusedLabelColor = Gray400
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true }
                    )
                    
                    // Invisible overlay to catch clicks for the dropdown because TextField consumes them sometimes
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { expanded = true }
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(Gray800)
                    ) {
                        // Option: No Parent
                        DropdownMenuItem(
                            text = { Text("No Parent (Top Level)", color = Color.White) },
                            onClick = {
                                parentId = null
                                expanded = false
                            },
                            leadingIcon = {
                                if (parentId == null) Icon(Icons.Rounded.Check, null, tint = Blue500)
                            }
                        )
                        
                        Divider(color = Gray700)

                        availableParents.forEach { parent ->
                            DropdownMenuItem(
                                text = { Text(parent.name, color = Color.White) },
                                onClick = {
                                    parentId = parent.id
                                    expanded = false
                                },
                                leadingIcon = {
                                    if (parentId == parent.id) Icon(Icons.Rounded.Check, null, tint = Blue500)
                                }
                            )
                        }
                    }
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Gray400)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(name, parentId) },
                        colors = ButtonDefaults.buttonColors(containerColor = Blue500),
                        enabled = name.isNotBlank()
                    ) {
                        Text("Save", color = Color.White)
                    }
                }
            }
        }
    }
}

// Helper to check descendance for filtering
private fun isDescendant(allWallets: List<Wallet>, potentialDescendantId: String, currentWalletId: String): Boolean {
    val children = allWallets.filter { it.parentId == currentWalletId }
    for (child in children) {
        if (child.id == potentialDescendantId) return true
        if (isDescendant(allWallets, potentialDescendantId, child.id)) return true
    }
    return false
}
