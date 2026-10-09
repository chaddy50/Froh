package com.chaddy50.froh.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "queue_prefs")

class QueuePreferences(private val context: Context) : IQueuePreferences {

    private val IS_QUEUE_HIDDEN_KEY = booleanPreferencesKey("is_queue_hidden")

    override val isQueueHidden: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_QUEUE_HIDDEN_KEY] ?: false
    }

    override suspend fun setQueueHidden(isHidden: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_QUEUE_HIDDEN_KEY] = isHidden
        }
    }
}
