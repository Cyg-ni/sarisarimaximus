package com.example.sarisarisales

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class TransactionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.confirm_transaction)

        val container = findViewById<LinearLayout>(R.id.llCartContainer) // You need to add this ID in XML
        val tvTotalCost = findViewById<TextView>(R.id.tvTotalCost) // Add ID in XML
        val etCash = findViewById<EditText>(R.id.etCash)
        val tvChange = findViewById<TextView>(R.id.tvChange) // Add ID in XML
        val btnConfirm = findViewById<android.widget.Button>(R.id.btnConfirm)

        val cart = CartRepository.currentCart
        var totalAmount = 0.0

        // 1. Build the Cart UI dynamically
        container.removeAllViews() // Clear placeholder items

        // Re-add Title
        val titleView = TextView(this).apply {
            text = "Cart"
            textSize = 20f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { gravity = Gravity.CENTER; bottomMargin = 32 }
        }
        container.addView(titleView)

        for (item in cart) {
            val cost = item.product.finalCost * item.quantity
            totalAmount += cost

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                weightSum = 4f
                setPadding(0, 0, 0, 16)
            }

            val tvName = TextView(this).apply {
                text = item.product.name
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
            }

            val tvQty = TextView(this).apply {
                text = "${item.quantity}x"
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                gravity = Gravity.END
            }

            val tvPrice = TextView(this).apply {
                text = String.format(Locale.US, "%.2f", cost)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                gravity = Gravity.END
            }

            row.addView(tvName)
            row.addView(tvQty)
            row.addView(tvPrice)
            container.addView(row)
        }

        // 2. Update Total
        tvTotalCost.text = String.format(Locale.US, "TOTAL COST     %.2f PHP", totalAmount)

        // 3. Calculate Change Logic
        etCash.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val cash = s.toString().toDoubleOrNull() ?: 0.0
                val change = cash - totalAmount
                if (change >= 0) {
                    tvChange.text = String.format(Locale.US, "CHANGE     %.2f PHP", change)
                } else {
                    tvChange.text = "CHANGE     0.00 PHP"
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnConfirm.setOnClickListener {
            val cash = etCash.text.toString().toDoubleOrNull() ?: 0.0
            if (cash >= totalAmount) {
                Toast.makeText(this, "Transaction Saved!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                etCash.error = "Insufficient Cash"
            }
        }
    }
}