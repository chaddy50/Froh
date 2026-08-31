package com.chaddy50.froh.ui.modalSheets.nowPlayingSheet

import android.content.Context
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun getColorSchemeForAlbumArtwork(artworkUri: Uri?, defaultColorScheme: ColorScheme): ColorScheme {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    var albumColorScheme by remember { mutableStateOf<ColorScheme?>(null) }
    LaunchedEffect(artworkUri, isDark) {
        albumColorScheme = null
        if (artworkUri != null) {
            albumColorScheme = buildAlbumColorScheme(context, artworkUri, isDark)
        }
    }
    val colorScheme = albumColorScheme ?: defaultColorScheme
    val spec = tween<Color>(500)

    val primary by animateColorAsState(colorScheme.primary, spec, label = "primary")
    val onPrimary by animateColorAsState(colorScheme.onPrimary, spec, label = "onPrimary")
    val primaryContainer by animateColorAsState(colorScheme.primaryContainer, spec, label = "primaryContainer")
    val onPrimaryContainer by animateColorAsState(colorScheme.onPrimaryContainer, spec, label = "onPrimaryContainer")
    val secondary by animateColorAsState(colorScheme.secondary, spec, label = "secondary")
    val onSecondary by animateColorAsState(colorScheme.onSecondary, spec, label = "onSecondary")
    val secondaryContainer by animateColorAsState(colorScheme.secondaryContainer, spec, label = "secondaryContainer")
    val onSecondaryContainer by animateColorAsState(colorScheme.onSecondaryContainer, spec, label = "onSecondaryContainer")
    val tertiary by animateColorAsState(colorScheme.tertiary, spec, label = "tertiary")
    val onTertiary by animateColorAsState(colorScheme.onTertiary, spec, label = "onTertiary")
    val tertiaryContainer by animateColorAsState(colorScheme.tertiaryContainer, spec, label = "tertiaryContainer")
    val onTertiaryContainer by animateColorAsState(colorScheme.onTertiaryContainer, spec, label = "onTertiaryContainer")
    val background by animateColorAsState(colorScheme.background, spec, label = "background")
    val onBackground by animateColorAsState(colorScheme.onBackground, spec, label = "onBackground")
    val surface by animateColorAsState(colorScheme.surface, spec, label = "surface")
    val onSurface by animateColorAsState(colorScheme.onSurface, spec, label = "onSurface")
    val surfaceVariant by animateColorAsState(colorScheme.surfaceVariant, spec, label = "surfaceVariant")
    val onSurfaceVariant by animateColorAsState(colorScheme.onSurfaceVariant, spec, label = "onSurfaceVariant")
    val surfaceTint by animateColorAsState(colorScheme.surfaceTint, spec, label = "surfaceTint")
    val inverseSurface by animateColorAsState(colorScheme.inverseSurface, spec, label = "inverseSurface")
    val inverseOnSurface by animateColorAsState(colorScheme.inverseOnSurface, spec, label = "inverseOnSurface")
    val inversePrimary by animateColorAsState(colorScheme.inversePrimary, spec, label = "inversePrimary")
    val error by animateColorAsState(colorScheme.error, spec, label = "error")
    val onError by animateColorAsState(colorScheme.onError, spec, label = "onError")
    val errorContainer by animateColorAsState(colorScheme.errorContainer, spec, label = "errorContainer")
    val onErrorContainer by animateColorAsState(colorScheme.onErrorContainer, spec, label = "onErrorContainer")
    val outline by animateColorAsState(colorScheme.outline, spec, label = "outline")
    val outlineVariant by animateColorAsState(colorScheme.outlineVariant, spec, label = "outlineVariant")
    val scrim by animateColorAsState(colorScheme.scrim, spec, label = "scrim")

    return colorScheme.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceTint = surfaceTint,
        inverseSurface = inverseSurface,
        inverseOnSurface = inverseOnSurface,
        inversePrimary = inversePrimary,
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        onErrorContainer = onErrorContainer,
        outline = outline,
        outlineVariant = outlineVariant,
        scrim = scrim
    )
}

