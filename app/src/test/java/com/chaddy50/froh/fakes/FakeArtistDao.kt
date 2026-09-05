package com.chaddy50.froh.fakes

import com.chaddy50.froh.data.dao.ArtistDao
import com.chaddy50.froh.data.entity.Artist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeArtistDao : ArtistDao {
    val artists = mutableMapOf<String, Artist>()
    private val allArtists = MutableStateFlow<List<Artist>>(emptyList())
    var nextInsertId = 1L
    var insertCount = 0

    override suspend fun insert(artist: Artist) {
        insertCount++
        if (artists.containsKey(artist.name)) return
        artists[artist.name] = artist.copy(id = nextInsertId++)
        allArtists.value = artists.values.toList()
    }

    override suspend fun getArtistByName(name: String): Artist? = artists[name]

    override fun getArtistById(id: Int): Flow<Artist?> =
        allArtists.map { list -> list.find { it.id == id.toLong() } }

    override fun getAllArtists(): Flow<List<Artist>> = allArtists

    override suspend fun update(artist: Artist) {
        artists[artist.name] = artist
        allArtists.value = artists.values.toList()
    }

    override suspend fun delete(artist: Artist) {
        artists.remove(artist.name)
        allArtists.value = artists.values.toList()
    }
}
