package org.connecttag.lib.actions

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Central bus for all user and system actions.
 * Used for tracking navigation breadcrumbs, telemetry, and debugging.
 */
public class UniversalActionBus(
    private val clock: ClockProvider = SystemClockProvider,
) {
    private val _actions = MutableSharedFlow<ActionBreadcrumb>(extraBufferCapacity = 64)
    public val actions: SharedFlow<ActionBreadcrumb> = _actions.asSharedFlow()

    /**
     * Reports an action to the bus.
     */
    public fun reportAction(name: String, details: Map<String, String> = emptyMap()) {
        _actions.tryEmit(
            ActionBreadcrumb(
                name = name,
                details = details,
                timestamp = clock.nowMillis(),
            ),
        )
    }
}

/**
 * Represents a single tracked action in the app.
 */
public data class ActionBreadcrumb(
    val name: String,
    val details: Map<String, String>,
    val timestamp: Long,
)
