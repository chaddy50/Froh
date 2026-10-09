package com.chaddy50.froh.ui.composables.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chaddy50.froh.data.entity.Playlist
import kotlinx.coroutines.flow.Flow

@Stable
class AddToPlaylistState<T : Any> internal constructor(
    internal val getPlaylistMembership: (T) -> Flow<Set<Long>>,
    private val onAdd: (playlistId: Long, entity: T) -> Unit,
    private val onCreateAndAdd: (name: String, entity: T) -> Unit,
) {
    var entityToAdd: T? by mutableStateOf(null)
        private set

    fun show(entity: T) {
        entityToAdd = entity
    }

    fun dismiss() {
        entityToAdd = null
    }

    internal fun addToPlaylist(playlistId: Long) {
        entityToAdd?.let { onAdd(playlistId, it) }
    }

    internal fun createAndAddToPlaylist(name: String) {
        entityToAdd?.let { onCreateAndAdd(name, it) }
    }
}

@Composable
fun <T : Any> rememberAddToPlaylistState(
    getPlaylistMembership: (T) -> Flow<Set<Long>>,
    onAdd: (playlistId: Long, entity: T) -> Unit,
    onCreateAndAdd: (name: String, entity: T) -> Unit,
): AddToPlaylistState<T> {
    return remember {
        AddToPlaylistState(
            getPlaylistMembership = getPlaylistMembership,
            onAdd = onAdd,
            onCreateAndAdd = onCreateAndAdd,
        )
    }
}

@Composable
fun <T : Any> AddToPlaylistHandler(
    state: AddToPlaylistState<T>,
    allPlaylists: List<Playlist>,
) {
    val entity = state.entityToAdd ?: return

    val playlistsThatEntityIsAlreadyIn by remember(entity) {
        state.getPlaylistMembership(entity)
    }.collectAsStateWithLifecycle(emptySet())

    AddToPlaylistSheet(
        allPlaylists = allPlaylists,
        playlistsThatEntityIsAlreadyIn = playlistsThatEntityIsAlreadyIn,
        onAddToPlaylist = { playlistId -> state.addToPlaylist(playlistId) },
        onCreateAndAdd = { name -> state.createAndAddToPlaylist(name) },
        onDismiss = { state.dismiss() },
    )
}
