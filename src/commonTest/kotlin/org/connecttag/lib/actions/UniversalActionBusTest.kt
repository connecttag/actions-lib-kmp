package org.connecttag.lib.actions

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class UniversalActionBusTest {

    @Test
    fun reportsAndBroadcastsActionBreadcrumb() = runTest {
        val testClock = ClockProvider { 1_700_000_000_000L }
        val bus = UniversalActionBus(clock = testClock)

        var received: ActionBreadcrumb? = null
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            bus.actions.collect { breadcrumb ->
                received = breadcrumb
            }
        }

        bus.reportAction("BUTTON_CLICK", mapOf("target" to "checkout"))

        assertNotNull(received)
        assertEquals("BUTTON_CLICK", received.name)
        assertEquals("checkout", received.details["target"])
        assertEquals(1_700_000_000_000L, received.timestamp)
    }
}
