package com.chaddy50.froh.services

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.annotation.VisibleForTesting
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService.LibraryParams
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.chaddy50.froh.MusicRepositoryProvider
import com.chaddy50.froh.data.entity.Album
import com.chaddy50.froh.data.entity.AlbumArtist
import com.chaddy50.froh.data.entity.Genre
import com.chaddy50.froh.data.entity.Performance
import com.chaddy50.froh.data.entity.Playlist
import com.chaddy50.froh.data.entity.Track
import com.chaddy50.froh.utilities.chooseAlbumArtworkPath
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.guava.future

private const val TAG = "AutoLibraryCallback"

/**
 * Handles Android Auto / Automotive OS browse tree requests.
 *
 * Browse hierarchy:
 *   root
 *   ├── library                                                  → top-level genres
 *   │   └── genre/{genreId}                                     → album artists
 *   │       └── genre/{genreId}/artist/{artistId}              → albums
 *   │           ├── (non-classical) album/{albumId}            → tracks
 *   │           └── (classical)     album/{albumId}            → performances
 *   │               └── perf/{perfId}                          → tracks
 *   └── playlists                                               → user playlists
 *       └── playlist/{playlistId}                              → tracks
 *
 * Classical genres are those with sub-genres. All sub-genres are collapsed into the
 * single top-level entry on the home screen (getAlbumArtistsForGenre already handles this).
 *
 * Track mediaIds use the flat "track/{id}" scheme so onAddMediaItems can re-resolve the
 * URI — MediaItem.localConfiguration is stripped when crossing the IPC boundary from Auto.
 */
