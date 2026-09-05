package ng.nviti.chat

import android.net.Uri

public data class NvitiChatConfig(
    val launchUrl: Uri,
    val allowedOrigin: Uri,
    val allowedActions: Set<NvitiNativeAction> = emptySet(),
    val userAgentSuffix: String = "NvitiChatAndroid/0.1.0",
) {
    init {
        require(launchUrl.scheme == "https") { "launchUrl must use HTTPS" }
        require(allowedOrigin.scheme == "https") { "allowedOrigin must use HTTPS" }
        require(launchUrl.sameOriginAs(allowedOrigin)) { "launchUrl must use allowedOrigin" }
        require(launchUrl.fragment?.contains("nviti_session=") != false || launchUrl.query?.contains("webview=1") == true) {
            "Use the webview_launch_url returned by the Nviti session API"
        }
    }
}

internal fun Uri.sameOriginAs(other: Uri): Boolean =
    scheme.equals(other.scheme, ignoreCase = true) &&
        host.equals(other.host, ignoreCase = true) &&
        effectivePort() == other.effectivePort()

private fun Uri.effectivePort(): Int = if (port != -1) port else if (scheme == "https") 443 else 80
