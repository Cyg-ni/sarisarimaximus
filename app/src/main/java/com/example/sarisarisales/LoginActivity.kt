package com.example.sarisarisales

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

         // auto-navigate to SettingsActivity after 1 second (for testing)
         // Handler(mainLooper).postDelayed({
             // startActivity(Intent(this, SettingsActivity::class.java))
            // finish() // TO CHECK IF BUTTON IS WORKING
       // }  , 1000)
    }
 }
