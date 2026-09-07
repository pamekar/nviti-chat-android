# Nviti mobile SDK integration guide

Embed the shared Nviti web conversation engine in Android, iOS, Flutter or
React Native. The SDK is a WebView host, not a second conversation backend.

## Packages and examples

- [Android SDK](https://github.com/pamekar/nviti-chat-android) · [Kotlin demo](https://github.com/pamekar/nviti-demo-app-android)
- [Flutter SDK](https://github.com/pamekar/nviti-chat-flutter) · [Flutter demo](https://github.com/pamekar/nviti-demo-app-flutter)
- [React Native SDK](https://github.com/pamekar/nviti-chat-react-native) · [React Native demo](https://github.com/pamekar/nviti-demo-app-react-native)
- [iOS SDK](https://github.com/pamekar/nviti-chat-ios) (Swift Package)

Follow each SDK README for source installation. GitHub source availability is
not an npm, pub.dev or Maven Central release. Use a published Git tag or commit
for reproducible production builds, not a moving branch.

## Secure session flow

1. Authenticate the customer in your own backend.
2. Your backend calls Nviti with its scoped API credential.
3. Return only the short-lived `data.webview_launch_url` to the mobile client.
4. Present the SDK using that URL and the configured, exact tenant origin.
5. Handle session expiry by requesting a new session from the backend.

Example backend request (replace the host and widget key):
```http
POST https://nviti.ng/api/v1/chat/sessions
Authorization: Bearer YOUR_SERVER_ONLY_CREDENTIAL
Content-Type: application/json

{
  "widget_key": "YOUR_WIDGET_KEY",
  "external_user_id": "YOUR_AUTHENTICATED_CUSTOMER_ID",
  "name": "Demo Customer",
  "permissions": ["close"],
  "ttl_minutes": 15
}
```

The API client needs the `chat:sessions` ability. Nviti administrators can
provision it through the platform's API-client tooling. Derive the user ID and
contact attributes from your authenticated server session, never arbitrary
request-body values. A typed phone number is not proof of identity.

The allowed origin is your trusted deployment's scheme, host and port,
for example `https://acme.nvt.ng`, without a path. It must match the launch URL.
Do not derive this security allowlist from an arbitrary deep link.

Treat the launch URL and its token as credentials. Do not log them or place
them in analytics, crash reports, screenshots or push payloads. Do not hardcode
an API credential in the app. Public demo URLs with `?webview=1` do not establish
authenticated account access.

## Presentation, close and lifecycle

Provide a visible native Close control around the SDK and implement the
allowlisted `close` bridge callback. The web close action is a lifecycle
exception to signed-session device permissions; it remains host-allowlisted.

Create the chat view when presenting its screen and release it on dismissal.
Android hosts must destroy the WebView when its screen is destroyed. Flutter
hosts dispose their route/controller; React Native hosts unmount the component.
Use a fresh view for a different user's launch URL and implement an explicit
logout/session-revocation policy. SDK WebViews use persistent browser storage;
remounting alone does not guarantee that cookies or prior anonymous identity
have been cleared. Do not share a chat session between signed-in users.

A floating launcher and invitation banner belong to the host application.
See the demo apps for native implementations and accessible dismissal.

## Features and responsibility

| Capability | Shared engine / host boundary |
| --- | --- |
| Messages, menus, forms, bookings, handoff | Rendered by Nviti when configured for the widget and agent |
| Documents and media | Web content plus platform WebView support; downloads/file selection may need host callbacks |
| Camera, file, location | Explicit native action allowlist, validated payloads, host implementation and OS permission |
| Voice recording | Requires device microphone permission and WebView media permission; test on physical devices |
| Background notifications | PWA Web Push is not native SDK push; host FCM/APNs integration is separate |
| Identity and account authorization | Backend-issued signed session and server-side ownership checks |
| Offline behavior | Do not promise offline transactions or guaranteed message delivery; show retry/reconnect states |

The v1 bridge supports `navigate`, `camera`, `file`, `location`, `push_token`
and `close`. Listing an action does not implement it. The server grants signed
permissions, the SDK independently allowlists them, and the host validates
payloads and obtains OS permission. Start with only `close`.

Do not grant every Android WebView permission resource merely because one OS
permission was approved. Only acknowledge capabilities actually implemented.
Return exactly one success/error result per native request; do not execute
arbitrary JavaScript, class names or URLs from bridge payloads. Handle denied
permissions without pretending the action succeeded.

## Troubleshooting and acceptance

- Blank screen: check HTTPS, exact origin, widget configuration and network access.
- Session rejected: refresh it through the authenticated backend; do not replay a consumed URL.
- External document/link blocked: implement the platform's supported navigation/download callback safely.
- Native action rejected: check server permissions, SDK allowlist and host handler.
- Recording unavailable: check both OS and WebView media permissions.
- Closing fails: implement host close callback and keep a native close button.
- Background messages absent: native apps need FCM/APNs; PWA permission is not sufficient.

Before production, verify open/close, reconnect, token expiry, logout and account
switching, all required forms/media, denied permissions, and Android/iOS physical
devices. Demo builds and emulator evidence are not a substitute for authenticated
customer integration, release signing or native background-push acceptance.
