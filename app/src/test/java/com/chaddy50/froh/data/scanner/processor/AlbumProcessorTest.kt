package com.chaddy50.froh.data.scanner.processor

import com.chaddy50.froh.data.entity.Album
import com.chaddy50.froh.data.repository.IAlbumRepository
import com.chaddy50.froh.data.scanner.util.UNKNOWN_ALBUM
import com.chaddy50.froh.data.scanner.util.IArtworkSaver
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private const val ALBUM_SYMPHONY = "Symphony No. 5"
private const val ALBUM_ABBEY_ROAD = "Abbey Road"
private const val ALBUM_PYRE = "Pyre - Original Soundtrack"
private const val ALBUM_PIANO_SONATA = "Piano Sonata K. 545"
private const val ALBUM_HUNGARIAN_DANCE = "Hungarian Dance WoO 1"
private const val YEAR_1808 = "1808"
private const val YEAR_2017 = "2017"

class AlbumProcessorTest {

    private suspend fun process(
        processor: AlbumProcessor,
        albumId: Long = 100L,
        trackId: Long = 1L,
        albumArtistId: Long = 5L,
        albumName: String = "Symphony No. 5 Op. 67",
        albumYear: String = YEAR_1808,
    ) = processor.process(albumId, trackId, albumArtistId, albumName, albumYear)

    @Test
    fun returnsAlbumProcessorResult() = runTest {
        val repo = FakeAlbumRepository()
        val artworkSaver = FakeArtworkSaver(artworkPath = "/art/100.jpg")
        val processor = AlbumProcessor(repo, artworkSaver)

        val result = process(processor)

        assertEquals(100L, result.albumId)
        assertEquals(ALBUM_SYMPHONY, result.albumName)
        assertEquals("/art/100.jpg", result.artworkPath)
        assertEquals(YEAR_1808, result.year)
    }

    @Test
    fun insertsAlbumWithCorrectFields() = runTest {
        val repo = FakeAlbumRepository()
        val processor = AlbumProcessor(repo, FakeArtworkSaver())

        process(processor)

        val album = repo.lastInsertedAlbum!!
        assertEquals(100L, album.id)
        assertEquals(ALBUM_SYMPHONY, album.title)
        assertEquals(670_000, album.catalogueSortIndex)
        assertEquals("Op. 67", album.catalogueString)
        assertEquals(5L, album.artistId)
        assertEquals(YEAR_1808, album.year)
    }

    @Test
    fun resolvedValuesAreUsedInsteadOfCursorFields() = runTest {
        val repo = FakeAlbumRepository()
        val processor = AlbumProcessor(repo, FakeArtworkSaver())

        // Album name and year now come from Media3, not the MediaStore row
        val result = process(processor, albumName = ALBUM_PYRE, albumYear = YEAR_2017)

        assertEquals(ALBUM_PYRE, result.albumName)
        assertEquals(YEAR_2017, result.year)
        assertEquals(ALBUM_PYRE, repo.lastInsertedAlbum?.title)
        assertEquals(YEAR_2017, repo.lastInsertedAlbum?.year)
    }

    @Test
    fun secondCallReturnsCachedResult() = runTest {
        val repo = FakeAlbumRepository()
        val processor = AlbumProcessor(repo, FakeArtworkSaver())

        process(processor, trackId = 1L)
        val result = process(processor, trackId = 2L)

        assertEquals(100L, result.albumId)
        assertEquals(1, repo.insertCount)
    }

    @Test
    fun differentAlbumIdsAreNotCached() = runTest {
        val repo = FakeAlbumRepository()
        val processor = AlbumProcessor(repo, FakeArtworkSaver())

        process(processor, albumId = 100L, trackId = 1L, albumYear = YEAR_1808)
        process(processor, albumId = 200L, trackId = 2L, albumYear = "1900")

        assertEquals(2, repo.insertCount)
    }

    @Test
    fun unknownAlbumNameIsPassedThrough() = runTest {
        val repo = FakeAlbumRepository()
        val processor = AlbumProcessor(repo, FakeArtworkSaver())

        // MetadataResolver substitutes UNKNOWN_ALBUM before the processor sees the name
        val result = process(processor, albumName = UNKNOWN_ALBUM)

        assertEquals("Unknown Album", result.albumName)
    }

