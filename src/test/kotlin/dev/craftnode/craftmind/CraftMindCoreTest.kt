package dev.craftnode.craftmind

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CraftMindCoreTest {
    private fun request(message: String, tools: Set<String> = ToolRegistry.readOnlyTools) = BridgeRequest(
        requestId = "test-request-1", message = message, allowedTools = tools,
    )

    @Test fun proposesReadOnlyStatusToolWhenAllowed() {
        val reply = CraftMindCore().handle(request("What is my server status?"))
        assertEquals(ToolRegistry.SERVER_STATUS, reply.proposal?.tool)
        assertEquals(false, reply.proposal?.requiresConfirmation)
    }

    @Test fun refusesToolNotInAllowlist() {
        val reply = CraftMindCore().handle(request("What is my server status?", emptySet()))
        assertNull(reply.proposal)
        assertTrue(reply.text!!.contains("not enabled"))
    }

    @Test fun rejectsUnsupportedProtocol() {
        val reply = CraftMindCore().handle(request("status").copy(protocol = "craftmind.bridge.v99"))
        assertNotNull(reply.text)
        assertTrue(reply.text!!.contains("Unsupported"))
    }

    @Test fun rejectsProviderWriteTool() {
        val provider = MindProvider { r -> MindReply(r.requestId, proposal = ToolProposal(
            requestId = r.requestId, tool = "server.stop", requiresConfirmation = true, reason = "stop",
        )) }
        val reply = CraftMindCore(provider).handle(request("stop server"))
        assertNull(reply.proposal)
        assertTrue(reply.text!!.contains("not allowed"))
    }

    @Test fun rejectsOversizedMessage() {
        val reply = CraftMindCore().handle(request("x".repeat(MAX_MESSAGE_CHARS + 1)))
        assertTrue(reply.text!!.contains("1–4,000"))
    }

    @Test fun providerFailureIsRecoverable() {
        val core = CraftMindCore(MindProvider { throw IllegalStateException("private diagnostic") })
        val reply = core.handle(request("hello"))
        assertTrue(reply.text!!.contains("try again"))
        assertTrue(!reply.text!!.contains("private diagnostic"))
    }
}
