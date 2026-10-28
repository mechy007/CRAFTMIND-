package dev.craftnode.craftmindstudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF10131B)
private val Panel = Color(0xFF1A2030)
private val Violet = Color(0xFF9B8CFF)
private val Mint = Color(0xFF69E0BB)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Violet, secondary = Mint, background = Ink, surface = Panel,
                onBackground = Color(0xFFF2F3FA), onSurface = Color(0xFFF2F3FA)
            )) { StudioScreen() }
        }
    }
}

@Composable
private fun StudioScreen() {
    var prompt by remember { mutableStateOf("Create a lightning sword with high damage and strong durability") }
    var packName by remember { mutableStateOf("Lightning Blade") }
    var status by remember { mutableStateOf("Ready to build your first add-on.") }
    var pendingBytes by remember { mutableStateOf<ByteArray?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val documentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        val bytes = pendingBytes
        if (uri == null || bytes == null) {
            status = "Export cancelled."
        } else {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                    ?: error("Could not open the selected destination.")
            }.onSuccess {
                status = "Add-on exported. Open the .mcaddon file with Minecraft to import it."
            }.onFailure {
                status = "Export failed: ${it.message ?: "unknown error"}"
            }
        }
    }

    Surface(Modifier.fillMaxSize(), color = Ink) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(48.dp).background(Violet.copy(alpha = 0.18f), RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AutoAwesome, null, tint = Violet, modifier = Modifier.size(28.dp))
                }
                Column {
                    Text("CRAFTMIND", fontSize = 12.sp, color = Mint, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("Studio", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(Modifier.weight(1f))
                Surface(color = Mint.copy(alpha = 0.12f), shape = RoundedCornerShape(50)) {
                    Text("BETA 0.1", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = Mint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Your idea. Your add-on.", fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Text("Describe a Bedrock item and generate an importable starter pack. Works offline; no API key required.", color = Color(0xFFB4B9CB), lineHeight = 21.sp)
            }
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.AutoAwesome, null, tint = Violet)
                        Text("Describe your creation", fontWeight = FontWeight.Bold)
                    }
                    OutlinedTextField(packName, { packName = it }, Modifier.fillMaxWidth(), label = { Text("Add-on name") }, singleLine = true)
                    OutlinedTextField(prompt, { prompt = it }, Modifier.fillMaxWidth(), label = { Text("What should it do?") }, minLines = 4, placeholder = { Text("Example: a frost hammer that deals extra damage") })
                    Text("Quick ideas", color = Color(0xFFB4B9CB), fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SuggestionChip(onClick = { packName = "Frost Hammer"; prompt = "Create a frost hammer with extra damage and high durability" }, label = { Text("Frost hammer") })
                        SuggestionChip(onClick = { packName = "Speed Pickaxe"; prompt = "Create a fast mining pickaxe with high durability" }, label = { Text("Speed pickaxe") })
                    }
                    Button(onClick = {
                        if (prompt.isBlank() || packName.isBlank()) status = "Enter a name and describe your add-on first."
                        else {
                            pendingBytes = BedrockPackGenerator.generate(prompt, packName)
                            status = "Pack generated locally. Choose where to save it."
                            documentLauncher.launch(packName.trim().replace(Regex("[^A-Za-z0-9_-]"), "_") + ".mcaddon")
                        }
                    }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(15.dp)) {
                        Icon(Icons.Default.Download, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Generate & export .mcaddon", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Included in this first build", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    FeatureRow(Icons.Default.Code, "Structured behavior-pack item definition", "Generates a custom item JSON file.")
                    FeatureRow(Icons.Default.FolderZip, "Resource pack and texture", "Includes a generated 16×16 icon and atlas.")
                    FeatureRow(Icons.Default.Security, "Offline-first and no API secrets", "This version uses a deterministic starter generator, not a remote AI model.")
                }
            }
            Surface(color = Violet.copy(alpha = 0.12f), shape = RoundedCornerShape(14.dp)) {
                Text(status, Modifier.fillMaxWidth().padding(14.dp), color = Color(0xFFE1DEFF), fontSize = 13.sp)
            }
            Text("Next: richer item templates, pack validation, script-based behavior, and optional free-tier AI providers.", color = Color(0xFF8D94A9), fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, title: String, subtitle: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = Mint, modifier = Modifier.size(22.dp))
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, color = Color(0xFFB4B9CB), fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
}