    @Test
    fun missingAlbumIdIsPassedThroughAsNegativeOne() = runTest {
        val repo = FakeAlbumRepository()
        val processor = AlbumProcessor(repo, FakeArtworkSaver())

        // MusicScanner substitutes -1 when MediaStore has no ALBUM_ID for the row
        val result = process(processor, albumId = -1L)

        assertEquals(-1L, result.albumId)
    }

    @Test
    fun albumWithNoCatalogueNumberGetsDefault() = runTest {
        val repo = FakeAlbumRepository()
        val processor = AlbumProcessor(repo, FakeArtworkSaver())

        process(processor, albumName = ALBUM_ABBEY_ROAD, albumYear = "1969")

        assertEquals(99_999_999, repo.lastInsertedAlbum?.catalogueSortIndex)
        assertNull(repo.lastInsertedAlbum?.catalogueString)
    }

    @Test
    fun nullArtworkPathPassedThrough() = runTest {
        val repo = FakeAlbumRepository()
        val processor = AlbumProcessor(repo, FakeArtworkSaver(artworkPath = null))

        val result = process(processor)

        assertNull(result.artworkPath)
    }
}

class ExtractCatalogueSortIndexTest {
    // Catalogue number is a compound sort key:
    // prefixPriority * 10_000_000 + mainNumber * 10_000 + letterSuffix * 100 + subNumber
    // WoO has prefixPriority=1, all others have 0

    // --- Opus ---
    @Test
    fun opWithDotAndSpace() {
        assertEquals(1_180_000, extractCatalogueSortIndex("Piano Pieces Op. 118"))
    }

    @Test
    fun opWithDotNoSpace() {
        assertEquals(270_000, extractCatalogueSortIndex("Piano Concerto Op.27"))
    }

    @Test
    fun opNoDot() {
        assertEquals(670_000, extractCatalogueSortIndex("Symphony Op 67"))
    }

    @Test
    fun opCaseInsensitive() {
        assertEquals(100_000, extractCatalogueSortIndex("Sonata op. 10"))
    }

    // --- Köchel (Mozart) ---

    @Test
    fun kWithDot() {
        assertEquals(5_450_000, extractCatalogueSortIndex(ALBUM_PIANO_SONATA))
    }

    @Test
    fun kNoDot() {
        assertEquals(4_660_000, extractCatalogueSortIndex("Piano Concerto K 466"))
    }

    // --- Bach ---

    @Test
    fun bwv() {
        assertEquals(10_410_000, extractCatalogueSortIndex("Violin Concerto BWV 1041"))
    }

    @Test
    fun bwvCaseInsensitive() {
        assertEquals(5_650_000, extractCatalogueSortIndex("Toccata and Fugue bwv 565"))
    }

    // --- Hoboken (Haydn) ---

    @Test
    fun hobWithDot() {
        assertEquals(940_000, extractCatalogueSortIndex("Symphony Hob. 94"))
    }

    @Test
    fun hobNoDot() {
        assertEquals(1_040_000, extractCatalogueSortIndex("Symphony Hob 104"))
    }

    // --- Ryom (Vivaldi) ---

    @Test
    fun rv() {
        assertEquals(2_690_000, extractCatalogueSortIndex("The Four Seasons RV 269"))
    }

    // --- Deutsch (Schubert) ---

    @Test
    fun dWithDot() {
        assertEquals(9_440_000, extractCatalogueSortIndex("Symphony D. 944"))
    }

    @Test
    fun dNoDot() {
        assertEquals(8_100_000, extractCatalogueSortIndex("String Quartet D 810"))
    }

    // --- S (Liszt) ---

    @Test
    fun sWithDot() {
        assertEquals(1_390_000, extractCatalogueSortIndex("Hungarian Rhapsody S. 139"))
    }

    // --- M (Ravel) ---

    @Test
    fun mWithDot() {
        assertEquals(770_000, extractCatalogueSortIndex("Daphnis et Chloé M. 77"))
    }

    // --- L (Debussy) ---

