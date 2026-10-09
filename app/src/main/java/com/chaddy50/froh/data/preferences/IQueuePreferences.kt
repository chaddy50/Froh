package com.chaddy50.froh.data.preferences

import kotlinx.coroutines.flow.Flow

interface IQueuePreferences {
    val isQueueHidden: Flow<Boolean>

    suspend fun setQueueHidden(isHidden: Boolean)
}
