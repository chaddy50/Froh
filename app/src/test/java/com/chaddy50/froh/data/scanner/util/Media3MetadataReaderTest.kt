package com.chaddy50.froh.data.scanner.util

import androidx.annotation.OptIn
import androidx.media3.common.Metadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.extractor.metadata.id3.ApicFrame
import androidx.media3.extractor.metadata.id3.TextInformationFrame
import androidx.media3.extractor.metadata.vorbis.VorbisComment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(UnstableApi::class)
private fun id3Frame(frameId: String, value: String): Metadata.Entry =
    TextInformationFrame(frameId, null, listOf(value))

@OptIn(UnstableApi::class)
private fun vorbisComment(key: String, value: String): Metadata.Entry = VorbisComment(key, value)

@OptIn(UnstableApi::class)
private fun id3Entries(): List<Metadata.Entry> = listOf(
    id3Frame("TIT2", "Dead on Arrival"),
    id3Frame("TPE1", "Fall Out Boy"),
    id3Frame("TPE2", "Fall Out Boy"),
    id3Frame("TALB", "Take This to Your Grave"),
    id3Frame("TCON", "Emo"),
    id3Frame("TRCK", "02/14"),
    id3Frame("TPOS", "1/1"),
    id3Frame("TDRC", "2003"),
)

@OptIn(UnstableApi::class)
private fun vorbisEntries(): List<Metadata.Entry> = listOf(
    vorbisComment("TITLE", "Dead on Arrival"),
    vorbisComment("ARTIST", "Fall Out Boy"),
    vorbisComment("ALBUMARTIST", "Fall Out Boy"),
    vorbisComment("ALBUM", "Take This to Your Grave"),
    vorbisComment("GENRE", "Emo"),
    vorbisComment("TRACKNUMBER", "02/14"),
    vorbisComment("DISCNUMBER", "1/1"),
    vorbisComment("DATE", "2003"),
)

@RunWith(RobolectricTestRunner::class)
class ToTrackMetadataTest {

    @Test
    fun id3FramesMapToTrackMetadata() {
        val metadata = toTrackMetadata(id3Entries(), durationMicroseconds = null)

        assertEquals("Dead on Arrival", metadata.title)
        assertEquals("Fall Out Boy", metadata.artist)
        assertEquals("Fall Out Boy", metadata.albumArtist)
        assertEquals("Take This to Your Grave", metadata.album)
        assertEquals("Emo", metadata.genre)
        assertEquals("02/14", metadata.trackNumber)
        assertEquals("1/1", metadata.discNumber)
        assertEquals("2003", metadata.year)
    }

    @Test
    fun vorbisCommentsMapToTrackMetadata() {
        val metadata = toTrackMetadata(vorbisEntries(), durationMicroseconds = null)

        assertEquals("Dead on Arrival", metadata.title)
        assertEquals("Fall Out Boy", metadata.artist)
        assertEquals("Fall Out Boy", metadata.albumArtist)
        assertEquals("Take This to Your Grave", metadata.album)
        assertEquals("Emo", metadata.genre)
        assertEquals("02/14", metadata.trackNumber)
        assertEquals("1/1", metadata.discNumber)
        assertEquals("2003", metadata.year)
    }

    @Test
    fun id3AndVorbisProduceEquivalentMetadata() {
        val fromId3 = toTrackMetadata(id3Entries(), durationMicroseconds = 205_128_000L)
        val fromVorbis = toTrackMetadata(vorbisEntries(), durationMicroseconds = 205_128_000L)

        assertEquals(fromId3, fromVorbis)
    }

    @Test
    fun tdrcIsPreferredOverTyer() {
        val entries = listOf(
            id3Frame("TYER", "1999"),
            id3Frame("TDRC", "2003"),
        )

        assertEquals("2003", toTrackMetadata(entries, durationMicroseconds = null).year)
    }

    @Test
    fun unrecognisedFramesAreIgnored() {
        val entries = listOf(
            id3Frame("TSRC", "USAB12345678"),
            id3Frame("TIT1", "Content Group"),
            ApicFrame("image/jpeg", "Cover", 3, byteArrayOf(1, 2, 3)),
            vorbisComment("DESCRIPTION", "A description"),
            vorbisComment("CONTENTGROUP", "A content group"),
            id3Frame("TIT2", "Surviving Exile"),
        )

        val metadata = toTrackMetadata(entries, durationMicroseconds = null)

        assertEquals("Surviving Exile", metadata.title)
        assertNull(metadata.artist)
        assertNull(metadata.albumArtist)
        assertNull(metadata.album)
        assertNull(metadata.genre)
        assertNull(metadata.year)
        assertNull(metadata.trackNumber)
        assertNull(metadata.discNumber)
    }

    @Test
    fun missingFramesLeaveFieldsNull() {
        val metadata = toTrackMetadata(emptyList(), durationMicroseconds = null)

        assertNull(metadata.title)
        assertNull(metadata.artist)
        assertNull(metadata.albumArtist)
        assertNull(metadata.album)
        assertNull(metadata.genre)
        assertNull(metadata.year)
        assertNull(metadata.trackNumber)
        assertNull(metadata.discNumber)
        assertNull(metadata.durationMilliseconds)
    }

    @Test
    fun durationIsConvertedToMilliseconds() {
        val metadata = toTrackMetadata(emptyList(), durationMicroseconds = 205_128_000L)

        assertEquals(205_128L, metadata.durationMilliseconds)
    }

    @Test
    fun emptyFrameValueIsTreatedAsNull() {
        val metadata = toTrackMetadata(listOf(id3Frame("TCON", "")), durationMicroseconds = null)

        assertNull(metadata.genre)
    }
}
