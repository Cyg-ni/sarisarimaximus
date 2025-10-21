package com.example.sarisarisales

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // 🔹 Footer navigation buttons
        val navHome: LinearLayout = findViewById(R.id.navHome)
        val navLogging: LinearLayout = findViewById(R.id.navLogging)
        val navPOS: LinearLayout = findViewById(R.id.navPOS)
        val navSettings: LinearLayout = findViewById(R.id.navSettings)

        navHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        navLogging.setOnClickListener {
            startActivity(Intent(this, PosAllActivity::class.java))
            finish()
        }

        navPOS.setOnClickListener {
            startActivity(Intent(this, SalesLoggingActivity::class.java))
            finish()
        }

        navSettings.setOnClickListener {
            // Already in Settings, do nothing
        }
    }
}
