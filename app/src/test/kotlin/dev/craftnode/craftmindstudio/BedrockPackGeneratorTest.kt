package dev.craftnode.craftmindstudio

import android.graphics.BitmapFactory
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BedrockPackGeneratorTest {
    @Test
    fun generatedMcaddonContainsBehaviorAndResourcePacks() {
        val outerEntries = unzip(BedrockPackGenerator.generate("durable hammer", "Frost Hammer"))
        assertEquals(setOf("Frost Hammer Behavior.mcpack", "Frost Hammer Resources.mcpack"), outerEntries.keys)

        val behavior = unzip(outerEntries.getValue("Frost Hammer Behavior.mcpack"))
        val resources = unzip(outerEntries.getValue("Frost Hammer Resources.mcpack"))
        assertTrue("manifest.json" in behavior)
        assertTrue("items/frost_hammer.json" in behavior)
        assertTrue("manifest.json" in resources)
        assertTrue("textures/item_texture.json" in resources)
        assertTrue("textures/items/frost_hammer.png" in resources)
    }

    @Test
    fun manifestsHaveValidUniqueUuidsAndMatchingDependency() {
        val outer = unzip(BedrockPackGenerator.generate("strong", "Test Blade"))
        val behavior = unzip(outer.getValue("Test Blade Behavior.mcpack"))
        val resources = unzip(outer.getValue("Test Blade Resources.mcpack"))
        val behaviorManifest = JSONObject(behavior.getValue("manifest.json").decodeToString())
        val resourceManifest = JSONObject(resources.getValue("manifest.json").decodeToString())
        val behaviorHeader = behaviorManifest.getJSONObject("header").getString("uuid")
        val resourceHeader = resourceManifest.getJSONObject("header").getString("uuid")

        assertTrue(behaviorHeader.matches(UUID_PATTERN))
        assertTrue(resourceHeader.matches(UUID_PATTERN))
        assertFalse(behaviorHeader == resourceHeader)
        assertEquals(
            resourceHeader,
            behaviorManifest.getJSONArray("dependencies").getJSONObject(0).getString("uuid")
        )
    }

    @Test
    fun itemDefinitionMatchesSanitizedNameAndPromptStats() {
        val outer = unzip(BedrockPackGenerator.generate("lightning sword with durable durability", ""))
        val behavior = unzip(outer.getValue("Custom Item Behavior.mcpack"))
        val item = JSONObject(behavior.getValue("items/custom_item.json").decodeToString())
            .getJSONObject("minecraft:item")
        assertEquals("craftmind:custom_item", item.getJSONObject("description").getString("identifier"))
        val components = item.getJSONObject("components")
        assertEquals(10, components.getInt("minecraft:damage"))
        assertEquals(1200, components.getJSONObject("minecraft:durability").getInt("max_durability"))
    }

    @Test
    fun generatedTextureIsAReadable16PixelPng() {
        val outer = unzip(BedrockPackGenerator.generate("pickaxe", "Miner"))
        val resources = unzip(outer.getValue("Miner Resources.mcpack"))
        val png = resources.getValue("textures/items/miner.png")
        val bitmap = BitmapFactory.decodeByteArray(png, 0, png.size)
        assertNotNull(bitmap)
        val decoded = requireNotNull(bitmap)
        assertEquals(16, decoded.width)
        assertEquals(16, decoded.height)
        decoded.recycle()
    }

    private fun unzip(bytes: ByteArray): Map<String, ByteArray> {
        val entries = linkedMapOf<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (!entry.isDirectory) entries[entry.name] = zip.readBytes()
                zip.closeEntry()
            }
        }
        return entries
    }

    companion object {
        private val UUID_PATTERN = Regex(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
        )
    }
}
