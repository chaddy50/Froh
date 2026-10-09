package com.chaddy50.froh.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.chaddy50.froh.data.repository.PlaylistRepository
import com.chaddy50.froh.data.repository.TrackRepository
import com.chaddy50.froh.fakes.FakePlaylistDao
import com.chaddy50.froh.fakes.FakeTrackDao
import com.chaddy50.froh.fakes.MainDispatcherRule
import com.chaddy50.froh.navigation.HomeRoute
import com.chaddy50.froh.navigation.LocalWindowWidthSizeClass
import com.chaddy50.froh.navigation.WindowWidthSizeClass
import com.chaddy50.froh.navigation.TopBarContent
import com.chaddy50.froh.navigation.rememberAppNavigator
import com.chaddy50.froh.ui.screens.playlistsScreen.PlaylistViewModel
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun doesNotReportTopBarContentOnWideLayout() {
        var reportedTopBarContent: TopBarContent? = null

        composeTestRule.setContent {
            CompositionLocalProvider(LocalWindowWidthSizeClass provides WindowWidthSizeClass.EXPANDED) {
                HomeScreen(
                    playbackViewModel = mockk(relaxed = true),
                    playlistViewModel = PlaylistViewModel(
                        TrackRepository(FakeTrackDao()),
                        PlaylistRepository(FakePlaylistDao()),
                    ),
                    appNavigator = rememberAppNavigator(HomeRoute),
                    onTopBarContentChanged = { reportedTopBarContent = it },
                )
            }
        }

        assertNull(reportedTopBarContent)
    }
}
