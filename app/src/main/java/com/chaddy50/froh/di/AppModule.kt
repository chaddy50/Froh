package com.chaddy50.froh.di

import android.content.Context
import com.chaddy50.froh.data.MusicDatabase
import com.chaddy50.froh.data.api.deezer.DeezerClient
import com.chaddy50.froh.data.api.listenBrainz.ListenBrainzClient
import com.chaddy50.froh.data.api.listenBrainz.ListenBrainzPreferences
import com.chaddy50.froh.data.api.listenBrainz.IListenBrainzPreferences
import com.chaddy50.froh.data.api.listenBrainz.ListenBrainzRepository
import com.chaddy50.froh.data.api.listenBrainz.ListenBrainzService
import com.chaddy50.froh.data.preferences.IQueuePreferences
import com.chaddy50.froh.data.preferences.QueuePreferences
import com.chaddy50.froh.data.scrobbling.IScrobbleService
import com.chaddy50.froh.data.scrobbling.ScrobbleManager
import com.chaddy50.froh.data.api.deezer.DeezerRepository
import com.chaddy50.froh.data.api.deezer.DeezerService
import com.chaddy50.froh.data.api.deezer.IDeezerRepository
import com.chaddy50.froh.data.api.openOpus.IOpenOpusRepository
import com.chaddy50.froh.data.api.openOpus.OpenOpusClient
import com.chaddy50.froh.data.api.openOpus.OpenOpusRepository
import com.chaddy50.froh.data.api.openOpus.OpenOpusService
import com.chaddy50.froh.data.dao.AlbumArtistDao
import com.chaddy50.froh.data.dao.AlbumDao
import com.chaddy50.froh.data.dao.ArtistDao
import com.chaddy50.froh.data.dao.ComposerDao
import com.chaddy50.froh.data.dao.GenreDao
import com.chaddy50.froh.data.dao.GenreMappingDao
import com.chaddy50.froh.data.dao.PerformanceDao
import com.chaddy50.froh.data.dao.PlaylistDao
import com.chaddy50.froh.data.dao.TrackDao
import com.chaddy50.froh.data.repository.AlbumArtistRepository
import com.chaddy50.froh.data.repository.AlbumRepository
import com.chaddy50.froh.data.repository.ArtistRepository
import com.chaddy50.froh.data.repository.ComposerRepository
import com.chaddy50.froh.data.repository.GenreMappingRepository
import com.chaddy50.froh.data.repository.GenreRepository
import com.chaddy50.froh.data.repository.PerformanceRepository
import com.chaddy50.froh.data.repository.PlaylistRepository
import com.chaddy50.froh.data.repository.TrackRepository
import com.chaddy50.froh.data.scanner.MusicScanner
import com.chaddy50.froh.data.util.ArtworkDownloader
import com.chaddy50.froh.data.util.IArtworkDownloader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- Database & DAOs ---

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MusicDatabase =
        MusicDatabase.getDatabase(context)

    @Provides fun provideTrackDao(db: MusicDatabase): TrackDao = db.trackDao()
    @Provides fun provideAlbumDao(db: MusicDatabase): AlbumDao = db.albumDao()
    @Provides fun provideArtistDao(db: MusicDatabase): ArtistDao = db.artistDao()
    @Provides fun provideAlbumArtistDao(db: MusicDatabase): AlbumArtistDao = db.albumArtistDao()
    @Provides fun provideGenreDao(db: MusicDatabase): GenreDao = db.genreDao()
    @Provides fun provideGenreMappingDao(db: MusicDatabase): GenreMappingDao = db.genreMappingDao()
    @Provides fun providePerformanceDao(db: MusicDatabase): PerformanceDao = db.performanceDao()
    @Provides fun provideComposerDao(db: MusicDatabase): ComposerDao = db.composerDao()
    @Provides fun providePlaylistDao(db: MusicDatabase): PlaylistDao = db.playlistDao()

    // --- Network ---

    @Provides
    @Singleton
    fun provideDeezerService(): DeezerService = DeezerClient.service

    @Provides
    @Singleton
    fun provideOpenOpusService(): OpenOpusService = OpenOpusClient.service

    @Provides
    @Singleton
    fun provideListenBrainzService(): ListenBrainzService = ListenBrainzClient.service

    // --- Utilities ---

    @Provides
    @Singleton
    fun provideArtworkDownloader(@ApplicationContext context: Context): IArtworkDownloader =
        ArtworkDownloader(context)

    // --- Repositories ---

    @Provides
    @Singleton
    fun provideTrackRepository(trackDao: TrackDao): TrackRepository =
        TrackRepository(trackDao)

    @Provides
    @Singleton
    fun provideAlbumRepository(albumDao: AlbumDao): AlbumRepository =
        AlbumRepository(albumDao)

    @Provides
    @Singleton
    fun provideArtistRepository(artistDao: ArtistDao): ArtistRepository =
        ArtistRepository(artistDao)

    @Provides
    @Singleton
    fun provideGenreRepository(genreDao: GenreDao): GenreRepository =
        GenreRepository(genreDao)

    @Provides
    @Singleton
    fun provideGenreMappingRepository(genreMappingDao: GenreMappingDao): GenreMappingRepository =
        GenreMappingRepository(genreMappingDao)

    @Provides
    @Singleton
    fun provideAlbumArtistRepository(
        albumArtistDao: AlbumArtistDao,
        deezerRepository: IDeezerRepository,
    ): AlbumArtistRepository =
        AlbumArtistRepository(albumArtistDao, deezerRepository)

    @Provides
    @Singleton
    fun providePerformanceRepository(performanceDao: PerformanceDao): PerformanceRepository =
        PerformanceRepository(performanceDao)

    @Provides
    @Singleton
    fun providePlaylistRepository(playlistDao: PlaylistDao): PlaylistRepository =
        PlaylistRepository(playlistDao)

    @Provides
    @Singleton
    fun provideDeezerRepository(
        service: DeezerService,
        artworkDownloader: IArtworkDownloader,
    ): IDeezerRepository =
        DeezerRepository(service, artworkDownloader)

    @Provides
    @Singleton
    fun provideOpenOpusRepository(service: OpenOpusService): IOpenOpusRepository =
        OpenOpusRepository(service)

    @Provides
    @Singleton
    fun provideComposerRepository(
        composerDao: ComposerDao,
        openOpusRepository: IOpenOpusRepository,
        artworkDownloader: IArtworkDownloader,
        albumArtistDao: AlbumArtistDao,
    ): ComposerRepository =
        ComposerRepository(composerDao, openOpusRepository, artworkDownloader, albumArtistDao)

    // --- ListenBrainz ---

    @Provides
    @Singleton
    fun provideListenBrainzPreferences(@ApplicationContext context: Context): IListenBrainzPreferences =
        ListenBrainzPreferences(context)

    @Provides
    @Singleton
    fun provideQueuePreferences(@ApplicationContext context: Context): IQueuePreferences =
        QueuePreferences(context)

    @Provides
    @Singleton
    fun provideListenBrainzRepository(
        service: ListenBrainzService,
        preferences: IListenBrainzPreferences
    ): ListenBrainzRepository =
        ListenBrainzRepository(service, preferences, CoroutineScope(SupervisorJob() + Dispatchers.IO))

    @Provides
    @Singleton
    fun provideScrobbleServices(
        listenBrainzRepository: ListenBrainzRepository
    ): List<IScrobbleService> =
        listOf(listenBrainzRepository)

    @Provides
    @Singleton
    fun provideScrobbleManager(
        services: List<@JvmSuppressWildcards IScrobbleService>
    ): ScrobbleManager =
        ScrobbleManager(services)

    // --- Scanner ---

    @Provides
    @Singleton
    fun provideMusicScanner(
        @ApplicationContext context: Context,
        genreRepository: GenreRepository,
        genreMappingRepository: GenreMappingRepository,
        artistRepository: ArtistRepository,
        albumArtistRepository: AlbumArtistRepository,
        albumRepository: AlbumRepository,
        trackRepository: TrackRepository,
        performanceRepository: PerformanceRepository,
        composerRepository: ComposerRepository,
    ): MusicScanner =
        MusicScanner(context, genreRepository, genreMappingRepository, artistRepository, albumArtistRepository, albumRepository, trackRepository, performanceRepository, composerRepository)
}
