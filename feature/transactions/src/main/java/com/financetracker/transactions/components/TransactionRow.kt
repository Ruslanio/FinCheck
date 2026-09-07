package com.financetracker.transactions.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.financetracker.core.ui.color.CategoryColors
import com.financetracker.data.model.CategoryType
import com.financetracker.data.model.TransactionUiModel

@Composable
fun TransactionRow(transaction: TransactionUiModel) {
    val isExpense = when (transaction.categoryType) {
        CategoryType.INCOME -> false
        CategoryType.EXPENSE -> true
        null -> transaction.amount < 0
    }

    val amountColor = if (isExpense) Color(0xFFE24B4A) else Color(0xFF1D9E75)

    val formattedAmount = remember(transaction.amount, isExpense) {
        val prefix = if (isExpense) "-" else "+"
        "$prefix${"%.2f".format(kotlin.math.abs(transaction.amount))}"
    }

    val formattedDate = remember(transaction.occurredAt) {
        val instant = java.time.Instant.ofEpochMilli(transaction.occurredAt)
        val local = instant.atZone(java.time.ZoneId.systemDefault())
        val month = local.month.name.lowercase().replaceFirstChar { it.uppercase() }
        "${local.dayOfMonth} $month ${local.year}"
    }

    val categoryColor = remember(transaction.categoryId) {
        CategoryColors.forId(transaction.categoryId)
    }

    val displayLabel = transaction.description ?: transaction.categoryName ?: transaction.categoryId

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = categoryIcon(transaction.categoryName ?: ""),
                    contentDescription = transaction.categoryName,
                    modifier = Modifier.size(20.dp),
                    tint = categoryColor,
                )
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = displayLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Text(
            text = formattedAmount,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = amountColor,
        )
    }
}
