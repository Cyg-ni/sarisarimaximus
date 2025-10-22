package com.example.sarisarisales

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class SettingsAccountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_account)

        // 🔹 Top bar buttons
        val btnBack: ImageButton = findViewById(R.id.btnBack)
        val btnLogOut: LinearLayout = findViewById(R.id.btnLogOut)

        btnBack.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btnLogOut.setOnClickListener {
             startActivity(Intent(this, LoginActivity::class.java))
         }

        }
    }

