package com.example.sarisarisales

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_dashboard) //MainActivity layout

        val btnWeekly: Button? = findViewById(R.id.check_sales_button)
        btnWeekly?.setOnClickListener {
            startActivity(Intent(this, WeeklyTransactionsActivity::class.java))
        }

    }
}