    @Test
    fun lWithDot() {
        assertEquals(750_000, extractCatalogueSortIndex("Prélude à l'après-midi d'un faune L. 75"))
    }

    // --- No match ---

    @Test
    fun noMatchReturnsDefault() {
        assertEquals(99_999_999, extractCatalogueSortIndex(ALBUM_SYMPHONY))
    }

    @Test
    fun emptyStringReturnsDefault() {
        assertEquals(99_999_999, extractCatalogueSortIndex(""))
    }

    @Test
    fun plainAlbumNameReturnsDefault() {
        assertEquals(99_999_999, extractCatalogueSortIndex(ALBUM_ABBEY_ROAD))
    }

    // --- Edge cases ---

    @Test
    fun firstMatchWins() {
        assertEquals(100_000, extractCatalogueSortIndex("Sonata Op. 10 BWV 999"))
    }

    @Test
    fun catalogNumberAtEndOfString() {
        assertEquals(3_310_000, extractCatalogueSortIndex("K. 331"))
    }

    @Test
    fun noSpaceBetweenPrefixAndNumber() {
        assertEquals(270_000, extractCatalogueSortIndex("Op.27"))
    }

    // --- Sub-pieces ---

    @Test
    fun opWithSubPieceNumber() {
        // Op. 1, No. 1 = 0 + 1*10000 + 0 + 1 = 10001
        assertEquals(10_001, extractCatalogueSortIndex("Op. 1, No. 1"))
    }

    @Test
    fun opWithLetterSuffix() {
        // Op. 3a = 0 + 3*10000 + 1*100 + 0 = 30100
        assertEquals(30_100, extractCatalogueSortIndex("Op. 3a"))
    }

    @Test
    fun subPieceSortsCorrectly() {
        // Op. 21, No. 1 should sort before Op. 21, No. 2
        val no1 = extractCatalogueSortIndex("Op. 21, No. 1")
        val no2 = extractCatalogueSortIndex("Op. 21, No. 2")
        assertEquals(210_001, no1)
        assertEquals(210_002, no2)
        assertTrue(no1 < no2)
    }

    // --- WoO (Brahms) ---

    @Test
    fun woo() {
        // WoO 1 = 1*10_000_000 + 1*10000 + 0 + 0 = 10_010_000
        assertEquals(10_010_000, extractCatalogueSortIndex(ALBUM_HUNGARIAN_DANCE))
    }

    @Test
    fun wooSortsAfterOp() {
        val op = extractCatalogueSortIndex("Op. 1")
        val woo = extractCatalogueSortIndex("WoO 1")
        assertTrue(woo > op)
    }
}

class ExtractCatalogueStringTest {
    @Test
    fun opWithDotAndSpace() {
        assertEquals("Op. 67", extractCatalogueString("Symphony No. 5 Op. 67"))
    }

    @Test
    fun opWithDotNoSpace() {
        assertEquals("Op.27", extractCatalogueString("Piano Concerto Op.27"))
    }

    @Test
    fun opNoDot() {
        assertEquals("Op 67", extractCatalogueString("Symphony Op 67"))
    }

    @Test
    fun opCaseInsensitive() {
        assertEquals("op. 10", extractCatalogueString("Sonata op. 10"))
    }

    @Test
    fun kWithDot() {
        assertEquals("K. 545", extractCatalogueString(ALBUM_PIANO_SONATA))
    }

    @Test
    fun kNoDot() {
        assertEquals("K 466", extractCatalogueString("Piano Concerto K 466"))
    }

    @Test
    fun bwv() {
        assertEquals("BWV 1041", extractCatalogueString("Violin Concerto BWV 1041"))
    }

    @Test
    fun hob() {
        assertEquals("Hob. 94", extractCatalogueString("Symphony Hob. 94"))
    }

    @Test
    fun rv() {
        assertEquals("RV 269", extractCatalogueString("The Four Seasons RV 269"))
    }

    @Test
    fun dWithDot() {
        assertEquals("D. 944", extractCatalogueString("Symphony D. 944"))
    }

    @Test
    fun sWithDot() {
        assertEquals("S. 139", extractCatalogueString("Hungarian Rhapsody S. 139"))
    }

