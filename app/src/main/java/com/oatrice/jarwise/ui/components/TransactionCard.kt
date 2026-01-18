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
import com.oatrice.jarwise.data.Transaction
import com.oatrice.jarwise.ui.theme.Gray500
import com.oatrice.jarwise.ui.theme.Gray700
import com.oatrice.jarwise.ui.theme.Gray800
import com.oatrice.jarwise.ui.theme.Gray900
import com.oatrice.jarwise.utils.getJarDetails
import com.oatrice.jarwise.utils.TransactionDisplayUtils
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import androidx.compose.ui.tooling.preview.Preview
import android.content.res.Configuration
import com.oatrice.jarwise.ui.theme.JarWiseTheme

@Composable
fun TransactionCard(
    transaction: Transaction,
    currencyCode: String = "THB" // Default to THB if not provided
) {
    val jar = getJarDetails(transaction.jarId)
    
    // Date Parsing (Naive ISO parser for display)
    val displayDate = try {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        isoFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = isoFormat.parse(transaction.date)
        val displayFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        displayFormat.format(date!!)
    } catch (e: Exception) {
        transaction.date // Fallback
    }

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
                    .background(jar.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = jar.icon,
                    fontSize = 20.sp
                )
            }

            // Details
            val (displayTitle, displaySubtitle) = TransactionDisplayUtils.getDisplayDetails(transaction)
            
            Column {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (displaySubtitle.isNotEmpty()) {
                        Text(
                            text = displaySubtitle,
                            style = MaterialTheme.typography.bodySmall.copy(color = Gray500)
                        )
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Gray700)
                        )
                    }
                    Text(
                        text = displayDate,
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
                text = TransactionDisplayUtils.formatCurrency(transaction.amount, currencyCode),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFF87171) // Red 400
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

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Transaction Card Dark"
)
@Composable
fun TransactionCardPreview() {
    JarWiseTheme {
        TransactionCard(
            transaction = Transaction(
                id = 1,
                amount = 1250.00,
                jarId = "necessities",
                note = "Groceries from Lotus",
                date = "2024-05-20T10:30:00.000Z"
            )
        )
    }
}
