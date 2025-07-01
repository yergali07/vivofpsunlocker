package com.dolbaeb1488company.fpsunlocker

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.core.net.toUri
import androidx.core.graphics.toColorInt
import androidx.core.content.edit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fpsSwitch = findViewById<SwitchMaterial>(R.id.fps_switch)
        val valueText = findViewById<TextView>(R.id.value_text)
        val onLabel = findViewById<TextView>(R.id.on_label)
        val offLabel = findViewById<TextView>(R.id.off_label)
        val prefs = getSharedPreferences("FpsPrefs", MODE_PRIVATE)

        @SuppressLint("SetTextI18n")
        fun updateUi(isChecked: Boolean) {
            valueText.text = "fps: ${if (isChecked) "144" else "0-120"}"
            onLabel.setTextColor(if (isChecked) "#34D399".toColorInt() else "#6B7280".toColorInt())
            offLabel.setTextColor(if (isChecked) "#6B7280".toColorInt() else "#34D399".toColorInt())
        }

        fun setSystemSetting(isChecked: Boolean) {
            try {
                Settings.System.putString(contentResolver, "gamecube_frame_interpolation_for_sr", if (isChecked) "1:1::72:144" else "0:-1:0:0:0")
                Toast.makeText(this, if (isChecked) "144 FPS on!" else "144 FPS off", Toast.LENGTH_SHORT).show()
                Log.i("FPSUnlocker", "true")
                updateUi(isChecked)
                prefs.edit { putBoolean("fps_enabled", isChecked) }
                if (isChecked) {
                    startService(Intent(this, FpsService::class.java))
                } else {
                    stopService(Intent(this, FpsService::class.java))
                }
            } catch (e: SecurityException) {
                Log.e("FPSUnlocker", "SecurityException: ${e.message}", e)
                Toast.makeText(this, "SecurityException: ${e.message}", Toast.LENGTH_LONG).show()
                fpsSwitch.isChecked = !isChecked
                updateUi(!isChecked)
            } catch (e: Exception) {
                Log.e("FPSUnlocker", "err: ${e.message}", e)
                Toast.makeText(this, "err: ${e.message}", Toast.LENGTH_LONG).show()
                fpsSwitch.isChecked = !isChecked
                updateUi(!isChecked)
            }
        }

        val currentValue = Settings.System.getString(contentResolver, "gamecube_frame_interpolation_for_sr")
        fpsSwitch.isChecked = currentValue == "1:1::72:144"
        updateUi(fpsSwitch.isChecked)
        Log.d("FPSUnlocker", "Initial: 'gamecube_frame_interpolation_for_sr' = '$currentValue'")

        fpsSwitch.setOnCheckedChangeListener { _, isChecked ->
            setSystemSetting(isChecked)
        }

        findViewById<ImageButton>(R.id.github_button).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW,
                "https://github.com/your-username/your-repo".toUri()))
        }
    }
}