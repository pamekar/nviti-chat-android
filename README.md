# Nviti Chat android SDK

Embed the shared Nviti conversation engine: messages, configured forms, menus,
bookings and human handoff. Apache-2.0 licensed.

## Source installation

Requirements: JDK 17, Android SDK 35, minimum Android API 24, and a current Android
System WebView. Maven Central publication is not yet available.

For a complete runnable integration, keep these sibling checkouts:
```sh
mkdir -p demo_apps sdks
git clone --branch v0.1.0 https://github.com/pamekar/nviti-chat-android.git sdks/nviti-chat-android
git clone https://github.com/pamekar/nviti-demo-app-android.git demo_apps/nviti-demo-app-android
cd demo_apps/nviti-demo-app-android
./gradlew testDebugUnitTest assembleDebug
```

In your own Gradle app, include the SDK's `library` directory as a project
dependency (matching compatible Android/Kotlin plugin versions):
```kotlin
// settings.gradle.kts; adjust the relative path to your checkout
include(":nviti-chat")
project(":nviti-chat").projectDir = file("../nviti-chat-android/library")
// app/build.gradle.kts
dependencies { implementation(project(":nviti-chat")) }
```

## Close-only integration

Inside an Activity, with `launchUrl` issued by your backend:
```kotlin
val chat = NvitiChatView(
    context = this,
    config = NvitiChatConfig(
        launchUrl = Uri.parse(launchUrl),
        allowedOrigin = Uri.parse("https://YOUR_TENANT.nvt.ng"),
        allowedActions = setOf(NvitiNativeAction.CLOSE),
    ),
    actionHandler = NvitiNativeActionHandler { action, _, respond ->
        if (action == NvitiNativeAction.CLOSE) {
            respond(Result.success(JSONObject()))
            finish()
        } else respond(Result.failure(IllegalArgumentException("Unsupported action")))
    },
)
```

Import `android.net.Uri`, `org.json.JSONObject` and `ng.nviti.chat.*`.
Add the view to your layout, provide a native Close button, and destroy it from
the Activity lifecycle. See the [complete demo](https://github.com/pamekar/nviti-demo-app-android).

## Integration guide

Read [secure sessions, lifecycle, native permissions, feature boundaries and troubleshooting](docs/integration.md).
Never embed a server API credential or trust a client-entered phone number as identity.
