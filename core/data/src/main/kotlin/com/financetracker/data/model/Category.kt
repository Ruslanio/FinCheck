package com.financetracker.data.model

data class Category(
    val id: String,
    val name: String,
    val type: CategoryType,
    val isFallback: Boolean,
)
