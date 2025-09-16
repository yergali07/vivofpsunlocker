package com.dezory.vivofpsunlocker

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

@RequiresApi(Build.VERSION_CODES.N)
class FpsTileService : TileService() {

    private fun isEnabled(): Boolean = FpsManager.isEnabled(this)

    private fun applyEnabledState(enabled: Boolean) {
        if (!FpsManager.canWriteSettings(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = android.net.Uri.parse("package:" + packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivityAndCollapse(intent)
            return
        }
        val ok = FpsManager.setEnabled(this, enabled)
        if (!ok) {
            Toast.makeText(this, "Failed to toggle FPS", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val enabled = isEnabled()
        tile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val newState = !isEnabled()
        applyEnabledState(newState)
        updateTileState()
    }
}
