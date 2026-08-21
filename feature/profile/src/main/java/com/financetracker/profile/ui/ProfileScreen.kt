package com.financetracker.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.financetracker.profile.R

@Composable
fun ProfileScreen(
    navigateToAuth: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.loggedOut.collect {
            navigateToAuth.invoke()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = viewModel::onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.label_logout))
        }
    }
}
