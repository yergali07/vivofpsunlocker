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

        LaunchedEffect(Unit) {
            val current = Settings.System.getString(context.contentResolver, "gamecube_frame_interpolation_for_sr")
            enabled = current == "1:1::72:144"
            Log.d("FPSUnlocker", "Initial: 'gamecube_frame_interpolation_for_sr' = '$current'")
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
                                        val canWrite = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            Settings.System.canWrite(context)
                                        } else true
                                        if (!canWrite) {
                                            showWriteSettingsDialog = true
                                            return@Switch
                                        }
                                        try {
                                            val value = if (isChecked) "1:1::72:144" else "0:-1:0:0:0"
                                            Settings.System.putString(context.contentResolver, "gamecube_frame_interpolation_for_sr", value)
                                            enabled = isChecked
                                            Toast.makeText(context, if (isChecked) "144 FPS on!" else "144 FPS off", Toast.LENGTH_SHORT).show()
                                            // Persist flag for the service refresher
                                            context.getSharedPreferences("FpsPrefs", android.content.Context.MODE_PRIVATE)
                                                .edit().putBoolean("fps_enabled", isChecked).apply()
                                            if (isChecked) context.startService(Intent(context, FpsService::class.java))
                                            else context.stopService(Intent(context, FpsService::class.java))
                                        } catch (e: SecurityException) {
                                            Log.e("FPSUnlocker", "SecurityException: ${e.message}", e)
                                            showWriteSettingsDialog = true
                                        } catch (e: Exception) {
                                            Log.e("FPSUnlocker", "err: ${e.message}", e)
                                            Toast.makeText(context, "err: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                )
                                Spacer(Modifier.width(12.dp))
                                Text("ON", color = if (enabled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline)
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(text = if (enabled) "Current FPS: 144" else "Current FPS: 0-120", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            val aboutText = """
                                |Vivo/iQOO 144 FPS Unlocker
                                |
                                |Unlocks 144 Hz globally on certain Vivo/iQOO (OriginOS) devices by writing the vendor system key.
                                |
                                |How it works
                                |- ON writes: gamecube_frame_interpolation_for_sr = 1:1::72:144
                                |- OFF writes: 0:-1:0:0:0
                                |- A foreground service re-applies the value periodically.
                                |
                                |Notes
                                |- Requires 'Modify system settings' permission to succeed.
                                |- Actual 144 Hz depends on device/app performance.
                                |
                                |Source & releases: https://github.com/yergali07/vivofpsunlocker
                                |""".trimMargin()
                            Text(
                                text = aboutText,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
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
