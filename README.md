# ConnectTag Actions Library (KMP)

[![JitPack](https://jitpack.io/v/connecttag/actions-lib-kmp.svg)](https://jitpack.io/#connecttag/actions-lib-kmp)
[![Kotlin](https://img.shields.io/badge/kotlin-2.4.20-blue.svg)](https://kotlinlang.org)
[![Platform](https://img.shields.io/badge/platform-android%20%7C%20jvm%20%7C%20ios-lightgrey.svg)](https://kotlinlang.org/docs/multiplatform.html)

A decoupled, multiplatform action bus, canonical action registry, validation suite, and event dispatching engine for Kotlin Multiplatform (Android, JVM, iOS).

---

## Features

- **ActionRegistry**: Canonical action type specifications (`NAVIGATION`, `OPEN_URL`, `SEND_SMS`, `SHARE_MESSAGE`, `SHOW_DIALOG`, etc.) with strict payload kind requirements.
- **ActionRequest & Content**: Type-safe payload polymorphism (`ActionContent.Text`, `Url`, `Route`, `Phone`, `Email`, `Json`, etc.).
- **ActionValidator**: Comprehensive structural and security content validation for deep links, scheme safety, and JSON payloads.
- **UniversalActionBus**: Reactive, lightweight coroutine flow bus for user interaction breadcrumbs, audit logs, and analytics.
- **PlatformActionPorts**: Portable abstraction boundaries for browser launching, sharing, and external app intents.

---

## Installation

Add JitPack repository to your root `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency to your module's `build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.github.connecttag:actions-lib-kmp:1.0.0")
        }
    }
}
```

---

## Usage

### 1. Creating and Validating Actions
```kotlin
val request = ActionRequestFactory.from(
    type = ActionType.OPEN_URL,
    content = "https://example.com/checkout",
    source = ActionSource.UI,
)

val error = ActionValidator.validate(request, hasNavController = true)
if (error != null) {
    println("Invalid action: ${error.toLogMessage()}")
}
```

### 2. Broadcasting via Universal Action Bus
```kotlin
val actionBus = UniversalActionBus()

// Listen to breadcrumbs:
actionBus.actions.collect { breadcrumb ->
    println("Action [${breadcrumb.name}] at ${breadcrumb.timestamp}")
}

// Report action:
actionBus.reportAction("PAYMENT_CLICKED", mapOf("method" to "WALLET"))
```

---

## License

```
Copyright 2026 ConnectTag.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
