package com.chaddy50.froh.data.scanner.util

import android.content.Context
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.chaddy50.froh.utilities.resolveId3GenreName
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Assets in the androidTest source set are packaged into the test APK, so they are readable from
 * the instrumentation context — not from the app under test. The copy lands in the app's cache so
 * the reader can open it.
 */
private fun copyAssetToCache(assetName: String): Uri {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val cachedFile = File(instrumentation.targetContext.cacheDir, assetName)
    instrumentation.context.assets.open(assetName).use { input ->
        cachedFile.outputStream().use { output -> input.copyTo(output) }
    }
    return Uri.fromFile(cachedFile)
}

@RunWith(AndroidJUnit4::class)
class Media3MetadataReaderReadTest {

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun readsGenreFromMp3WithNumericId3GenreCode() = runBlocking {
        val reader = Media3MetadataReader(context)

        val metadata = reader.read(copyAssetToCache("numeric_genre_id3.mp3"))

        assertNotNull("Media3 should read the MP3", metadata)
        assertEquals("(161)", metadata?.genre)
        assertEquals("Emo", resolveId3GenreName(metadata?.genre))
        assertEquals("Dead on Arrival", metadata?.title)
        assertEquals("Fall Out Boy", metadata?.artist)
    }

    @Test
    fun readsAllTagsFromFlacVorbisComments() = runBlocking {
        val reader = Media3MetadataReader(context)

        val metadata = reader.read(copyAssetToCache("vorbis_comments.flac"))

        assertNotNull("Media3 should read the FLAC", metadata)
        assertEquals("Surviving Exile", metadata?.title)
        assertEquals("Darren Korb", metadata?.artist)
        assertEquals("Pyre", metadata?.albumArtist)
        assertEquals("Pyre - Original Soundtrack", metadata?.album)
        assertEquals("Video Game", metadata?.genre)
        assertEquals("2017", metadata?.year)
        assertEquals("06", metadata?.trackNumber)
    }

    @Test
    fun returnsNullForUnreadableFile() = runBlocking {
        val reader = Media3MetadataReader(context)

        val metadata = reader.read(copyAssetToCache("unreadable.bin"))

        assertNull(metadata)
    }
}
