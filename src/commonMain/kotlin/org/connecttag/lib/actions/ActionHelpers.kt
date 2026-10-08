package org.connecttag.lib.actions

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

public fun interface ClockProvider {
    public fun nowMillis(): Long
}

public object SystemClockProvider : ClockProvider {
    override fun nowMillis(): Long = kotlin.time.Clock.System.now().toEpochMilliseconds()
}

internal object HttpUrlPolicy {
    fun isHttpUrl(value: String?): Boolean {
        val cleaned = value?.trim()?.takeIf { it.isNotEmpty() } ?: return false
        if (cleaned.any { it.code < 0x20 || it.code == 0x7F }) return false
        return cleaned.startsWith("http://", ignoreCase = true) ||
            cleaned.startsWith("https://", ignoreCase = true)
    }
}

internal fun Json.parseJsonElementOrNull(raw: String?): JsonElement? {
    if (raw.isNullOrBlank()) return null
    return runCatching { parseToJsonElement(raw) }.getOrNull()
}

internal fun JsonObject.stringValue(vararg keys: String): String? =
    keys.firstNotNullOfOrNull { key ->
        (this[key] as? JsonPrimitive)?.contentOrNull
    }
