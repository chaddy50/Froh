package com.chaddy50.froh.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.chaddy50.froh.navigation.AppNavigator
import com.chaddy50.froh.navigation.SettingsRoute
import com.chaddy50.froh.navigation.TopBarContent
import com.chaddy50.froh.ui.composables.SettingsGearButton
import com.chaddy50.froh.ui.screens.genresScreen.GenresScreen
import com.chaddy50.froh.ui.screens.playlistsScreen.PlaylistsScreen
import com.chaddy50.froh.ui.composables.nowPlayingBar.PlaybackViewModel
import com.chaddy50.froh.ui.screens.playlistsScreen.PlaylistViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    playbackViewModel: PlaybackViewModel,
    playlistViewModel: PlaylistViewModel,
    appNavigator: AppNavigator,
    onTopBarContentChanged: (TopBarContent) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        val title = if (pagerState.currentPage == 0) "Library" else "Playlists"
        onTopBarContentChanged(
            TopBarContent(
                title = title,
                actions = { SettingsGearButton(onClick = { appNavigator.push(SettingsRoute) }) },
            )
        )
    }

    Column {
        TabRow(selectedTabIndex = pagerState.currentPage) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                icon = { Icon(Icons.Filled.LibraryMusic, contentDescription = null) },
            )
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                icon = { Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null) },
            )
        }

        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
        ) { page ->
            when (page) {
                0 -> GenresScreen(playbackViewModel, playlistViewModel, appNavigator)
                1 -> PlaylistsScreen(playlistViewModel, appNavigator)
            }
        }
    }
}
