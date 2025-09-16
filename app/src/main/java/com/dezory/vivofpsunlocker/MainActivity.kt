package com.dezory.vivofpsunlocker

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.LazyColumn

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        setContent { FpsUnlockerApp() }
    }
}

@Composable
private fun FpsUnlockerApp() {
    val context = LocalContext.current

    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (isSystemInDarkTheme()) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    }

    MaterialTheme(colorScheme = colorScheme) {
        var enabled by remember { mutableStateOf(false) }
        var selectedTab by remember { mutableStateOf(0) } // 0 = Home, 1 = About
        var showWriteSettingsDialog by remember { mutableStateOf(false) }
        var currentFps by remember { mutableStateOf<Float?>(null) }
        var overlayEnabled by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            val current = Settings.System.getString(context.contentResolver, "gamecube_frame_interpolation_for_sr")
            enabled = current == "1:1::72:144"
            Log.d("FPSUnlocker", "Initial: 'gamecube_frame_interpolation_for_sr' = '$current'")
            overlayEnabled = context.getSharedPreferences("FpsPrefs", android.content.Context.MODE_PRIVATE)
                .getBoolean("overlay_enabled", false)
        }

        // Listen for state changes from QS tile or other sources
        DisposableEffect(Unit) {
            val receiver = object : android.content.BroadcastReceiver() {
                override fun onReceive(c: android.content.Context?, intent: Intent?) {
                    if (intent?.action == FpsManager.ACTION_FPS_CHANGED) {
                        val newEnabled = intent.getBooleanExtra(FpsManager.EXTRA_ENABLED, FpsManager.isEnabled(context))
                        enabled = newEnabled
                    } else if (intent?.action == OverlayManager.ACTION_OVERLAY_CHANGED) {
                        val newOverlay = intent.getBooleanExtra(OverlayManager.EXTRA_ENABLED, OverlayManager.isEnabled(context))
                        overlayEnabled = newOverlay
                    }
                }
            }
            val filter = android.content.IntentFilter().apply {
                addAction(FpsManager.ACTION_FPS_CHANGED)
                addAction(OverlayManager.ACTION_OVERLAY_CHANGED)
            }
            context.registerReceiver(receiver, filter)
            onDispose { context.unregisterReceiver(receiver) }
        }

        // Simple in-app FPS monitor (for this Activity only)
        LaunchedEffect(Unit) {
            while (true) {
                val windowNs = 1_000_000_000L // 1s window
                val start = System.nanoTime()
                var frames = 0
                while (System.nanoTime() - start < windowNs) {
                    withFrameNanos { _ -> frames++ }
                }
                val elapsed = (System.nanoTime() - start).coerceAtLeast(1L)
                currentFps = frames.toFloat() / (elapsed / 1_000_000_000f)
            }
        }

        Scaffold(
            contentWindowInsets = WindowInsets(0.dp),
            topBar = {},
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Outlined.Info, contentDescription = "About") },
                        label = { Text("About") }
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (selectedTab == 0) Arrangement.Center else Arrangement.Top
            ) {
                if (selectedTab == 0) {
                    // Home tab content
                    Card() {
                        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Force 144 Hz globally", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("OFF", color = if (!enabled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline)
                                Spacer(Modifier.width(12.dp))
                                Switch(
                                    checked = enabled,
                                    onCheckedChange = { isChecked ->
                                        val canWrite = FpsManager.canWriteSettings(context)
                                        if (!canWrite) {
                                            showWriteSettingsDialog = true
                                            return@Switch
                                        }
                                        val ok = FpsManager.setEnabled(context, isChecked)
                                        if (ok) enabled = isChecked else Toast.makeText(context, "Failed to toggle FPS", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                Spacer(Modifier.width(12.dp))
                                Text("ON", color = if (enabled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline)
                            }
                            Spacer(Modifier.height(12.dp))
                            val fpsText = currentFps?.let { String.format("Current FPS: %.0f", it) }
                                ?: "Current FPS: —"
                            Text(text = fpsText, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(Modifier.height(16.dp))
                            Divider()
                            Spacer(Modifier.height(16.dp))

                            // Overlay toggle row
                            Text("FPS Overlay (over other apps)", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("OFF", color = if (!overlayEnabled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline)
                                Spacer(Modifier.width(12.dp))
                                Switch(
                                    checked = overlayEnabled,
                                    onCheckedChange = { isChecked ->
                                        if (isChecked && !OverlayManager.canDrawOverlays(context)) {
                                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.packageName))
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            context.startActivity(intent)
                                            Toast.makeText(context, "Grant 'Display over other apps' and toggle again", Toast.LENGTH_LONG).show()
                                            return@Switch
                                        }
                                        val ok = OverlayManager.setEnabled(context, isChecked)
                                        if (ok) overlayEnabled = isChecked else Toast.makeText(context, "Failed to toggle overlay", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                Spacer(Modifier.width(12.dp))
                                Text("ON", color = if (overlayEnabled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                    if (showWriteSettingsDialog) {
                        AlertDialog(
                            onDismissRequest = { showWriteSettingsDialog = false },
                            title = { Text("Permission required") },
                            text = { Text("Allow this app to modify system settings to enable 144 Hz.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showWriteSettingsDialog = false
                                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                                        data = Uri.parse("package:" + context.packageName)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                }) { Text("Open Settings") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showWriteSettingsDialog = false }) { Text("Cancel") }
                            }
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
                    ) {
                        item {
                            Text(
                                text = "Vivo/iQOO 144 FPS Unlocker",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Unlock 144 Hz globally on supported OriginOS devices by writing the vendor system key.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(Modifier.height(24.dp))
                            Text("How it works", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            Text("• ON writes: gamecube_frame_interpolation_for_sr = 1:1::72:144", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("• OFF writes: 0:-1:0:0:0", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("• Foreground service periodically re-applies the value.", color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(Modifier.height(16.dp))
                            Text("Quick Settings tiles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            Text("• 144 FPS tile — toggles the global 144 Hz unlock.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("• FPS Overlay tile — toggles the floating FPS bubble.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Tip: Pull down QS → Edit → add both tiles.", color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(Modifier.height(16.dp))
                            Text("FPS overlay", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            Text("• Shows current FPS above all apps (draggable).", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("• Requires ‘Display over other apps’ permission on first use.", color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(Modifier.height(16.dp))
                            Text("Notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            Text("• Needs ‘Modify system settings’ to toggle 144 Hz.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("• Actual 144 Hz depends on device/app performance.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(24.dp))
                        }
                        item {
                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                OutlinedButton(onClick = {
                                    val uri = android.net.Uri.parse("https://github.com/yergali07/vivofpsunlocker")
                                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                }) { Text("Open GitHub") }
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
