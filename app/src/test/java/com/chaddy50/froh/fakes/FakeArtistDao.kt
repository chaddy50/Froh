package com.chaddy50.froh.fakes

import com.chaddy50.froh.data.dao.ArtistDao
import com.chaddy50.froh.data.entity.Artist

class FakeArtistDao : ArtistDao {
    val artists = mutableMapOf<String, Artist>()
    var nextInsertId = 1L
    var insertCount = 0

    override suspend fun insert(artist: Artist) {
        insertCount++
        if (artists.containsKey(artist.name)) return
        artists[artist.name] = artist.copy(id = nextInsertId++)
    }

    override suspend fun getArtistByName(name: String): Artist? = artists[name]

    override suspend fun update(artist: Artist) {
        artists[artist.name] = artist
    }

    override suspend fun delete(artist: Artist) {
        artists.remove(artist.name)
    }
}
