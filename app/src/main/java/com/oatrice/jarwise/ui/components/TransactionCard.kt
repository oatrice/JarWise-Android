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
    linkedTransaction: Transaction? = null, // For transfers: the counterpart transaction
    currencyCode: String = "THB",
    showDate: Boolean = true
) {
    val isTransfer = transaction.linkedTransactionId != null
    val jar = getJarDetails(transaction.jarId)
    
    // Date Parsing (Naive ISO parser for display)
    val displayDate = try {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        isoFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = isoFormat.parse(transaction.date)
        val displayFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        isoFormat.parse(transaction.date)?.let { date ->
            displayFormat.format(date)
        } ?: transaction.date
    } catch (e: Exception) {
        transaction.date // Fallback
    }

    // Draft styling colors
    val isDraft = transaction.status == "draft"
    val cardBackground = if (isDraft) Color(0xFF422006).copy(alpha = 0.2f) else Gray900.copy(alpha = 0.4f)
    val cardBorder = if (isDraft) Color(0xFFFBBF24).copy(alpha = 0.3f) else Gray800.copy(alpha = 0.5f)

    // Transfer styling
    val transferBlue = Color(0xFF3B82F6) // Blue 500

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBackground)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .clickable { /* Handle click */ }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon: Use transfer icon or jar icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isTransfer) transferBlue.copy(alpha = 0.2f) else jar.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isTransfer) "🔄" else jar.icon,
                    fontSize = 20.sp
                )
            }

            // Details
            val wallet = com.oatrice.jarwise.utils.getWalletDetails(transaction.walletId)
            val destWallet = linkedTransaction?.let { com.oatrice.jarwise.utils.getWalletDetails(it.walletId) }
            
            // Transfer: "SourceWallet → DestWallet", Normal: Note or Jar Name
            val displayTitle = if (isTransfer && destWallet != null) {
                "${wallet.name} → ${destWallet.name}"
            } else if (transaction.note.isNotBlank()) {
                transaction.note
            } else {
                jar.name
            }
            
            // Subtitle: "Transfer" for transfers, or Jar • Wallet for normal
            val displaySubtitle = if (isTransfer) {
                "Transfer"
            } else {
                val subtitleParts = mutableListOf<String>()
                if (transaction.note.isNotBlank()) {
                    subtitleParts.add(jar.name)
                }
                subtitleParts.add(wallet.name)
                subtitleParts.joinToString(" • ")
            }
            
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    // Draft badge
                    if (isDraft) {
                        Surface(
                            color = Color(0xFFFBBF24).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "DRAFT",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFFBBF24),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
                Text(
                    text = displaySubtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = Gray500),
                    maxLines = 1
                )
            }
        }

        // Amount & Date
        Column(horizontalAlignment = Alignment.End) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Amount color: Blue for transfers, Green for income, Red for expense
                val amountColor = when {
                    isTransfer -> transferBlue
                    transaction.type == "income" -> Color(0xFF4ADE80) // Green 400
                    else -> Color(0xFFF87171) // Red 400
                }
                Text(
                    text = TransactionDisplayUtils.formatCurrency(transaction.amount, currencyCode),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = amountColor
                    )
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowRight,
                    contentDescription = null,
                    tint = Gray500,
                    modifier = Modifier.size(16.dp)
                )
            }
            if (showDate) {
                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Gray500,
                        fontSize = 11.sp
                    )
                )
            }
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
