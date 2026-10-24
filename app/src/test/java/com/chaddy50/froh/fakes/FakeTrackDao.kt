package com.chaddy50.froh.fakes

import com.chaddy50.froh.data.dao.TrackDao
import com.chaddy50.froh.data.entity.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeTrackDao(
    private val tracksFlow: MutableStateFlow<List<Track>> = MutableStateFlow(emptyList()),
) : TrackDao {
    val tracks = mutableListOf<Track>()

    override fun getTracksForGenre(genreId: Long): Flow<List<Track>> =
        tracksFlow.map { list -> list.filter { it.genreId == genreId || it.parentGenreId == genreId } }

    override fun getTracksForAlbum(albumId: Long): Flow<List<Track>> =
        tracksFlow.map { list -> list.filter { it.albumId == albumId } }

    override fun getTracksForPerformance(performanceId: Long): Flow<List<Track>> =
        tracksFlow.map { list -> list.filter { it.performanceId == performanceId } }

    override fun getTracksForAlbumArtist(albumArtistId: Long): Flow<List<Track>> =
        tracksFlow.map { list -> list.filter { it.albumArtistId == albumArtistId } }

    override fun getTracksForAlbumArtistInGenre(albumArtistId: Long, genreId: Long): Flow<List<Track>> =
        tracksFlow.map { list -> list.filter { it.albumArtistId == albumArtistId && (it.genreId == genreId || it.parentGenreId == genreId) } }

    override fun getTracksForAlbumInGenre(albumId: Long, genreId: Long): Flow<List<Track>> =
        tracksFlow.map { list -> list.filter { it.albumId == albumId && (it.genreId == genreId || it.parentGenreId == genreId) } }

    override fun getNumberOfTracksInAlbum(albumId: Long): Flow<Int> =
        tracksFlow.map { list -> list.count { it.albumId == albumId } }

    override fun getNumberOfTracksInPerformance(performanceId: Long): Flow<Int> =
        tracksFlow.map { list -> list.count { it.performanceId == performanceId } }

    override fun getAllTracks(): Flow<List<Track>> = tracksFlow

    override fun getTrackById(id: Long): Flow<Track?> =
        tracksFlow.map { list -> list.find { it.id == id } }

    override suspend fun insert(track: Track) { tracks.add(track) }
    override suspend fun insertMultiple(tracks: List<Track>) { this.tracks.addAll(tracks) }
    override suspend fun update(track: Track) = Unit
    override suspend fun delete(track: Track) = Unit
    override fun getNumberOfTracks(): Flow<Int> = tracksFlow.map { it.size }
    override suspend fun getAllTrackIds(): List<Long> = tracksFlow.value.map { it.id }
    override suspend fun deleteByIds(ids: List<Long>) = Unit
}
