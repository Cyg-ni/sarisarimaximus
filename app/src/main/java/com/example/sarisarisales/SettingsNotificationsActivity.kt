package com.example.sarisarisales

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class SettingsNotificationsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_notifications)

        // 🔹 Top bar buttons
        val btnBack: ImageButton = findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }

        }
    }

