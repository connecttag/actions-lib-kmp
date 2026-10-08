package org.connecttag.lib.actions

import kotlinx.serialization.Serializable

@Serializable
public enum class ActionType {
    NONE,
    NAVIGATION,
    SHOW_IMAGE_VIEW,
    OPEN_URL,
    OPEN_URL_IN_APP,
    OPEN_WEBVIEW,
    OPEN_FILE,
    OPEN_ACTIVITY,
    OPEN_DIALOG,
    OPEN_BOTTOM_SHEET,
    OPEN_EXTERNAL_APP,
    OPEN_DEEPLINK,
    OPEN_APP,
    CELL_PHONE,
    SEND_EMAIL,
    SEND_SMS,
    SEND_WHATSAPP,
    SEND_WHATSAPP_GROUP,
    OPEN_WHATSAPP_CHANNEL,
    SEND_MESSENGER,
    SEND_TELEGRAM,
    OPEN_SETTINGS,
    OPEN_PLAY_STORE,
    OPEN_APP_STORE,
    RATE_APP,
    SHARE_APP,
    SHARE_MESSAGE,
    SHARE_PRODUCT_CARD,
    SHOW_TOAST,
    OPEN_POPUP_MENU,
    COPY_TO_CLIPBOARD,
    DOWNLOAD_FILE,
    OPEN_NOTIFICATION,
    PERMISSION_REQUEST,
    CUSTOM_ACTION,
    OPEN_MAP,
    OPEN_FACEBOOK,
    OPEN_X,
    OPEN_YOUTUBE,
    UPDATE_APP,
    CLEAR_CACHE,
    REQUEST_REVIEW,
    TOGGLE_THEME,
    SHOW_DIALOG,
    SHOW_SNACKBAR,
    CLOSE_APP,
}

@Serializable
public enum class ActionSource {
    UI,
    SERVER_CONTENT,
    LOCAL_CACHE,
    SHORTCUT,
    UNKNOWN,
}

@Serializable
public enum class ActionContentKind {
    NONE,
    TEXT,
    URL,
    ROUTE,
    PHONE,
    EMAIL,
    PACKAGE_NAME,
    URI,
    MAP_QUERY,
    JSON,
}

public data class ActionTypeSpec(
    val type: ActionType,
    val requiresContent: Boolean,
    val contentKind: ActionContentKind,
    val supported: Boolean = true,
    val requiresNavController: Boolean = false,
    val userFeedbackOnFailure: Boolean = true,
)

@Serializable
public sealed interface ActionContent {
    val kind: ActionContentKind
    val rawValue: String?

    @Serializable
    public data object None : ActionContent {
        override val kind: ActionContentKind = ActionContentKind.NONE
        override val rawValue: String? = null
    }

    @Serializable
    public sealed interface Value : ActionContent {
        val value: String
        override val rawValue: String
            get() = value
    }

    @Serializable
    public data class Text(override val value: String) : Value {
        override val kind: ActionContentKind = ActionContentKind.TEXT
    }

    @Serializable
    public data class Url(override val value: String) : Value {
        override val kind: ActionContentKind = ActionContentKind.URL
    }

    @Serializable
    public data class Route(override val value: String) : Value {
        override val kind: ActionContentKind = ActionContentKind.ROUTE
    }

    @Serializable
    public data class Phone(override val value: String) : Value {
        override val kind: ActionContentKind = ActionContentKind.PHONE
    }

    @Serializable
    public data class Email(override val value: String) : Value {
        override val kind: ActionContentKind = ActionContentKind.EMAIL
    }

    @Serializable
    public data class PackageName(override val value: String) : Value {
        override val kind: ActionContentKind = ActionContentKind.PACKAGE_NAME
    }

    @Serializable
    public data class Uri(override val value: String) : Value {
        override val kind: ActionContentKind = ActionContentKind.URI
    }

    @Serializable
    public data class MapQuery(override val value: String) : Value {
        override val kind: ActionContentKind = ActionContentKind.MAP_QUERY
    }

    @Serializable
    public data class Json(override val value: String) : Value {
        override val kind: ActionContentKind = ActionContentKind.JSON
    }
}

@Serializable
public data class ActionRequest(
    val type: ActionType,
    val payload: ActionContent,
    val source: ActionSource = ActionSource.UNKNOWN,
)

