package com.example.sarisarisales

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // 🔹 Top bar buttons
        val btnBack: ImageButton = findViewById(R.id.btnBack)
        val btnDashboard: ImageButton = findViewById(R.id.btnDashboard)

        btnBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        btnDashboard.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // 🔹 Settings items (these are LinearLayouts, not Buttons)
        val tabAccount: LinearLayout = findViewById(R.id.tabAccount)
        val tabNotifs: LinearLayout = findViewById(R.id.tabNotifs)
        val tabTheme: LinearLayout = findViewById(R.id.tabTheme)
        val tabHelp: LinearLayout = findViewById(R.id.tabHelp)

        tabAccount.setOnClickListener {
            startActivity(Intent(this, SettingsAccountActivity::class.java))
            finish()
        }

        tabNotifs.setOnClickListener {
            startActivity(Intent(this, SettingsNotificationsActivity::class.java))
            finish()
        }

        tabTheme.setOnClickListener {
            startActivity(Intent(this, SettingsAppearanceActivity::class.java))
            finish()
        }

        tabHelp.setOnClickListener {
            startActivity(Intent(this, SettingsHelpActivity::class.java))
            finish()
        }


        // 🔹 Bottom navigation
        val navHome: LinearLayout = findViewById(R.id.navHome)
        val navLogging: LinearLayout = findViewById(R.id.navLogging)
        val navPOS: LinearLayout = findViewById(R.id.navPOS)
        val navSettings: LinearLayout = findViewById(R.id.navSettings)

        navHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        navLogging.setOnClickListener {
            startActivity(Intent(this, SalesLoggingActivity::class.java))
            finish()
        }

        navPOS.setOnClickListener {
            startActivity(Intent(this, PosAllActivity::class.java))
            finish()
        }

        navSettings.setOnClickListener {
            // Already in Settings — no action
        }
    }
}
