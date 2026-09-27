package com.chaddy50.froh.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chaddy50.froh.data.entity.Playlist
import com.chaddy50.froh.data.entity.PlaylistTrack
import com.chaddy50.froh.data.entity.Track
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    fun getPlaylistById(id: Long): Flow<Playlist?>

    @Query("""
        SELECT tracks.* FROM tracks
        INNER JOIN playlist_tracks ON tracks.id = playlist_tracks.trackId
        WHERE playlist_tracks.playlistId = :playlistId
        ORDER BY playlist_tracks.position ASC
    """)
    fun getTracksForPlaylist(playlistId: Long): Flow<List<Track>>

    @Query("SELECT MAX(position) FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun getMaxPosition(playlistId: Long): Int?

    @Insert
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Update
    suspend fun update(playlist: Playlist)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylistTrack(playlistTrack: PlaylistTrack)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun deletePlaylistTrack(playlistId: Long, trackId: Long)

    @Query("SELECT DISTINCT playlistId FROM playlist_tracks WHERE trackId = :trackId")
    fun getPlaylistIdsContainingTrack(trackId: Long): Flow<List<Long>>

    @Query("""
        SELECT p.id
        FROM playlists p
        WHERE NOT EXISTS (
            SELECT 1
            FROM tracks t
            WHERE t.albumId = :albumId
            AND NOT EXISTS (
                SELECT 1
                FROM playlist_tracks pt
                WHERE pt.playlistId = p.id
                AND pt.trackId = t.id
            )
        )
    """)
    fun getPlaylistIdsContainingAlbum(albumId: Long): Flow<List<Long>>

    @Query("""
        SELECT p.id
        FROM playlists p
        WHERE NOT EXISTS (
            SELECT 1
            FROM tracks t
            WHERE t.albumArtistId = :albumArtistId
            AND NOT EXISTS (
                SELECT 1
                FROM playlist_tracks pt
                WHERE pt.playlistId = p.id
                AND pt.trackId = t.id
            )
        )
    """)
    fun getPlaylistIdsContainingAlbumArtist(albumArtistId: Long): Flow<List<Long>>

    @Query("""
        SELECT p.id
        FROM playlists p
        WHERE NOT EXISTS (
            SELECT 1
            FROM tracks t
            WHERE t.genreId = :genreId
                OR t.parentGenreId = :genreId
            AND NOT EXISTS (
                SELECT 1
                FROM playlist_tracks pt
                WHERE pt.playlistId = p.id
                AND pt.trackId = t.id
            )
        )
    """)
    fun getPlaylistIdsContainingGenre(genreId: Long): Flow<List<Long>>
}
