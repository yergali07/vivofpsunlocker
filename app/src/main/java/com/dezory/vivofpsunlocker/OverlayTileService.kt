package com.dezory.vivofpsunlocker

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class OverlayTileService : TileService() {
    private fun isEnabled(): Boolean = OverlayManager.isEnabled(this)

    private fun updateTileState() {
        val tile = qsTile ?: return
        tile.state = if (isEnabled()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val newState = !isEnabled()
        if (newState && !OverlayManager.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:" + packageName))
            startActivityAndCollapse(intent)
            Toast.makeText(this, "Grant overlay permission and toggle again", Toast.LENGTH_SHORT).show()
            return
        }
        val ok = OverlayManager.setEnabled(this, newState)
        if (!ok) Toast.makeText(this, "Failed to toggle overlay", Toast.LENGTH_SHORT).show()
        updateTileState()
    }
}

