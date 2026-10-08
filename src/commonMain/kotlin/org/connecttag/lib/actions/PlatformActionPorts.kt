package org.connecttag.lib.actions

public enum class PlatformLinkTarget {
    External,
    InApp,
}

public data class PlatformLinkRequest(
    val url: String,
    val target: PlatformLinkTarget = PlatformLinkTarget.External,
    val title: String? = null,
)

public data class PlatformShareRequest(
    val text: String,
    val title: String? = null,
    val url: String? = null,
)

public sealed interface PlatformActionOutcome {
    public data object Completed : PlatformActionOutcome
    public data object Unsupported : PlatformActionOutcome
    public data object Blocked : PlatformActionOutcome
    public data class Failed(val cause: Throwable? = null) : PlatformActionOutcome
}

/** Portable boundary for opening links. Platform Intent/URI types stay in adapters. */
public fun interface PlatformLinkOpener {
    public fun open(request: PlatformLinkRequest): PlatformActionOutcome
}

/** Portable boundary for invoking the platform share surface. */
public fun interface PlatformShareService {
    public fun share(request: PlatformShareRequest): PlatformActionOutcome
}

public interface PlatformExternalActions : PlatformLinkOpener, PlatformShareService
