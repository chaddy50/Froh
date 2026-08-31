package com.chaddy50.froh.ui.modalSheets.nowPlayingSheet

import android.view.Window
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NowPlayingSheetTest {

    // JUnit builds a fresh instance per test method, so each test gets its own window.
    private val window: Window by lazy {
        Robolectric.buildActivity(ComponentActivity::class.java).setup().get().window
    }

    private fun isAppearanceLightStatusBars() =
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars

    private fun isAppearanceLightNavigationBars() =
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars

    @Test
    fun disablesNavigationBarContrastEnforcement() {
        window.isNavigationBarContrastEnforced = true

        applyAlbumThemeToSystemBars(window, shouldUseDarkIcons = true)

        assertFalse(window.isNavigationBarContrastEnforced)
    }

    @Test
    fun usesDarkBarIconsWhenSurfaceIsLight() {
        applyAlbumThemeToSystemBars(window, shouldUseDarkIcons = true)

        assertTrue(isAppearanceLightStatusBars())
        assertTrue(isAppearanceLightNavigationBars())
    }

    @Test
    fun usesLightBarIconsWhenSurfaceIsDark() {
        applyAlbumThemeToSystemBars(window, shouldUseDarkIcons = false)

        assertFalse(isAppearanceLightStatusBars())
        assertFalse(isAppearanceLightNavigationBars())
    }

    @Test
    fun appliesTheSameAppearanceToBothBars() {
        listOf(true, false).forEach { shouldUseDarkIcons ->
            applyAlbumThemeToSystemBars(window, shouldUseDarkIcons)

            assertEquals(
                "Status and navigation bars must not diverge for shouldUseDarkIcons=$shouldUseDarkIcons",
                isAppearanceLightStatusBars(),
                isAppearanceLightNavigationBars(),
            )
        }
    }

    @Test
    fun contrastEnforcementStaysDisabledRegardlessOfIconAppearance() {
        listOf(true, false).forEach { shouldUseDarkIcons ->
            window.isNavigationBarContrastEnforced = true

            applyAlbumThemeToSystemBars(window, shouldUseDarkIcons)

            assertFalse(window.isNavigationBarContrastEnforced)
        }
    }

    @Test
    fun isIdempotentAcrossRepeatedCalls() {
        applyAlbumThemeToSystemBars(window, shouldUseDarkIcons = true)
        applyAlbumThemeToSystemBars(window, shouldUseDarkIcons = true)

        assertFalse(window.isNavigationBarContrastEnforced)
        assertTrue(isAppearanceLightStatusBars())
        assertTrue(isAppearanceLightNavigationBars())
    }
}
