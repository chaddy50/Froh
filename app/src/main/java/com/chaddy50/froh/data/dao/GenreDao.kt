package com.chaddy50.froh.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chaddy50.froh.data.entity.Genre
import kotlinx.coroutines.flow.Flow

@Dao
interface GenreDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(genre: Genre): Long

    @Update
    suspend fun update(genre: Genre)

    @Delete
    suspend fun delete(genre: Genre)

    @Query("SELECT * FROM genres WHERE id = :id")
    fun getGenreById(id: Long): Flow<Genre?>

    @Query("SELECT * FROM genres WHERE name = :name LIMIT 1")
    suspend fun getGenreByName(name: String): Genre?

    @Query("SELECT * FROM genres ORDER BY name ASC")
    fun getAllGenres(): Flow<List<Genre>>

    @Query("SELECT name FROM genres WHERE id = :genreId")
    suspend fun getGenreName(genreId: Long): String?

    @Query("""
        SELECT * FROM genres
        WHERE parentGenreId IS NULL AND EXISTS (
            SELECT 1 FROM tracks
            WHERE tracks.genreId = genres.id
            UNION
            SELECT 1 FROM tracks
            INNER JOIN genres AS sub_genres ON tracks.genreId = sub_genres.id
            WHERE sub_genres.parentGenreId = genres.id
        )
        ORDER BY name ASC
    """)
    fun getAllTopLevelGenres(): Flow<List<Genre>>

    @Query("SELECT * FROM genres WHERE parentGenreId = :parentGenreId ORDER BY name ASC")
    fun getSubGenres(parentGenreId: Long): Flow<List<Genre>>

    @Query("""
        SELECT DISTINCT genres.* FROM genres
        INNER JOIN tracks ON genres.id = tracks.genreId
        INNER JOIN albums ON tracks.albumId = albums.id
        WHERE albums.artistId = :albumArtistId AND genres.parentGenreId = :parentGenreId
        ORDER BY genres.name ASC
    """)
    fun getSubGenresForAlbumArtist(parentGenreId: Long, albumArtistId: Long): Flow<List<Genre>>

    @Query("SELECT parentGenreId FROM genres WHERE id = :genreId")
    suspend fun getParentGenreId(genreId: Long): Long?
}
