package com.chaddy50.froh.ui.screens.settingsScreen.genreMappings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.chaddy50.froh.data.repository.GenreMappingRepository
import com.chaddy50.froh.data.repository.GenreRepository
import com.chaddy50.froh.fakes.FakeGenreDao
import com.chaddy50.froh.fakes.FakeGenreMappingDao
import com.chaddy50.froh.fakes.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ClassicalGenreSettingsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun reportsClassicalGenresAsTopBarTitle() {
        val screenViewModel = ClassicalGenreSettingsViewModel(
            GenreRepository(FakeGenreDao()),
            GenreMappingRepository(FakeGenreMappingDao()),
        )
        var reportedTitle = ""

        composeTestRule.setContent {
            ClassicalGenreSettingsScreen(
                onNavigateBack = {},
                onRebuildLibrary = {},
                viewModel = screenViewModel,
                onTopBarContentChanged = { reportedTitle = it.title },
            )
        }

        assertEquals("Classical Genres", reportedTitle)
    }
}
