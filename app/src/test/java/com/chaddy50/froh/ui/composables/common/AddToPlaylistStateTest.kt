package com.chaddy50.froh.ui.composables.common

import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private const val GENRE_ENTITY = "genre1"
private const val ENTITY_NAME = "myEntity"

class AddToPlaylistStateTest {

    private fun createState(
        onAdd: (Long, String) -> Unit = { _, _ -> },
        onCreateAndAdd: (String, String) -> Unit = { _, _ -> },
    ) = AddToPlaylistState<String>(
        getPlaylistMembership = { flowOf(emptySet()) },
        onAdd = onAdd,
        onCreateAndAdd = onCreateAndAdd,
    )

    @Test
    fun entityToAddIsInitiallyNull() {
        val state = createState()
        assertNull(state.entityToAdd)
    }

    @Test
    fun showSetsEntityToAdd() {
        val state = createState()
        state.show(GENRE_ENTITY)
        assertEquals(GENRE_ENTITY, state.entityToAdd)
    }

    @Test
    fun dismissClearsEntityToAdd() {
        val state = createState()
        state.show(GENRE_ENTITY)
        state.dismiss()
        assertNull(state.entityToAdd)
    }

    @Test
    fun showOverwritesPreviousEntity() {
        val state = createState()
        state.show("first")
        state.show("second")
        assertEquals("second", state.entityToAdd)
    }

    @Test
    fun addToPlaylistDelegatesToCallback() {
        var capturedPlaylistId: Long? = null
        var capturedEntity: String? = null
        val state = createState(
            onAdd = { playlistId, entity ->
                capturedPlaylistId = playlistId
                capturedEntity = entity
            },
        )
        state.show(ENTITY_NAME)
        state.addToPlaylist(42L)
        assertEquals(42L, capturedPlaylistId)
        assertEquals(ENTITY_NAME, capturedEntity)
    }

    @Test
    fun createAndAddToPlaylistDelegatesToCallback() {
        var capturedName: String? = null
        var capturedEntity: String? = null
        val state = createState(
            onCreateAndAdd = { name, entity ->
                capturedName = name
                capturedEntity = entity
            },
        )
        state.show(ENTITY_NAME)
        state.createAndAddToPlaylist("New Playlist")
        assertEquals("New Playlist", capturedName)
        assertEquals(ENTITY_NAME, capturedEntity)
    }

    @Test
    fun addToPlaylistDoesNothingWhenEntityIsNull() {
        var called = false
        val state = createState(onAdd = { _, _ -> called = true })
        state.addToPlaylist(1L)
        assertEquals(false, called)
    }

    @Test
    fun createAndAddDoesNothingWhenEntityIsNull() {
        var called = false
        val state = createState(onCreateAndAdd = { _, _ -> called = true })
        state.createAndAddToPlaylist("name")
        assertEquals(false, called)
    }
}
