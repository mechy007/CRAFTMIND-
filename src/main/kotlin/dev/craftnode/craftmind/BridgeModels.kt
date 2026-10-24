package dev.craftnode.craftmind

const val BRIDGE_PROTOCOL = "craftmind.bridge.v1"
const val MAX_MESSAGE_CHARS = 4_000
const val MAX_ARGUMENT_CHARS = 8_000

enum class ServerState { RUNNING, STOPPED, STARTING, STOPPING, UNKNOWN }
data class ServerContext(val activeWorldId: String? = null, val engineId: String? = null, val serverState: ServerState = ServerState.UNKNOWN)
data class BridgeRequest(val protocol: String = BRIDGE_PROTOCOL, val requestId: String, val message: String, val context: ServerContext = ServerContext(), val allowedTools: Set<String> = emptySet(), val locale: String = "en-IN")
data class ToolProposal(val protocol: String = BRIDGE_PROTOCOL, val requestId: String, val tool: String, val arguments: Map<String, String> = emptyMap(), val requiresConfirmation: Boolean = false, val reason: String)
data class MindReply(val requestId: String, val text: String? = null, val proposal: ToolProposal? = null)
enum class ToolStatus { SUCCESS, REJECTED, NEEDS_CONFIRMATION, UNSUPPORTED, FAILED, TIMEOUT }
data class ToolResult(val protocol: String = BRIDGE_PROTOCOL, val requestId: String, val tool: String, val status: ToolStatus, val message: String, val verified: Boolean = false, val data: Map<String, String> = emptyMap(), val errorCode: String? = null)
