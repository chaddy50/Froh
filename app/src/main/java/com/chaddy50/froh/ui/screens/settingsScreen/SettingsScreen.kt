package com.chaddy50.froh.ui.screens.settingsScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chaddy50.froh.navigation.AppNavigator
import com.chaddy50.froh.navigation.ClassicalGenreSettingsRoute
import com.chaddy50.froh.navigation.LocalWindowWidthSizeClass
import com.chaddy50.froh.navigation.WindowWidthSizeClass
import com.chaddy50.froh.navigation.TopBarContent
import com.chaddy50.froh.ui.screens.settingsScreen.genreMappings.ClassicalGenreSettingsRow
import com.chaddy50.froh.ui.screens.settingsScreen.listenBrainzLogin.ListenBrainzLogin

@Composable
fun SettingsScreen(
    appNavigator: AppNavigator,
    onTopBarContentChanged: (TopBarContent) -> Unit = {},
) {
    LaunchedEffect(Unit) {
        onTopBarContentChanged(TopBarContent(title = "Settings"))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = if (LocalWindowWidthSizeClass.current == WindowWidthSizeClass.EXPANDED) 120.dp else 0.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ListenBrainzLogin()
        ClassicalGenreSettingsRow(onClick = { appNavigator.push(ClassicalGenreSettingsRoute) })
    }
}
