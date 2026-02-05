package com.oatrice.jarwise.ui.reportfilter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.oatrice.jarwise.ui.theme.Blue400
import com.oatrice.jarwise.ui.theme.Gray400
import com.oatrice.jarwise.ui.theme.Gray700
import com.oatrice.jarwise.ui.theme.Gray950

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFilterSheet(
    viewModel: ReportFilterViewModel,
    onApply: (Set<String>, Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Gray950
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.FilterList,
                    contentDescription = null,
                    tint = Blue400,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "Report Filters",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = "Choose jars and wallets to filter results",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray400
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = Blue400)
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                FilterContent(
                    uiState = uiState,
                    onJarSelected = viewModel::toggleJarSelection,
                    onWalletSelected = viewModel::toggleWalletSelection
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Gray700)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = viewModel::clearSelections) {
                    Text("Clear", color = Gray400)
                }
                Button(onClick = {
                    val (jarIds, walletIds) = viewModel.getSelectedFilterIds()
                    onApply(jarIds, walletIds)
                }) {
                    Text("Apply")
                }
            }
        }
    }
}

@Composable
private fun FilterContent(
    uiState: ReportFilterUiState,
    onJarSelected: (String) -> Unit,
    onWalletSelected: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Jars",
                style = MaterialTheme.typography.titleSmall,
                color = Gray400
            )
        }
        items(uiState.jars, key = { "jar_${it.id}" }) { item ->
            SelectableRow(item = item, onClick = { onJarSelected(item.id) })
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Gray700)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Wallets",
                style = MaterialTheme.typography.titleSmall,
                color = Gray400
            )
        }
        items(uiState.wallets, key = { "wallet_${it.id}" }) { item ->
            SelectableRow(item = item, onClick = { onWalletSelected(item.id) })
        }
    }
}

@Composable
private fun SelectableRow(item: SelectableItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isSelected,
            onCheckedChange = null
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )
    }
}
