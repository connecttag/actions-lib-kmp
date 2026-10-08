package org.connecttag.lib.actions

public object ActionRegistry {
    private val runtimeInfoActionTypes = setOf(
        ActionType.NAVIGATION,
        ActionType.OPEN_URL,
        ActionType.OPEN_URL_IN_APP,
        ActionType.OPEN_WEBVIEW,
        ActionType.OPEN_YOUTUBE,
        ActionType.CELL_PHONE,
        ActionType.SEND_EMAIL,
        ActionType.SHARE_MESSAGE,
        ActionType.COPY_TO_CLIPBOARD,
        ActionType.OPEN_MAP,
    )

    private val experienceCampaignActionTypes = setOf(
        ActionType.NAVIGATION,
        ActionType.OPEN_URL,
        ActionType.OPEN_URL_IN_APP,
        ActionType.OPEN_WEBVIEW,
        ActionType.OPEN_YOUTUBE,
        ActionType.OPEN_PLAY_STORE,
        ActionType.OPEN_APP_STORE,
        ActionType.OPEN_MAP,
        ActionType.CELL_PHONE,
        ActionType.SEND_EMAIL,
        ActionType.SHARE_MESSAGE,
    )

    public fun parseCanonicalActionType(value: String?): ActionType? {
        return ActionType.entries.firstOrNull { actionType -> actionType.name == value }
    }

    public fun isRuntimeInfoActionType(type: ActionType?): Boolean =
        type != null && type in runtimeInfoActionTypes

    public fun isExperienceCampaignActionType(type: ActionType?): Boolean =
        type != null && type in experienceCampaignActionTypes

    public fun specFor(type: ActionType?): ActionTypeSpec = when (type) {
        null -> spec(ActionType.NONE, true, ActionContentKind.TEXT, supported = false)
        ActionType.NONE -> spec(
            type = type,
            requiresContent = false,
            contentKind = ActionContentKind.NONE,
            supported = false,
            userFeedbackOnFailure = false,
        )
        ActionType.NAVIGATION -> spec(type, true, ActionContentKind.ROUTE, requiresNavController = true)
        ActionType.SHOW_IMAGE_VIEW -> spec(type, true, ActionContentKind.TEXT)
        ActionType.OPEN_URL -> spec(type, true, ActionContentKind.URL)
        ActionType.OPEN_URL_IN_APP -> spec(type, true, ActionContentKind.URL)
        ActionType.OPEN_WEBVIEW -> spec(type, true, ActionContentKind.URL, requiresNavController = true)
        ActionType.OPEN_FILE -> spec(type, true, ActionContentKind.URI)
        ActionType.OPEN_ACTIVITY -> spec(type, true, ActionContentKind.TEXT, supported = false)
        ActionType.OPEN_DIALOG -> spec(type, true, ActionContentKind.JSON, supported = false)
        ActionType.OPEN_BOTTOM_SHEET -> spec(type, true, ActionContentKind.JSON, supported = false)
        ActionType.OPEN_EXTERNAL_APP -> spec(type, true, ActionContentKind.PACKAGE_NAME)
        ActionType.OPEN_DEEPLINK -> spec(type, true, ActionContentKind.URI)
        ActionType.OPEN_APP -> spec(type, false, ActionContentKind.NONE)
        ActionType.CELL_PHONE -> spec(type, true, ActionContentKind.PHONE)
        ActionType.SEND_EMAIL -> spec(type, true, ActionContentKind.EMAIL)
        ActionType.SEND_SMS -> spec(type, true, ActionContentKind.PHONE)
        ActionType.SEND_WHATSAPP -> spec(type, true, ActionContentKind.PHONE)
        ActionType.SEND_WHATSAPP_GROUP -> spec(type, true, ActionContentKind.URL)
        ActionType.OPEN_WHATSAPP_CHANNEL -> spec(type, true, ActionContentKind.URL)
        ActionType.SEND_MESSENGER -> spec(type, true, ActionContentKind.TEXT)
        ActionType.SEND_TELEGRAM -> spec(type, true, ActionContentKind.TEXT)
        ActionType.OPEN_SETTINGS -> spec(type, false, ActionContentKind.NONE)
        ActionType.OPEN_PLAY_STORE -> spec(type, false, ActionContentKind.PACKAGE_NAME)
        ActionType.OPEN_APP_STORE -> spec(type, false, ActionContentKind.PACKAGE_NAME)
        ActionType.RATE_APP -> spec(type, false, ActionContentKind.NONE)
        ActionType.SHARE_APP -> spec(type, false, ActionContentKind.TEXT)
        ActionType.SHARE_MESSAGE -> spec(type, true, ActionContentKind.TEXT)
        ActionType.SHARE_PRODUCT_CARD -> spec(type, true, ActionContentKind.TEXT)
        ActionType.SHOW_TOAST -> spec(type, true, ActionContentKind.TEXT)
        ActionType.OPEN_POPUP_MENU -> spec(type, true, ActionContentKind.TEXT, supported = false)
        ActionType.COPY_TO_CLIPBOARD -> spec(type, true, ActionContentKind.TEXT)
        ActionType.DOWNLOAD_FILE -> spec(type, true, ActionContentKind.URL)
        ActionType.OPEN_NOTIFICATION -> spec(type, false, ActionContentKind.NONE)
        ActionType.PERMISSION_REQUEST -> spec(type, true, ActionContentKind.TEXT)
        ActionType.CUSTOM_ACTION -> spec(type, true, ActionContentKind.TEXT, supported = false)
        ActionType.OPEN_MAP -> spec(type, true, ActionContentKind.MAP_QUERY)
        ActionType.OPEN_FACEBOOK -> spec(type, true, ActionContentKind.URL)
        ActionType.OPEN_X -> spec(type, true, ActionContentKind.URL)
        ActionType.OPEN_YOUTUBE -> spec(type, true, ActionContentKind.URL)
        ActionType.UPDATE_APP -> spec(type, false, ActionContentKind.NONE)
        ActionType.CLEAR_CACHE -> spec(type, false, ActionContentKind.NONE)
        ActionType.REQUEST_REVIEW -> spec(type, false, ActionContentKind.NONE)
        ActionType.TOGGLE_THEME -> spec(type, false, ActionContentKind.NONE)
        ActionType.SHOW_DIALOG -> spec(type, true, ActionContentKind.JSON)
        ActionType.SHOW_SNACKBAR -> spec(type, true, ActionContentKind.TEXT)
        ActionType.CLOSE_APP -> spec(type, false, ActionContentKind.NONE)
    }

    public fun isHttpOnlyUrlAction(type: ActionType?): Boolean {
        return type == ActionType.OPEN_URL_IN_APP || type == ActionType.OPEN_WEBVIEW
    }

    private fun spec(
        type: ActionType,
        requiresContent: Boolean,
        contentKind: ActionContentKind,
        supported: Boolean = true,
        requiresNavController: Boolean = false,
        userFeedbackOnFailure: Boolean = true,
    ): ActionTypeSpec = ActionTypeSpec(
        type = type,
        requiresContent = requiresContent,
        contentKind = contentKind,
        supported = supported,
        requiresNavController = requiresNavController,
        userFeedbackOnFailure = userFeedbackOnFailure,
    )
}
