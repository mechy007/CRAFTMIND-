package dev.craftnode.craftmind

/** Only tools listed here can be proposed by the first, read-only core. */
object ToolRegistry {
    const val SERVER_STATUS = "server.status"
    const val LOGS_EXPLAIN = "server.logs.explain"
    const val WORLD_LIST = "world.list"
    const val WORLD_GET = "world.get"
    const val PLAYERS_LIST = "players.list"

    val readOnlyTools: Set<String> = setOf(
        SERVER_STATUS, LOGS_EXPLAIN, WORLD_LIST, WORLD_GET, PLAYERS_LIST,
    )

    fun validate(request: BridgeRequest): ToolResult? {
        if (request.protocol != BRIDGE_PROTOCOL) return rejected(request, "UNSUPPORTED_PROTOCOL", "Unsupported CraftMind bridge protocol.")
        if (request.requestId.isBlank() || request.requestId.length > 128) return rejected(request, "INVALID_REQUEST_ID", "Request ID is missing or too long.")
        if (request.message.isBlank() || request.message.length > MAX_MESSAGE_CHARS) return rejected(request, "INVALID_MESSAGE", "Message must contain 1–4,000 characters.")
        if (request.allowedTools.any { it !in readOnlyTools }) return rejected(request, "INVALID_ALLOWLIST", "The allowlist contains a tool not enabled in this read-only core.")
        return null
    }

    fun validateProposal(request: BridgeRequest, proposal: ToolProposal): ToolResult? {
        if (proposal.protocol != BRIDGE_PROTOCOL || proposal.requestId != request.requestId)
            return rejected(request, "PROPOSAL_MISMATCH", "The proposed action does not match this request.")
        if (proposal.tool !in readOnlyTools || proposal.tool !in request.allowedTools)
            return rejected(request, "TOOL_NOT_ALLOWED", "That tool is not allowed for this request.")
        if (proposal.arguments.values.any { it.length > MAX_LOG_CHARS })
            return rejected(request, "ARGUMENT_TOO_LARGE", "A tool argument exceeds the allowed size.")
        // Read-only tools must never request state-changing confirmation or be presented as writes.
        if (proposal.requiresConfirmation)
            return rejected(request, "INVALID_READ_ONLY_PROPOSAL", "Read-only proposals cannot request a write confirmation.")
        return null
    }

    private fun rejected(request: BridgeRequest, code: String, message: String) = ToolResult(
        requestId = request.requestId,
        tool = "craftmind.request",
        status = ToolStatus.REJECTED,
        message = message,
        errorCode = code,
    )
}
