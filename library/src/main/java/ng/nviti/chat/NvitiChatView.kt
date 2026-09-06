package ng.nviti.chat

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import org.json.JSONObject

public class NvitiChatView @SuppressLint("SetJavaScriptEnabled") constructor(
    context: Context,
    public val config: NvitiChatConfig,
    private val actionHandler: NvitiNativeActionHandler,
    private val mediaPermissionHandler: NvitiMediaPermissionHandler? = null,
    private val onExternalNavigation: ((Uri) -> Unit)? = null,
) : WebView(context) {
    private val exactOrigin = "${config.allowedOrigin.scheme}://${config.allowedOrigin.authority}"

    init {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = false
        settings.allowContentAccess = false
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        settings.userAgentString = "${settings.userAgentString} ${config.userAgentSuffix}"
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_ENABLE)) {
            WebViewCompat.startSafeBrowsing(context) {}
        }
        installBridge()
        installNavigationGuard()
        installPermissionGuard()
        setDownloadListener { url, _, _, _, _ ->
            val uri = Uri.parse(url)
            if (uri.scheme == "https") onExternalNavigation?.invoke(uri)
        }
        loadUrl(config.launchUrl.toString())
    }

    private fun installBridge() {
        require(WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            "This Android System WebView does not support secure web messages"
        }
        WebViewCompat.addWebMessageListener(this, "NvitiAndroid", setOf(exactOrigin)) {
                _, message, sourceOrigin, isMainFrame, _ ->
            if (isMainFrame && Uri.parse(sourceOrigin.toString()).sameOriginAs(config.allowedOrigin)) {
                message.data?.let(::handleNativeMessage)
            }
        }
    }

    private fun installNavigationGuard() {
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                if (request.url.sameOriginAs(config.allowedOrigin)) return false
                if (request.isForMainFrame && request.url.scheme == "https") onExternalNavigation?.invoke(request.url)
                return true
            }
        }
    }

    private fun installPermissionGuard() {
        webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                if (!Uri.parse(request.origin.toString()).sameOriginAs(config.allowedOrigin)) {
                    request.deny()
                    return
                }
                val requested = request.resources.toSet()
                mediaPermissionHandler?.request(requested) { granted ->
                    post { if (granted) request.grant(request.resources) else request.deny() }
                } ?: request.deny()
            }
        }
    }

    private fun handleNativeMessage(raw: String) {
        val request = runCatching { JSONObject(raw) }.getOrNull() ?: return
        if (request.optInt("version") != 1 || request.optString("type") != "nviti.native.request") return
        val id = request.optString("request_id")
        val action = NvitiNativeAction.fromWireName(request.optString("action"))
        if (id.isBlank() || action == null || action !in config.allowedActions) {
            if (id.isNotBlank()) respond(id, Result.failure(IllegalStateException("This native action is not allowed by the host app.")))
            return
        }
        post {
            actionHandler.handle(action, request.optJSONObject("payload") ?: JSONObject()) { result ->
                respond(id, result)
            }
        }
    }

    private fun respond(id: String, result: Result<JSONObject?>) {
        val response = JSONObject().put("version", 1).put("type", "nviti.native.response")
            .put("request_id", id).put("ok", result.isSuccess)
        result.onSuccess { response.put("result", it ?: JSONObject.NULL) }
            .onFailure { response.put("error", it.message ?: "Native action failed.") }
        post { evaluateJavascript("window.postMessage(${JSONObject.quote(response.toString())}, window.location.origin)", null) }
    }

    public fun reloadConversation(): Unit = reload()

    public fun dispose() {
        stopLoading()
        loadUrl("about:blank")
        clearHistory()
        removeAllViews()
        destroy()
    }
}
