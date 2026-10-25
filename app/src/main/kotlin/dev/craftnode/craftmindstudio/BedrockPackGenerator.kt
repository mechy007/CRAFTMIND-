package dev.craftnode.craftmindstudio

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Offline starter generator. This version uses templates, not an LLM. */
object BedrockPackGenerator {
    fun generate(prompt: String, displayName: String): ByteArray {
        val name = displayName.trim().take(60).ifBlank { "Custom Item" }
        val id = safeId(name)
        val behaviorHeader = uuid()
        val behaviorModule = uuid()
        val resourceHeader = uuid()
        val resourceModule = uuid()
        val lower = prompt.lowercase()
        val damage = when {
            "lightning" in lower || "thunder" in lower -> 10
            "hammer" in lower -> 9
            "pickaxe" in lower -> 5
            "strong" in lower || "high damage" in lower -> 8
            else -> 6
        }
        val durability = if ("durability" in lower || "durable" in lower) 1200 else 500
        val behaviorManifest = JSONObject().put("format_version", 2).put("header",
            JSONObject().put("name", "$name Behavior Pack").put("description", "Generated offline by CraftMind Studio")
                .put("uuid", behaviorHeader).put("version", version()).put("min_engine_version", engineVersion())
        ).put("modules", org.json.JSONArray().put(
            JSONObject().put("type", "data").put("uuid", behaviorModule).put("version", version())
        )).put("dependencies", org.json.JSONArray().put(
            JSONObject().put("uuid", resourceHeader).put("version", version())
        )).toString(2)
        val resourceManifest = JSONObject().put("format_version", 2).put("header",
            JSONObject().put("name", "$name Resource Pack").put("description", "Generated offline by CraftMind Studio")
                .put("uuid", resourceHeader).put("version", version()).put("min_engine_version", engineVersion())
        ).put("modules", org.json.JSONArray().put(
            JSONObject().put("type", "resources").put("uuid", resourceModule).put("version", version())
        )).toString(2)
        val item = JSONObject().put("format_version", "1.21.10").put("minecraft:item",
            JSONObject().put("description", JSONObject().put("identifier", "craftmind:$id")
                .put("menu_category", JSONObject().put("category", "equipment")))
                .put("components", JSONObject()
                    .put("minecraft:display_name", JSONObject().put("value", name))
                    .put("minecraft:icon", id)
                    .put("minecraft:max_stack_size", 1)
                    .put("minecraft:hand_equipped", true)
                    .put("minecraft:damage", damage)
                    .put("minecraft:durability", JSONObject().put("max_durability", durability))
                    .put("minecraft:enchantable", JSONObject().put("value", 15))
                )
        ).toString(2)
        val atlas = JSONObject().put("resource_pack_name", "$name Resource Pack")
            .put("texture_name", "atlas.items").put("texture_data",
                JSONObject().put(id, JSONObject().put("textures", "textures/items/$id"))
            ).toString(2)
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            zip.addText("BehaviorPack/manifest.json", behaviorManifest)
            zip.addText("BehaviorPack/items/$id.json", item)
            zip.addText("ResourcePack/manifest.json", resourceManifest)
            zip.addText("ResourcePack/textures/item_texture.json", atlas)
            zip.addBytes("ResourcePack/textures/items/$id.png", createIcon())
        }
        return output.toByteArray()
    }

    private fun safeId(value: String): String =
        value.lowercase().replace(Regex("[^a-z0-9_]"), "_").trim('_').take(32).ifBlank { "custom_item" }
    private fun uuid() = UUID.randomUUID().toString()
    private fun version() = org.json.JSONArray().put(1).put(0).put(0)
    private fun engineVersion() = org.json.JSONArray().put(1).put(20).put(80)

    private fun createIcon(): ByteArray {
        val bitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply { isAntiAlias = false }
        val pixels = listOf(13 to 1, 12 to 2, 11 to 3, 10 to 4, 9 to 5, 8 to 6, 7 to 7, 6 to 8, 5 to 9, 4 to 10, 3 to 11, 2 to 12)
        for ((x, y) in pixels) {
            paint.color = Color.rgb(190, 205, 255)
            canvas.drawRect(x.toFloat(), y.toFloat(), (x + 1).toFloat(), (y + 1).toFloat(), paint)
            if (x > 1 && y < 12) {
                paint.color = Color.rgb(90, 105, 160)
                canvas.drawRect((x + 1).toFloat(), y.toFloat(), (x + 2).toFloat(), (y + 1).toFloat(), paint)
            }
        }
        paint.color = Color.rgb(100, 235, 255)
        canvas.drawRect(7f, 7f, 9f, 9f, paint)
        canvas.drawRect(5f, 9f, 7f, 11f, paint)
        paint.color = Color.rgb(118, 82, 50)
        canvas.drawRect(1f, 12f, 4f, 13f, paint)
        canvas.drawRect(0f, 13f, 2f, 14f, paint)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        bitmap.recycle()
        return stream.toByteArray()
    }

    private fun ZipOutputStream.addText(path: String, value: String) = addBytes(path, value.toByteArray(Charsets.UTF_8))
    private fun ZipOutputStream.addBytes(path: String, bytes: ByteArray) {
        putNextEntry(ZipEntry(path))
        write(bytes)
        closeEntry()
    }
}
