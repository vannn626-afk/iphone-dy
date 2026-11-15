package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.IslandDisplayMode
import com.example.data.IslandPayload
import com.example.manager.IslandStateCoordinator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("LEOVANISLAND", appName)
    }

    @Test
    fun `island state coordinator handles split pill and expand transitions`() {
        IslandStateCoordinator.triggerSimulatedSplitPillMusicAndTimer()
        assertEquals(IslandDisplayMode.SPLIT_PILL, IslandStateCoordinator.displayMode.value)

        IslandStateCoordinator.toggleExpandCollapse()
        assertEquals(IslandDisplayMode.EXPANDED, IslandStateCoordinator.displayMode.value)

        IslandStateCoordinator.triggerSimulatedNotification(
            appName = "WhatsApp",
            packageName = "com.whatsapp",
            sender = "Penguji",
            message = "Pesan uji coba"
        )
        assertTrue(IslandStateCoordinator.primaryPayload.value is IslandPayload.NotificationItem)
    }
}
