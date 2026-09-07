package com.financetracker.core.ui.color

import androidx.compose.ui.graphics.Color

object CategoryColors {

    private val palette = listOf(
        Color(0xFF6B7FE3),
        Color(0xFF4CAF82),
        Color(0xFFE87C4A),
        Color(0xFF9C6DD8),
        Color(0xFF4A90D9),
        Color(0xFFE85D82),
        Color(0xFF45B5A8),
        Color(0xFFE8B84A),
        Color(0xFF7C8F6E),
        Color(0xFFD97C6B),
    )

    fun forId(categoryId: String): Color {
        val index = categoryId.hashCode().let { hash ->
            val mod = hash % palette.size
            if (mod < 0) mod + palette.size else mod
        }
        return palette[index]
    }
}
