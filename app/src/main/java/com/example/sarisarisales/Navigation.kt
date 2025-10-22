package com.example.sarisarisales

import android.app.Activity
import android.content.Intent
import android.widget.ImageView
import com.example.sarisarisales.R

object NavigationHandler {

    fun setupBottomNavBar(currentActivity: Activity) {

        val navHome = currentActivity.findViewById<ImageView>(R.id.navHome)
        val navLogging = currentActivity.findViewById<ImageView>(R.id.navLogging)
        val navPOS = currentActivity.findViewById<ImageView>(R.id.navPOS)
        val navSettings = currentActivity.findViewById<ImageView>(R.id.navSettings)


        // Home Navigation
        navHome.setOnClickListener {
            if (currentActivity !is MainActivity) {
                val intent = Intent(currentActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                currentActivity.startActivity(intent)
                currentActivity.finish()
            }
        }

        // Logging Navigation
        navLogging.setOnClickListener {
            if (currentActivity !is SalesLoggingActivity) {
                val intent = Intent(currentActivity, SalesLoggingActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                currentActivity.startActivity(intent)
                currentActivity.finish()
            }
        }

        // POS Navigation
        navPOS.setOnClickListener {
            if (currentActivity !is PosAllActivity) {
                val intent = Intent(currentActivity, PosAllActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                currentActivity.startActivity(intent)
                currentActivity.finish()
            }
        }

        // Settings Navigation
        navSettings.setOnClickListener {
            if (currentActivity !is SettingsActivity) {
                val intent = Intent(currentActivity, SettingsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                currentActivity.startActivity(intent)
                currentActivity.finish()
            }
        }
    }
}