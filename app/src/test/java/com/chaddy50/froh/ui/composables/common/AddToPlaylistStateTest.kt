package com.chaddy50.froh.ui.composables.common

import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

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
        state.show("genre1")
        assertEquals("genre1", state.entityToAdd)
    }

    @Test
    fun dismissClearsEntityToAdd() {
        val state = createState()
        state.show("genre1")
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
        state.show("myEntity")
        state.addToPlaylist(42L)
        assertEquals(42L, capturedPlaylistId)
        assertEquals("myEntity", capturedEntity)
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
        state.show("myEntity")
        state.createAndAddToPlaylist("New Playlist")
        assertEquals("New Playlist", capturedName)
        assertEquals("myEntity", capturedEntity)
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
