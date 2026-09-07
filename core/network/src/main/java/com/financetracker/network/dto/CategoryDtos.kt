package com.financetracker.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryResponseDto(
    val id: String,
    val name: String,
    val type: String,
    val isFallback: Boolean,
)
