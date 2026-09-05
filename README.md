# Nviti Chat Android SDK

Embed the complete Nviti conversation experience in an Android application without rebuilding messages, forms, bookings, media, menus, human handoff, or realtime delivery.

## Requirements

- Android API 24+
- Android System WebView with secure web-message support
- A `webview_launch_url` issued by your backend through Nviti's signed-session API

## Install

Until the first Maven Central release, include this repository as a Gradle composite or source dependency. The package publishes the Maven coordinate `ng.nviti:nviti-chat:0.1.0`.

```kotlin
implementation("ng.nviti:nviti-chat:0.1.0")
```

## Use

```kotlin
val chat = NvitiChatView(
    context = this,
    config = NvitiChatConfig(
        launchUrl = Uri.parse(session.webviewLaunchUrl),
        allowedOrigin = Uri.parse("https://banking-mobile.nvt.ng"),
        allowedActions = setOf(NvitiNativeAction.LOCATION, NvitiNativeAction.CLOSE),
    ),
    actionHandler = NvitiNativeActionHandler { action, payload, respond ->
        respond(Result.success(JSONObject()))
    },
)
```

Generate signed sessions on your backend. Never put an Nviti API credential in an Android application. The SDK rejects cleartext launch URLs, cross-origin navigation, mixed content, unsupported bridge messages and actions that the host has not explicitly allowed.

See the bank demo repository for lifecycle, runtime permission and file-picker integration.
