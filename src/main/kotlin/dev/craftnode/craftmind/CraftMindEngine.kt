package dev.craftnode.craftmind

/** Provider implementations may suggest a response or a structured tool proposal. */
fun interface MindProvider {
    fun respond(request: BridgeRequest): MindReply
}

/** Deterministic provider for local development and tests; no network or API key required. */
class DemoProvider : MindProvider {
    override fun respond(request: BridgeRequest): MindReply {
        val text = request.message.lowercase()
        val tool = when {
            listOf("server status", "server running", "is my server", "server state").any(text::contains) -> ToolRegistry.SERVER_STATUS
            listOf("list worlds", "show worlds", "worlds available").any(text::contains) -> ToolRegistry.WORLD_LIST
            listOf("list players", "who is online", "players online").any(text::contains) -> ToolRegistry.PLAYERS_LIST
            listOf("explain logs", "explain this log", "log error").any(text::contains) -> ToolRegistry.LOGS_EXPLAIN
            listOf("world settings", "get world", "world difficulty").any(text::contains) -> ToolRegistry.WORLD_GET
            else -> null
        }
        if (tool == null) return MindReply(
            requestId = request.requestId,
            text = "I’m running in demo mode. I can request read-only server status, world list/settings, online players, or help explain a selected log excerpt. No server action was performed.",
        )
        if (tool !in request.allowedTools) return MindReply(
            requestId = request.requestId,
            text = "That information tool is not enabled by CraftNode for this request. No action was performed.",
        )
        val args = if (tool == ToolRegistry.WORLD_GET && !request.context.activeWorldId.isNullOrBlank()) {
            mapOf("worldId" to request.context.activeWorldId)
        } else emptyMap()
        return MindReply(requestId = request.requestId, proposal = ToolProposal(
            requestId = request.requestId,
            tool = tool,
            arguments = args,
            requiresConfirmation = false,
            reason = "Read-only information request; CraftNode must provide the authoritative result.",
        ))
    }
}

class CraftMindCore(private val provider: MindProvider = DemoProvider()) {
    fun handle(request: BridgeRequest): MindReply {
        ToolRegistry.validate(request)?.let { return MindReply(requestId = request.requestId, text = it.message) }
        val reply = try {
            provider.respond(request)
        } catch (_: Exception) {
            return MindReply(requestId = request.requestId, text = "CraftMind could not process this request. Please try again.")
        }
        if (reply.requestId != request.requestId) return MindReply(
            requestId = request.requestId,
            text = "The provider returned a mismatched request. The response was discarded.",
        )
        val proposal = reply.proposal ?: return reply
        val invalid = ToolRegistry.validateProposal(request, proposal)
        return if (invalid != null) MindReply(requestId = request.requestId, text = invalid.message) else reply
    }
}
