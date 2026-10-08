package org.connecttag.lib.actions

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

public fun interface ActionRouteStringPolicy {
    public fun isSupported(routeString: String): Boolean
}

public object ActionValidator {
    private val json = Json { ignoreUnknownKeys = true }
    private val allowAnyRouteString = ActionRouteStringPolicy { true }

    public fun validate(
        request: ActionRequest,
        hasNavController: Boolean,
        routeStringPolicy: ActionRouteStringPolicy = allowAnyRouteString,
    ): ActionFailureReason? {
        return validate(
            type = request.type,
            payload = request.payload,
            hasNavController = hasNavController,
            routeStringPolicy = routeStringPolicy,
        )
    }

    public fun validate(
        type: ActionType?,
        payload: ActionContent,
        hasNavController: Boolean,
        routeStringPolicy: ActionRouteStringPolicy = allowAnyRouteString,
    ): ActionFailureReason? {
        val spec = ActionRegistry.specFor(type)
        val trimmedContent = payload.rawValue?.trim()

        if (!spec.supported) {
            return ActionFailureReason.UnsupportedActionType(type)
        }

        if (spec.requiresNavController && !hasNavController) {
            return ActionFailureReason.MissingNavigationController
        }

        if (spec.requiresContent && trimmedContent.isNullOrBlank()) {
            return ActionFailureReason.MissingContent
        }

        if (payload !is ActionContent.None && payload.kind != spec.contentKind) {
            return ActionFailureReason.ContentTypeMismatch(
                expected = spec.contentKind,
                actual = payload.kind,
            )
        }

        if (!trimmedContent.isNullOrBlank() && !isValidContent(spec.contentKind, trimmedContent, spec.type, routeStringPolicy)) {
            return ActionFailureReason.InvalidContent
        }

        return null
    }

    private fun isValidContent(
        contentKind: ActionContentKind,
        content: String,
        actionType: ActionType,
        routeStringPolicy: ActionRouteStringPolicy,
    ): Boolean {
        return when (contentKind) {
            ActionContentKind.NONE -> true
            ActionContentKind.TEXT -> content.isNotBlank()
            ActionContentKind.JSON -> isValidJsonContent(content, actionType)
            ActionContentKind.URL -> isAllowedUrl(content, actionType)
            ActionContentKind.ROUTE -> routeStringPolicy.isSupported(content)
            ActionContentKind.PHONE -> if (actionType == ActionType.SEND_SMS) {
                isPlausibleSmsContent(content)
            } else {
                isPlausiblePhoneContent(content)
            }
            ActionContentKind.EMAIL -> content.contains("@") && !content.contains(" ")
            ActionContentKind.PACKAGE_NAME -> content.matches(PACKAGE_NAME_PATTERN)
            ActionContentKind.URI -> content.startsWith("/") || schemeOf(content) != null
            ActionContentKind.MAP_QUERY -> content.isNotBlank()
        }
    }

    private fun isAllowedUrl(content: String, actionType: ActionType): Boolean {
        val scheme = schemeOf(content)?.lowercase() ?: return false
        return if (ActionRegistry.isHttpOnlyUrlAction(actionType)) {
            scheme in setOf("http", "https")
        } else {
            scheme in setOf("http", "https", "geo", "market")
        }
    }

    private fun schemeOf(content: String): String? {
        val separatorIndex = content.indexOf(':')
        if (separatorIndex <= 0) return null

        val scheme = content.substring(0, separatorIndex)
        if (!scheme.first().isLetter()) return null
        return scheme.takeIf { token ->
            token.all { char -> char.isLetterOrDigit() || char == '+' || char == '-' || char == '.' }
        }
    }

    private fun isValidJsonContent(content: String, actionType: ActionType): Boolean {
        val element = json.parseJsonElementOrNull(content) ?: return false

        return when (actionType) {
            ActionType.SHOW_DIALOG -> isValidDialogContent(element)
            else -> element is JsonObject
        }
    }

    private fun isValidDialogContent(element: JsonElement): Boolean {
        val obj = element as? JsonObject ?: return false
        if (obj.isEmpty()) return false

        val type = obj.stringValue("type")?.uppercase()
        if (type != null && type !in DIALOG_TYPES) return false

        val buttonKeys = listOf("positiveButton", "negativeButton", "neutralButton")
        if (buttonKeys.any { key -> obj[key] != null && !isValidDialogButton(obj[key]) }) {
            return false
        }

        val hasVisibleContent = obj.hasNonBlankString("title") ||
            obj.hasNonBlankString("message") ||
            obj.hasNonBlankString("imageUrl")
        val hasActionButton = buttonKeys.any { key -> obj[key] != null }

        return hasVisibleContent || hasActionButton
    }

    private fun isValidDialogButton(element: JsonElement?): Boolean {
        val obj = element as? JsonObject ?: return false
        if (!obj.hasNonBlankString("text")) return false
        val command = obj.stringValue("actionType") ?: return true
        return ActionRegistry.parseCanonicalActionType(command) != null
    }

    private fun JsonObject.hasNonBlankString(key: String): Boolean {
        return !stringValue(key).isNullOrBlank()
    }

    private fun isPlausiblePhoneContent(content: String): Boolean {
        return content.all { char ->
            char.isDigit() || char in setOf('+', '*', '#', ',', ';', ' ', '-', '(', ')', '?', '=', '&')
        }
    }

    private fun isPlausibleSmsContent(content: String): Boolean {
        val phonePart = content.substringBefore("?")
        return phonePart.isNotBlank() &&
            isPlausiblePhoneContent(phonePart) &&
            !content.contains('\n')
    }

    private val PACKAGE_NAME_PATTERN = Regex("^[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+$")
    private val DIALOG_TYPES = setOf("INFO", "WARNING", "ERROR", "SUCCESS", "IMAGE")
}
