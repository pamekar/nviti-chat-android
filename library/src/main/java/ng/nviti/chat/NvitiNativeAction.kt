package ng.nviti.chat

import org.json.JSONObject

public enum class NvitiNativeAction(public val wireName: String) {
    NAVIGATE("navigate"), CAMERA("camera"), FILE("file"), LOCATION("location"),
    PUSH_TOKEN("push_token"), CLOSE("close");

    public companion object {
        public fun fromWireName(value: String): NvitiNativeAction? = entries.firstOrNull { it.wireName == value }
    }
}

public fun interface NvitiNativeActionHandler {
    public fun handle(
        action: NvitiNativeAction,
        payload: JSONObject,
        respond: (Result<JSONObject?>) -> Unit,
    )
}

public fun interface NvitiMediaPermissionHandler {
    public fun request(resources: Set<String>, respond: (Boolean) -> Unit)
}