    @Test
    fun mWithDot() {
        assertEquals("M. 77", extractCatalogueString("Daphnis et Chloé M. 77"))
    }

    @Test
    fun lWithDot() {
        assertEquals("L. 75", extractCatalogueString("Prélude à l'après-midi d'un faune L. 75"))
    }

    @Test
    fun noMatchReturnsNull() {
        assertNull(extractCatalogueString(ALBUM_SYMPHONY))
    }

    @Test
    fun emptyStringReturnsNull() {
        assertNull(extractCatalogueString(""))
    }

    @Test
    fun plainAlbumNameReturnsNull() {
        assertNull(extractCatalogueString(ALBUM_ABBEY_ROAD))
    }

    @Test
    fun firstMatchWins() {
        assertEquals("Op. 10", extractCatalogueString("Sonata Op. 10 BWV 999"))
    }

    // --- Sub-pieces ---

    @Test
    fun opWithCommaNoSubPiece() {
        assertEquals("Op. 1, No. 1", extractCatalogueString("Op. 1, No. 1 - Sonata in F"))
    }

    @Test
    fun opWithNoSubPieceNoComma() {
        assertEquals("Op. 1 No. 1", extractCatalogueString("Op. 1 No. 1 - Sonata in F"))
    }

    @Test
    fun opWithLetterSuffix() {
        assertEquals("Op. 3a", extractCatalogueString("Op. 3a - Concerto Grosso"))
    }

    // --- WoO (Brahms) ---

    @Test
    fun woo() {
        assertEquals("WoO 1", extractCatalogueString(ALBUM_HUNGARIAN_DANCE))
    }
}

class StripCatalogueFromTitleTest {
    @Test
    fun stripsFromEndWithComma() {
        assertEquals("Goldberg Variations", stripCatalogueFromTitle("Goldberg Variations, BWV 988"))
    }

    @Test
    fun stripsFromStartWithDash() {
        assertEquals(ALBUM_SYMPHONY, stripCatalogueFromTitle("Op. 67 - Symphony No. 5"))
    }

    @Test
    fun stripsFromStartWithDashReversed() {
        assertEquals("Goldberg Variations", stripCatalogueFromTitle("BWV 988 - Goldberg Variations"))
    }

    @Test
    fun stripsFromEndWithSpaceOnly() {
        assertEquals("Piano Sonata", stripCatalogueFromTitle(ALBUM_PIANO_SONATA))
    }

    @Test
    fun stripsFromMiddlePreservingBothSides() {
        assertEquals("Symphony No. 5 in C minor", stripCatalogueFromTitle("Symphony No. 5 Op. 67 in C minor"))
    }

    @Test
    fun noMatchReturnsOriginal() {
        assertEquals(ALBUM_ABBEY_ROAD, stripCatalogueFromTitle(ALBUM_ABBEY_ROAD))
    }

    @Test
    fun catalogueOnlyReturnsEmpty() {
        assertEquals("", stripCatalogueFromTitle("BWV 988"))
    }

    @Test
    fun trimsWhitespace() {
        assertEquals("Symphony", stripCatalogueFromTitle("  Symphony  Op. 67  "))
    }

    @Test
    fun stripsSubPieceWithComma() {
        assertEquals("Sonata in F", stripCatalogueFromTitle("Op. 1, No. 1 - Sonata in F"))
    }

    @Test
    fun stripsLetterSuffix() {
        assertEquals("Concerto Grosso", stripCatalogueFromTitle("Op. 3a - Concerto Grosso"))
    }

    @Test
    fun stripsWoo() {
        assertEquals("Hungarian Dance", stripCatalogueFromTitle(ALBUM_HUNGARIAN_DANCE))
    }
}

private class FakeAlbumRepository : IAlbumRepository {
    var insertCount = 0
    var lastInsertedAlbum: Album? = null

    override suspend fun insert(album: Album) {
        insertCount++
        lastInsertedAlbum = album
    }
}

class FakeArtworkSaver(
    private val artworkPath: String? = null,
) : IArtworkSaver {
    override fun loadAndSaveArtwork(trackId: Long, entityId: Long): String? = artworkPath
}
