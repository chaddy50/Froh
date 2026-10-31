package com.chaddy50.froh.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chaddy50.froh.data.entity.Track
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    // Insert a single track. If a track with the same primary key already exists, replace it.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(track: Track)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultiple(tracks: List<Track>)

    // Update an existing track.
    @Update
    suspend fun update(track: Track)

    // Delete a track.
    @Delete
    suspend fun delete(track: Track)

    @Query("SELECT COUNT(*) FROM tracks")
    fun getNumberOfTracks(): Flow<Int>

    // Get a single track by its ID. Returns a Flow, which will automatically update
    // if the track's data changes.
    @Query("SELECT * FROM tracks WHERE id = :id")
    fun getTrackById(id: Long): Flow<Track?>

    // Get all tracks from the table, ordered by track number.
    // The Flow will emit a new list of tracks whenever the table's content changes.
    @Query("SELECT * FROM tracks ORDER BY title ASC")
    fun getAllTracks(): Flow<List<Track>>

    // Get all tracks belonging to a specific album.
    @Query("SELECT * FROM tracks WHERE albumID = :albumId ORDER BY discNumber, number ASC")
    fun getTracksForAlbum(albumId: Long): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE performanceId = :performanceId ORDER BY discNumber, number ASC")
    fun getTracksForPerformance(performanceId: Long): Flow<List<Track>>

    @Query("SELECT COUNT(*) FROM tracks WHERE albumId = :albumId")
    fun getNumberOfTracksInAlbum(albumId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM tracks WHERE performanceId = :performanceId")
    fun getNumberOfTracksInPerformance(performanceId: Long): Flow<Int>

    @Query("""
        SELECT tracks.* FROM tracks
        INNER JOIN albums on tracks.albumId = albums.id
        WHERE albums.artistId = :albumArtistId
        ORDER BY albums.year, tracks.discNumber, tracks.number ASC
    """)
    fun getTracksForAlbumArtist(albumArtistId: Long): Flow<List<Track>>

    @Query("""
        SELECT tracks.* FROM tracks
        INNER JOIN albums on tracks.albumId = albums.id
        WHERE albums.artistId = :albumArtistId
        AND (tracks.genreId = :genreId OR tracks.parentGenreId = :genreId)
        ORDER BY albums.year, tracks.discNumber, tracks.number ASC
    """)
    fun getTracksForAlbumArtistInGenre(albumArtistId: Long, genreId: Long): Flow<List<Track>>

    @Query("""
        SELECT * FROM tracks
        WHERE albumId = :albumId
        AND (genreId = :genreId OR parentGenreId = :genreId)
        ORDER BY discNumber, number ASC
    """)
    fun getTracksForAlbumInGenre(albumId: Long, genreId: Long): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE genreId = :genreId OR parentGenreId = :genreId")
    fun getTracksForGenre(genreId: Long): Flow<List<Track>>

    @Query("SELECT id FROM tracks")
    suspend fun getAllTrackIds(): List<Long>

    @Query("DELETE FROM tracks WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}