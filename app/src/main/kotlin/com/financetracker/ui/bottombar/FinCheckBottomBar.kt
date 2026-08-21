package com.financetracker.ui.bottombar

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy

@Composable
fun FinCheckBottomBar(
    destinations: List<BottomBarDestination>,
    currentDestination: NavDestination?,
    onNavigateToDestination: (screen: BottomBarDestination) -> Unit,
) {
    NavigationBar(
        contentColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        var isSelected: Boolean
        destinations.forEach { screen ->
            isSelected = currentDestination
                ?.hierarchy
                ?.any { it.hasRoute(screen.route::class) } == true
            NavigationBarItem(
                icon = {
                    NavbarItemIcon(screen.icon, isSelected)
                },
                label = { NavbarItemText(stringResource(screen.labelRes), isSelected) },
                selected = isSelected,
                onClick = { onNavigateToDestination(screen) }
            )
        }
    }
}

@Composable
private fun NavbarItemText(
    text: String,
    isSelected: Boolean
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = if (isSelected)
            MaterialTheme.colorScheme.onSurface
        else
            MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun NavbarItemIcon(
    icon: ImageVector,
    isSelected: Boolean
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isSelected)
            MaterialTheme.colorScheme.onSecondaryContainer
        else
            MaterialTheme.colorScheme.onSurfaceVariant
    )
}