public fun interface ActionRequestHandler {
    public fun dispatch(request: ActionRequest)

    public operator fun invoke(request: ActionRequest): Unit = dispatch(request)

    public operator fun invoke(
        type: ActionType,
        content: String? = null,
        source: ActionSource = ActionSource.UI,
    ): Unit = dispatch(ActionRequestFactory.from(type, content, source))
}

public sealed interface ActionResult {
    val handled: Boolean

    public data object Handled : ActionResult {
        override val handled: Boolean = true
    }

    public data object Ignored : ActionResult {
        override val handled: Boolean = false
    }

    public data class Failed(val reason: ActionFailureReason) : ActionResult {
        override val handled: Boolean = false
    }
}

public object ActionResults {
    val handled: ActionResult = ActionResult.Handled
    val ignored: ActionResult = ActionResult.Ignored

    public fun failed(reason: ActionFailureReason): ActionResult =
        ActionResult.Failed(reason)

    public fun failureReasonOrNull(result: ActionResult): ActionFailureReason? =
        (result as? ActionResult.Failed)?.reason
}

public sealed interface ActionFailureReason {
    public data object MissingContent : ActionFailureReason
    public data object InvalidContent : ActionFailureReason
    public data class ContentTypeMismatch(
        val expected: ActionContentKind,
        val actual: ActionContentKind,
    ) : ActionFailureReason
    public data object MissingNavigationController : ActionFailureReason
    public data object NoAppCanHandleIntent : ActionFailureReason
    public data object BlockedByPolicy : ActionFailureReason
    public data class UnsupportedActionType(val actionType: ActionType?) : ActionFailureReason
    public data class ExceptionThrown(val message: String?) : ActionFailureReason

    public fun toLogMessage(): String {
        return when (this) {
            MissingContent -> "Missing content"
            InvalidContent -> "Invalid content"
            is ContentTypeMismatch -> "Action content type mismatch: expected $expected, received $actual"
            MissingNavigationController -> "Missing navigation controller"
            NoAppCanHandleIntent -> "No app can handle intent"
            BlockedByPolicy -> "Blocked by policy"
            is UnsupportedActionType -> "Unsupported action type: $actionType"
            is ExceptionThrown -> "Exception: $message"
        }
    }
}

public enum class ActionFailureKind {
    MISSING_CONTENT,
    INVALID_CONTENT,
    PAYLOAD_TYPE_MISMATCH,
    MISSING_NAVIGATION_CONTROLLER,
    NO_APP_CAN_HANDLE_INTENT,
    BLOCKED_BY_POLICY,
    UNSUPPORTED_ACTION_TYPE,
    EXCEPTION_THROWN,
}

public object ActionFailureReasons {
    val missingContent: ActionFailureReason = ActionFailureReason.MissingContent
    val invalidContent: ActionFailureReason = ActionFailureReason.InvalidContent
    val missingNavigationController: ActionFailureReason = ActionFailureReason.MissingNavigationController
    val noAppCanHandleIntent: ActionFailureReason = ActionFailureReason.NoAppCanHandleIntent
    val blockedByPolicy: ActionFailureReason = ActionFailureReason.BlockedByPolicy

    public fun unsupportedActionType(actionType: ActionType?): ActionFailureReason =
        ActionFailureReason.UnsupportedActionType(actionType)

    public fun exceptionThrown(message: String?): ActionFailureReason =
        ActionFailureReason.ExceptionThrown(message)

    public fun kindOf(reason: ActionFailureReason): ActionFailureKind =
        when (reason) {
            ActionFailureReason.MissingContent -> ActionFailureKind.MISSING_CONTENT
            ActionFailureReason.InvalidContent -> ActionFailureKind.INVALID_CONTENT
            is ActionFailureReason.ContentTypeMismatch -> ActionFailureKind.PAYLOAD_TYPE_MISMATCH
            ActionFailureReason.MissingNavigationController -> ActionFailureKind.MISSING_NAVIGATION_CONTROLLER
            ActionFailureReason.NoAppCanHandleIntent -> ActionFailureKind.NO_APP_CAN_HANDLE_INTENT
            ActionFailureReason.BlockedByPolicy -> ActionFailureKind.BLOCKED_BY_POLICY
            is ActionFailureReason.UnsupportedActionType -> ActionFailureKind.UNSUPPORTED_ACTION_TYPE
            is ActionFailureReason.ExceptionThrown -> ActionFailureKind.EXCEPTION_THROWN
        }
}
