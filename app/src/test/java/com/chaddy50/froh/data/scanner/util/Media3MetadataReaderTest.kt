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

private const val ARTIST_FALL_OUT_BOY = "Fall Out Boy"
private const val ALBUM_TAKE_THIS_TO_YOUR_GRAVE = "Take This to Your Grave"
private const val TITLE_DEAD_ON_ARRIVAL = "Dead on Arrival"
private const val GENRE_EMO = "Emo"
private const val YEAR_2003 = "2003"
private const val DISC_ONE_OF_ONE = "1/1"
private const val TRACK_TWO_OF_FOURTEEN = "02/14"

@OptIn(UnstableApi::class)
private fun id3Frame(frameId: String, value: String): Metadata.Entry =
    TextInformationFrame(frameId, null, listOf(value))

@OptIn(UnstableApi::class)
private fun vorbisComment(key: String, value: String): Metadata.Entry = VorbisComment(key, value)

@OptIn(UnstableApi::class)
private fun id3Entries(): List<Metadata.Entry> = listOf(
    id3Frame("TIT2", TITLE_DEAD_ON_ARRIVAL),
    id3Frame("TPE1", ARTIST_FALL_OUT_BOY),
    id3Frame("TPE2", ARTIST_FALL_OUT_BOY),
    id3Frame("TALB", ALBUM_TAKE_THIS_TO_YOUR_GRAVE),
    id3Frame("TCON", GENRE_EMO),
    id3Frame("TRCK", TRACK_TWO_OF_FOURTEEN),
    id3Frame("TPOS", DISC_ONE_OF_ONE),
    id3Frame("TDRC", YEAR_2003),
)

/** ID3v2.2 uses three-character frame ids, which Media3 emits verbatim. */
@OptIn(UnstableApi::class)
private fun id3Version2Entries(): List<Metadata.Entry> = listOf(
    id3Frame("TT2", TITLE_DEAD_ON_ARRIVAL),
    id3Frame("TP1", ARTIST_FALL_OUT_BOY),
    id3Frame("TP2", ARTIST_FALL_OUT_BOY),
    id3Frame("TAL", ALBUM_TAKE_THIS_TO_YOUR_GRAVE),
    id3Frame("TCO", GENRE_EMO),
    id3Frame("TRK", TRACK_TWO_OF_FOURTEEN),
    id3Frame("TPA", DISC_ONE_OF_ONE),
    id3Frame("TYE", YEAR_2003),
)

@OptIn(UnstableApi::class)
private fun vorbisEntries(): List<Metadata.Entry> = listOf(
    vorbisComment("TITLE", TITLE_DEAD_ON_ARRIVAL),
    vorbisComment("ARTIST", ARTIST_FALL_OUT_BOY),
    vorbisComment("ALBUMARTIST", ARTIST_FALL_OUT_BOY),
    vorbisComment("ALBUM", ALBUM_TAKE_THIS_TO_YOUR_GRAVE),
    vorbisComment("GENRE", GENRE_EMO),
    vorbisComment("TRACKNUMBER", TRACK_TWO_OF_FOURTEEN),
    vorbisComment("DISCNUMBER", DISC_ONE_OF_ONE),
    vorbisComment("DATE", YEAR_2003),
)

@RunWith(RobolectricTestRunner::class)
class ToTrackMetadataTest {

    @Test
    fun id3FramesMapToTrackMetadata() {
        val metadata = toTrackMetadata(id3Entries(), durationMicroseconds = null)

        assertEquals(TITLE_DEAD_ON_ARRIVAL, metadata.title)
        assertEquals(ARTIST_FALL_OUT_BOY, metadata.artist)
        assertEquals(ARTIST_FALL_OUT_BOY, metadata.albumArtist)
        assertEquals(ALBUM_TAKE_THIS_TO_YOUR_GRAVE, metadata.album)
        assertEquals(GENRE_EMO, metadata.genre)
        assertEquals(TRACK_TWO_OF_FOURTEEN, metadata.trackNumber)
        assertEquals(DISC_ONE_OF_ONE, metadata.discNumber)
        assertEquals(YEAR_2003, metadata.year)
    }

    @Test
    fun legacyId3Version2FramesMapToTrackMetadata() {
        val metadata = toTrackMetadata(id3Version2Entries(), durationMicroseconds = null)

        assertEquals(TITLE_DEAD_ON_ARRIVAL, metadata.title)
        assertEquals(ARTIST_FALL_OUT_BOY, metadata.artist)
        assertEquals(ARTIST_FALL_OUT_BOY, metadata.albumArtist)
        assertEquals(ALBUM_TAKE_THIS_TO_YOUR_GRAVE, metadata.album)
        assertEquals(GENRE_EMO, metadata.genre)
        assertEquals(TRACK_TWO_OF_FOURTEEN, metadata.trackNumber)
        assertEquals(DISC_ONE_OF_ONE, metadata.discNumber)
        assertEquals(YEAR_2003, metadata.year)
    }

    @Test
    fun modernId3FramesWinOverLegacyAliases() {
        val entries = id3Version2Entries() + id3Frame("TIT2", "Modern Title")

        assertEquals("Modern Title", toTrackMetadata(entries, durationMicroseconds = null).title)
    }

    @Test
    fun vorbisCommentsMapToTrackMetadata() {
        val metadata = toTrackMetadata(vorbisEntries(), durationMicroseconds = null)

        assertEquals(TITLE_DEAD_ON_ARRIVAL, metadata.title)
        assertEquals(ARTIST_FALL_OUT_BOY, metadata.artist)
        assertEquals(ARTIST_FALL_OUT_BOY, metadata.albumArtist)
        assertEquals(ALBUM_TAKE_THIS_TO_YOUR_GRAVE, metadata.album)
        assertEquals(GENRE_EMO, metadata.genre)
        assertEquals(TRACK_TWO_OF_FOURTEEN, metadata.trackNumber)
        assertEquals(DISC_ONE_OF_ONE, metadata.discNumber)
        assertEquals(YEAR_2003, metadata.year)
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
            id3Frame("TDRC", YEAR_2003),
        )

        assertEquals(YEAR_2003, toTrackMetadata(entries, durationMicroseconds = null).year)
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
