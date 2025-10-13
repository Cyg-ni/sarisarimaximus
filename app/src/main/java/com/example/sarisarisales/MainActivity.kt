package com.example.sarisarisales

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.sarisarisales.databinding.ActivityWeeklyTransactionsBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWeeklyTransactionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeeklyTransactionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toggle Summary
        binding.btnSummary.setOnClickListener {
            toggleVisibility(binding.summaryLayout.root)
        }

        // Toggle Transaction History
        binding.btnTransactionHistory.setOnClickListener {
            toggleVisibility(binding.transactionHistoryLayout.root)
        }
    }

    private fun toggleVisibility(view: View) {
        view.visibility = if (view.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }
}
