package com.chaddy50.froh.data.repository

import com.chaddy50.froh.data.dao.TrackDao
import com.chaddy50.froh.data.entity.Track
import kotlinx.coroutines.flow.Flow

class TrackRepository(private val trackDao: TrackDao) {

    fun getAllTracks(): Flow<List<Track>> = trackDao.getAllTracks()

    fun getTrackById(id: Long): Flow<Track?> = trackDao.getTrackById(id)

    fun getTracksForGenre(genreId: Long) = trackDao.getTracksForGenre(genreId)

    fun getTracksForAlbum(albumId: Long): Flow<List<Track>> = trackDao.getTracksForAlbum(albumId)

    fun getTracksForPerformance(performanceId: Long): Flow<List<Track>> = trackDao.getTracksForPerformance(performanceId)

    fun getTracksForAlbumArtist(albumArtistId: Long) = trackDao.getTracksForAlbumArtist(albumArtistId)

    fun getTracksForAlbumArtistInGenre(albumArtistId: Long, genreId: Long) = trackDao.getTracksForAlbumArtistInGenre(albumArtistId, genreId)

    fun getTracksForAlbumInGenre(albumId: Long, genreId: Long) = trackDao.getTracksForAlbumInGenre(albumId, genreId)

    fun getNumberOfTracksInAlbum(albumId: Long) = trackDao.getNumberOfTracksInAlbum(albumId)

    fun getNumberOfTracks() = trackDao.getNumberOfTracks()

    fun getNumberOfTracksInPerformance(performanceId: Long) = trackDao.getNumberOfTracksInPerformance(performanceId)

    suspend fun getNumberOfTracksSuspend(): Int {
        return trackDao.getNumberOfTracksSuspend()
    }

    suspend fun insert(track: Track) {
        trackDao.insert(track)
    }

    suspend fun insertMultiple(tracks: List<Track>) {
        trackDao.insertMultiple(tracks)
    }

    suspend fun update(track: Track) {
        trackDao.update(track)
    }

    suspend fun delete(track: Track) {
        trackDao.delete(track)
    }

    suspend fun getAllTrackIds(): List<Long> = trackDao.getAllTrackIds()

    suspend fun deleteByIds(ids: List<Long>) {
        ids.chunked(MAXIMUM_IDS_PER_DELETE).forEach { trackDao.deleteByIds(it) }
    }

    private companion object {
        const val MAXIMUM_IDS_PER_DELETE = 900
    }
}
