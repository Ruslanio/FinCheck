package com.financetracker.core.ui.util

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable

@Composable
fun nestedScaffoldInsets() = WindowInsets.safeDrawing
    .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
