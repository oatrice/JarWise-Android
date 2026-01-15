package com.oatrice.jarwise.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oatrice.jarwise.model.Transaction
import com.oatrice.jarwise.ui.theme.Gray500
import com.oatrice.jarwise.ui.theme.Gray700
import com.oatrice.jarwise.ui.theme.Gray800
import com.oatrice.jarwise.ui.theme.Gray900

@Composable
fun TransactionCard(transaction: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Gray900.copy(alpha = 0.4f))
            .border(1.dp, Gray800.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable { /* Handle click */ }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(transaction.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = transaction.icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = transaction.iconTint
                )
            }

            // Details
            Column {
                Text(
                    text = transaction.merchant,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.bodySmall.copy(color = Gray500)
                    )
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(Gray700)
                    )
                    Text(
                        text = transaction.date,
                        style = MaterialTheme.typography.bodySmall.copy(color = Gray500)
                    )
                }
            }
        }

        // Amount & Arrow
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "-$${String.format("%.2f", transaction.amount)}",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowRight,
                contentDescription = null,
                tint = Gray500,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
