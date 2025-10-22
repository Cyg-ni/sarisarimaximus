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

        // Top buttons
        val btnBack: ImageButton = findViewById(R.id.btnBack)
        val btnDashboard: ImageButton = findViewById(R.id.btnDashboard)


        btnBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        btnDashboard.setOnClickListener {
            // Navigate to Dashboard --> Change the values for this if there is a dashboard already
            // startActivity(Intent(this, MainActivity::class.java))
            // finish()
        }

        NavigationHandler.setupBottomNavBar(this)
    }
}
