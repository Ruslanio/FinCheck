package com.financetracker.core.ui.util

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

@Composable
fun nestedScaffoldInsets() = WindowInsets.safeDrawing
    .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)

val LocalSnackbarHostState = compositionLocalOf<SnackbarHostState?> { null }
