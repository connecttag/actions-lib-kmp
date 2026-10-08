package org.connecttag.lib.actions

public object ActionRequestFactory {
    public fun from(
        type: ActionType?,
        content: String?,
        source: ActionSource = ActionSource.UNKNOWN,
        routeStringNormalizer: (String?) -> String? = { null },
    ): ActionRequest {
        val resolvedType = type ?: ActionType.NONE
        val normalizedContent = normalizeContent(
            type = resolvedType,
            content = content,
            routeStringNormalizer = routeStringNormalizer,
        )
        return ActionRequest(
            type = resolvedType,
            payload = payloadFor(ActionRegistry.specFor(resolvedType).contentKind, normalizedContent),
            source = source,
        )
    }

    public fun from(
        type: String?,
        content: String?,
        source: ActionSource = ActionSource.UNKNOWN,
        routeStringNormalizer: (String?) -> String? = { null },
    ): ActionRequest? {
        val resolvedType = ActionRegistry.parseCanonicalActionType(type) ?: return null
        return from(resolvedType, content, source, routeStringNormalizer)
    }

    public fun fromParts(
        type: String?,
        content: String?,
        target: String? = null,
        fallbackType: ActionType? = null,
        source: ActionSource = ActionSource.UNKNOWN,
        routeStringNormalizer: (String?) -> String? = { null },
    ): ActionRequest? {
        val resolvedContent = target.cleanContent() ?: content.cleanContent()
        val resolvedType = ActionRegistry.parseCanonicalActionType(type)
            ?: fallbackType
            ?: resolvedContent?.takeIf { HttpUrlPolicy.isHttpUrl(it) }?.let { ActionType.OPEN_URL }
            ?: return null
        return from(resolvedType, resolvedContent, source, routeStringNormalizer)
    }

    public fun normalizeContent(
        type: ActionType,
        content: String?,
        routeStringNormalizer: (String?) -> String? = { null },
    ): String? {
        return when (type) {
            ActionType.NAVIGATION -> routeStringNormalizer(content) ?: content?.trim()
            else -> content?.trim()
        }
    }

    public fun payloadFor(
        kind: ActionContentKind,
        content: String?,
    ): ActionContent {
        val value = content?.trim()?.takeIf(String::isNotEmpty) ?: return ActionContent.None
        return when (kind) {
            ActionContentKind.NONE -> ActionContent.None
            ActionContentKind.TEXT -> ActionContent.Text(value)
            ActionContentKind.URL -> ActionContent.Url(value)
            ActionContentKind.ROUTE -> ActionContent.Route(value)
            ActionContentKind.PHONE -> ActionContent.Phone(value)
            ActionContentKind.EMAIL -> ActionContent.Email(value)
            ActionContentKind.PACKAGE_NAME -> ActionContent.PackageName(value)
            ActionContentKind.URI -> ActionContent.Uri(value)
            ActionContentKind.MAP_QUERY -> ActionContent.MapQuery(value)
            ActionContentKind.JSON -> ActionContent.Json(value)
        }
    }

    private fun String?.cleanContent(): String? {
        return this?.trim()?.takeIf { it.isNotEmpty() }
    }
}
