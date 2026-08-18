package com.financetracker.feature.transactions.components

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

val TRANSACTION_CATEGORIES = listOf(
    "Food", "Transport", "Shopping", "Health",
    "Entertainment", "Housing", "Income", "Other",
)

internal fun categoryIcon(category: String): ImageVector =
    when (category.lowercase()) {
        "food" -> Icons.Default.Restaurant
        "transport" -> Icons.Default.DirectionsCar
        "shopping" -> Icons.Default.ShoppingCart
        "health" -> Icons.Default.HealthAndSafety
        "entertainment" -> Icons.Default.SportsEsports
        "housing" -> Icons.Default.Home
        "income" -> Icons.AutoMirrored.Filled.TrendingUp
        else -> Icons.Default.Category
    }

@Composable
fun CategoryChips(
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    onClearFilter: () -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(TRANSACTION_CATEGORIES) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = {
                    if (selectedCategory == category) onClearFilter()
                    else onCategorySelected(category)
                },
                label = { Text(category) },
                leadingIcon = {
                    Icon(
                        imageVector = categoryIcon(category),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
            )
        }
    }
}
