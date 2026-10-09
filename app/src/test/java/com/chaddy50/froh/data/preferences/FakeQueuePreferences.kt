package com.chaddy50.froh.data.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeQueuePreferences(
    initialIsQueueHidden: Boolean = false,
) : IQueuePreferences {

    private val _isQueueHidden = MutableStateFlow(initialIsQueueHidden)

    override val isQueueHidden: Flow<Boolean> = _isQueueHidden

    override suspend fun setQueueHidden(isHidden: Boolean) {
        _isQueueHidden.value = isHidden
    }
}