@OptIn(UnstableApi::class)
class AutoLibraryCallback(
    private val application: MusicRepositoryProvider,
    private val scope: CoroutineScope
) : MediaLibrarySession.Callback {

    // region Library root

    override fun onGetLibraryRoot(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: LibraryParams?
    ): ListenableFuture<LibraryResult<MediaItem>> {
        Log.d(TAG, "onGetLibraryRoot: pkg=${browser.packageName}")
        val rootItem = MediaItem.Builder()
            .setMediaId(ROOT_ID)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Music")
                    .setIsPlayable(false)
                    .setIsBrowsable(true)
                    .build()
            )
            .build()
        return Futures.immediateFuture(LibraryResult.ofItem(rootItem, null))
    }

    // endregion

    // region Connection & custom commands

    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val result = super.onConnect(session, controller)
        val commands = result.availableSessionCommands.buildUpon()
            .add(SessionCommand(TOGGLE_SHUFFLE_ACTION, Bundle.EMPTY))
            .build()
        return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
            .setAvailableSessionCommands(commands)
            .build()
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        if (customCommand.customAction == TOGGLE_SHUFFLE_ACTION) {
            session.player.shuffleModeEnabled = !session.player.shuffleModeEnabled
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
        return super.onCustomCommand(session, controller, customCommand, args)
    }

    // endregion

    // region Browse children

    override fun onGetChildren(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        Log.d(TAG, "onGetChildren: parentId=$parentId")
        return scope.future {
            val items = getChildrenFor(parentId)
            Log.d(TAG, "onGetChildren: parentId=$parentId → ${items.size} items")
            LibraryResult.ofItemList(ImmutableList.copyOf(items), null)
        }
    }

    @VisibleForTesting
    internal suspend fun getChildrenFor(parentId: String): List<MediaItem> {
        // Home screen: Library and Playlists folders
        if (parentId == ROOT_ID) return getRootItems()

        // Library folder: top-level genres
        if (parentId == LIBRARY_ID) return getTopLevelGenreItems()

        // Playlists folder: all playlists (playable, not browsable)
        if (parentId == PLAYLISTS_ID) return getPlaylistItems()

        val path = parsePath(parentId) ?: return emptyList()

        return when {
            // genre/123 → album artists in this genre (sub-genres are collapsed automatically)
            path.albumArtistId == null ->
                getAlbumArtistItems(path.genreId)

            // genre/123/artist/456 → albums for this artist
            path.albumId == null ->
                getAlbumItems(path.genreId, path.albumArtistId)

            // genre/123/artist/456/album/789/perf/101 → tracks for this performance (classical)
            path.perfId != null ->
                getTrackItemsForPerformance(path.perfId)

            // genre/123/artist/456/album/789 → performances (classical) or tracks (non-classical)
            else ->
                getAlbumChildren(path.genreId, path.albumArtistId, path.albumId)
        }
    }

    private fun getRootItems(): List<MediaItem> = listOf(
        MediaItem.Builder()
            .setMediaId(LIBRARY_ID)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Library")
                    .setIsPlayable(false)
                    .setIsBrowsable(true)
                    .build()
            )
            .build(),
        MediaItem.Builder()
            .setMediaId(PLAYLISTS_ID)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Playlists")
                    .setIsPlayable(false)
                    .setIsBrowsable(true)
                    .build()
            )
            .build()
    )


    private suspend fun getPlaylistItems(): List<MediaItem> =
        application.playlistRepository.getAllPlaylists().first()
            .map { it.toMediaItem() }

    private suspend fun getTopLevelGenreItems(): List<MediaItem> =
        application.genreRepository.getAllTopLevelGenres().first()
            .map { it.toMediaItem() }

    private suspend fun getAlbumArtistItems(genreId: Long): List<MediaItem> =
        application.albumArtistRepository.getAlbumArtistsForGenre(genreId).first()
            .map { it.toMediaItem(genreId) }

    private suspend fun getAlbumItems(genreId: Long, albumArtistId: Long): List<MediaItem> {
        val classical = isClassicalGenre(genreId)
        val albums = if (classical) {
            // Classical: show all albums for the artist sorted by catalogue number
            application.albumRepository.getAlbumsForArtist(albumArtistId, true).first()
        } else {
            // Non-classical: only albums in this specific genre, sorted by year
            application.albumRepository.getAlbumsForArtistInGenre(albumArtistId, genreId, false).first()
        }
        val composerPortraitPath = if (classical) {
            application.albumArtistRepository.getAlbumArtistById(albumArtistId).first()?.portraitPath
        } else {
            null
        }
        return albums.map { album ->
            album.toMediaItem(
                genreId = genreId,
                albumArtistId = albumArtistId,
                isPlayable = !classical,
                isBrowsable = classical,
                isClassical = classical,
                composerPortraitPath = composerPortraitPath,
            )
        }
    }

    private suspend fun getAlbumChildren(
        genreId: Long,
        albumArtistId: Long,
        albumId: Long
    ): List<MediaItem> {
        return if (isClassicalGenre(genreId)) {
            // Classical: album → performances
            application.performanceRepository.getPerformancesForAlbum(albumId).first()
                .map { it.toMediaItem(genreId, albumArtistId, albumId) }
        } else {
            // Non-classical: album → tracks directly
            application.trackRepository.getTracksForAlbum(albumId).first()
                .map { it.toPlayableMediaItem() }
        }
    }

    private suspend fun getTrackItemsForPerformance(perfId: Long): List<MediaItem> =
        application.trackRepository.getTracksForPerformance(perfId).first()
            .map { it.toPlayableMediaItem() }

    /** A genre is "classical" (multi-level) if it has any sub-genres. */
    private suspend fun isClassicalGenre(genreId: Long): Boolean =
        application.genreRepository.getSubGenres(genreId).first().isNotEmpty()

    // endregion

    // region Single item lookup

    override fun onGetItem(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        mediaId: String
    ): ListenableFuture<LibraryResult<MediaItem>> {
        return scope.future {
            val track = resolveTrack(mediaId)
            if (track != null) {
                LibraryResult.ofItem(track.toPlayableMediaItem(), null)
            } else {
                LibraryResult.ofError(SessionError(SessionError.ERROR_UNKNOWN, "Track not found"))
            }
        }
    }

    // endregion

    // region URI resolution (IPC boundary fix)

    /**
     * Re-attaches track URIs before ExoPlayer receives the items.
     * MediaItem.localConfiguration (which holds the URI) is stripped when crossing the IPC
     * boundary from the car head unit to this service, so we rebuild it from the mediaId here.
     *
     * Also expands browsable+playable items (artist, album, performance) into their constituent
     * tracks so Auto can enqueue the full set when the user taps play.
     */
    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>
    ): ListenableFuture<List<MediaItem>> {
        return scope.future {
            mediaItems.flatMap { item -> resolveToTracks(item) }
        }
    }

    /**
     * Resolves a MediaItem to a flat list of playable track items.
     *
     * - Phone app items have plain numeric mediaIds and carry their URI already — return as-is.
     * - Auto browse items use "track/{id}", "genre/.../album/{id}", or "genre/.../perf/{id}"
     *   mediaIds and need URI re-attachment or expansion.
     */
    private suspend fun resolveToTracks(item: MediaItem): List<MediaItem> {
        val mediaId = item.mediaId

        // Phone app items already have the URI attached (localConfiguration != null).
        // Their mediaIds are plain numeric strings — return them unchanged.
        if (item.localConfiguration != null) return listOf(item)

        // Auto: single track — re-attach URI
        if (mediaId.startsWith(TRACK_PREFIX)) {
            val track = resolveTrack(mediaId)
            return if (track != null) listOf(track.toPlayableMediaItem()) else emptyList()
        }

        // Playlist: all tracks in the playlist
        if (mediaId.startsWith(PLAYLIST_PREFIX)) {
            val playlistId = mediaId.removePrefix(PLAYLIST_PREFIX).toLongOrNull()
                ?: return emptyList()
            return application.playlistRepository.getTracksForPlaylist(playlistId).first()
                .map { it.toPlayableMediaItem() }
        }

        val path = parsePath(mediaId) ?: return emptyList()

        return when {
            // Performance: all tracks in this performance
            path.perfId != null ->
                application.trackRepository.getTracksForPerformance(path.perfId).first()
                    .map { it.toPlayableMediaItem() }

            // Album: all tracks in this album
            path.albumId != null ->
                application.trackRepository.getTracksForAlbum(path.albumId).first()
                    .map { it.toPlayableMediaItem() }

            else -> emptyList()
        }
    }

    private suspend fun resolveTrack(mediaId: String): Track? {
        if (!mediaId.startsWith(TRACK_PREFIX)) return null
        val trackId = mediaId.removePrefix(TRACK_PREFIX).toLongOrNull() ?: return null
        return application.trackRepository.getTrackById(trackId).first()
    }

    // endregion

    // region Path parsing

    /**
     * Parses a hierarchical mediaId of the form:
     *   genre/{genreId}[/artist/{artistId}[/album/{albumId}[/perf/{perfId}]]]
     */
    private data class MediaPath(
        val genreId: Long,
        val albumArtistId: Long? = null,
        val albumId: Long? = null,
        val perfId: Long? = null,
    )

    private fun parsePath(mediaId: String): MediaPath? {
        val parts = mediaId.split("/")
        if (parts.size < 2 || parts[0] != "genre") return null
        val genreId = parts[1].toLongOrNull() ?: return null
        val albumArtistId = if (parts.size >= 4 && parts[2] == "artist") parts[3].toLongOrNull() else null
        val albumId = if (parts.size >= 6 && parts[4] == "album") parts[5].toLongOrNull() else null
        val perfId = if (parts.size >= 8 && parts[6] == "perf") parts[7].toLongOrNull() else null
        return MediaPath(genreId, albumArtistId, albumId, perfId)
    }

    // endregion

    // region MediaItem builders

    private fun Playlist.toMediaItem(): MediaItem =
        MediaItem.Builder()
            .setMediaId("$PLAYLIST_PREFIX$id")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(name)
                    .setIsPlayable(true)
                    .setIsBrowsable(false)
                    .build()
            )
            .build()

    private fun Genre.toMediaItem(): MediaItem =
        MediaItem.Builder()
            .setMediaId("genre/$id")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(name)
                    .setIsPlayable(false)
                    .setIsBrowsable(true)
                    .build()
            )
            .build()

    private fun AlbumArtist.toMediaItem(genreId: Long): MediaItem {
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(name)
            .setIsPlayable(false)
            .setIsBrowsable(true)
        portraitPath?.let { metadataBuilder.setArtworkUri(artworkUri(it)) }
        return MediaItem.Builder()
            .setMediaId("genre/$genreId/artist/$id")
            .setMediaMetadata(metadataBuilder.build())
            .build()
    }

    private fun Album.toMediaItem(
        genreId: Long,
        albumArtistId: Long,
        isPlayable: Boolean,
        isBrowsable: Boolean,
        isClassical: Boolean,
        composerPortraitPath: String?,
    ): MediaItem {
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(title)
            .setSubtitle(year)
            .setIsPlayable(isPlayable)
            .setIsBrowsable(isBrowsable)
        chooseAlbumArtworkPath(isClassical, artworkPath, composerPortraitPath)?.let {
            metadataBuilder.setArtworkUri(artworkUri(it))
        }
        return MediaItem.Builder()
            .setMediaId("genre/$genreId/artist/$albumArtistId/album/$id")
            .setMediaMetadata(metadataBuilder.build())
            .build()
    }

    private fun Performance.toMediaItem(
        genreId: Long,
        albumArtistId: Long,
        albumId: Long
    ): MediaItem =
        MediaItem.Builder()
            .setMediaId("genre/$genreId/artist/$albumArtistId/album/$albumId/perf/$id")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(artistName)
                    .setSubtitle(year)
                    .setIsPlayable(true)
                    .setIsBrowsable(false)
                    .build()
            )
            .build()

    private suspend fun Track.toPlayableMediaItem(): MediaItem {
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artistName)
            .setAlbumTitle(albumName)
            .setIsPlayable(true)
            .setIsBrowsable(false)
        // The only genres with a parentGenreId in this app are classical sub-genres.
        val isClassicalTrack = parentGenreId != null
        val composerPortraitPath = if (isClassicalTrack && artworkPath == null) {
            application.albumArtistRepository.getAlbumArtistById(albumArtistId).first()?.portraitPath
        } else {
            null
        }
        chooseAlbumArtworkPath(isClassicalTrack, artworkPath, composerPortraitPath)?.let {
            metadataBuilder.setArtworkUri(artworkUri(it))
        }
        return MediaItem.Builder()
            .setMediaId("$TRACK_PREFIX$id")
            .setUri(uri)
            .setMediaMetadata(metadataBuilder.build())
            .build()
    }

    /**
     * Converts an absolute artwork file path to a content:// URI served by [ArtworkProvider].
     * The path stored in the DB is absolute (e.g. /data/.../files/album_artwork/123.jpg);
     * we strip the filesDir prefix to get the relative path used in the URI.
     */
    private fun artworkUri(absolutePath: String): Uri {
        val relativePath = absolutePath.removePrefix(application.filesDirPath)
        return Uri.parse("content://${ARTWORK_AUTHORITY}$relativePath")
    }

    // endregion

    companion object {
        const val ROOT_ID = "root"
        const val LIBRARY_ID = "library"
        const val PLAYLISTS_ID = "playlists"
        const val TRACK_PREFIX = "track/"
        const val PLAYLIST_PREFIX = "playlist/"
        const val ARTWORK_AUTHORITY = "com.chaddy50.froh.artwork"
        const val TOGGLE_SHUFFLE_ACTION = "com.chaddy50.froh.TOGGLE_SHUFFLE"
    }
}