private suspend fun buildAlbumColorScheme(context: Context, artworkUri: Uri, isDark: Boolean): ColorScheme? {
    val loader = ImageLoader(context)
    val request = ImageRequest.Builder(context)
        .data(artworkUri)
        .size(256, 256)
        .allowHardware(false)
        .build()
    val result = loader.execute(request)
    val bitmap = (result as? SuccessResult)?.drawable?.let {
        (it as? android.graphics.drawable.BitmapDrawable)?.bitmap
    } ?: return null

    val palette = withContext(Dispatchers.Default) { Palette.from(bitmap).generate() }

    val vibrant = palette.vibrantSwatch
    val darkVibrant = palette.darkVibrantSwatch
    val lightVibrant = palette.lightVibrantSwatch
    val muted = palette.mutedSwatch
    val darkMuted = palette.darkMutedSwatch
    val lightMuted = palette.lightMutedSwatch

    if (vibrant == null && darkVibrant == null && muted == null) return null

    return buildColorScheme(isDark, vibrant, darkVibrant, lightVibrant, muted, darkMuted, lightMuted)
}

private fun buildColorScheme(
    isDark: Boolean,
    vibrant: Palette.Swatch?,
    darkVibrant: Palette.Swatch?,
    lightVibrant: Palette.Swatch?,
    muted: Palette.Swatch?,
    darkMuted: Palette.Swatch?,
    lightMuted: Palette.Swatch?
): ColorScheme {
    val anchor = if (isDark) Color.Black else Color.White

    val rawSurface = Color(
        if (isDark) darkVibrant?.rgb ?: muted?.rgb ?: vibrant!!.rgb
        else        lightVibrant?.rgb ?: muted?.rgb ?: vibrant!!.rgb
    )
    val surface = lerp(anchor, rawSurface, 0.45f)

    val primary = Color(
        if (isDark) lightVibrant?.rgb ?: vibrant?.rgb ?: muted!!.rgb
        else        darkVibrant?.rgb ?: vibrant?.rgb ?: muted!!.rgb
    )
    val onPrimary = getContrastingColor(primary)
    val primaryContainer = Color(vibrant?.rgb ?: (if (isDark) lightVibrant else darkVibrant)?.rgb ?: muted!!.rgb)
    val onPrimaryContainer = getContrastingColor(primaryContainer)

    val secondary = Color(muted?.rgb ?: vibrant?.rgb ?: darkVibrant!!.rgb)
    val onSecondary = getContrastingColor(secondary)
    val secondaryContainer = Color(
        if (isDark) darkMuted?.rgb ?: darkVibrant?.rgb ?: muted?.rgb ?: vibrant!!.rgb
        else        lightMuted?.rgb ?: lightVibrant?.rgb ?: muted?.rgb ?: vibrant!!.rgb
    )
    val onSecondaryContainer = getContrastingColor(secondaryContainer)

    val tertiary = Color(
        if (isDark) lightMuted?.rgb ?: lightVibrant?.rgb ?: muted?.rgb ?: vibrant!!.rgb
        else        darkMuted?.rgb ?: darkVibrant?.rgb ?: muted?.rgb ?: vibrant!!.rgb
    )

    val rawSurfaceVariant = Color(
        if (isDark) darkMuted?.rgb ?: darkVibrant?.rgb ?: muted?.rgb ?: vibrant!!.rgb
        else        lightMuted?.rgb ?: lightVibrant?.rgb ?: muted?.rgb ?: vibrant!!.rgb
    )
    val surfaceVariant = lerp(anchor, rawSurfaceVariant, 0.55f)
    val onSurfaceVariant = getContrastingColor(surfaceVariant)

    val onSurface = getContrastingColor(surface)
    val defaults = if (isDark) darkColorScheme() else lightColorScheme()
    return defaults.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        background = surface,
        onBackground = onSurface,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceTint = primary
    )
}

@androidx.annotation.VisibleForTesting
internal fun getContrastingColor(background: Color): Color =
    if (background.luminance() > 0.179f) Color.Black else Color.White

@androidx.annotation.VisibleForTesting
internal fun shouldUseDarkSystemBarIcons(surfaceColor: Color): Boolean =
    getContrastingColor(surfaceColor) == Color.Black