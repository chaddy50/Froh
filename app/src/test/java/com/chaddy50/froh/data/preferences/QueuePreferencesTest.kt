package com.chaddy50.froh.data.preferences

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class QueuePreferencesTest {

    private val context = ApplicationProvider.getApplicationContext<android.app.Application>()

    @Test
    fun defaultsToQueueNotHidden() = runTest {
        val preferences = QueuePreferences(context)

        assertFalse(preferences.isQueueHidden.first())
    }

    @Test
    fun persistsQueueHiddenAcrossInstances() = runTest {
        QueuePreferences(context).setQueueHidden(true)

        assertEquals(true, QueuePreferences(context).isQueueHidden.first())
    }
}
