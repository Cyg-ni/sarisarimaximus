package com.example.sarisarisales

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var storeDropdownHeader: LinearLayout
    private lateinit var dropdownArrow: ImageView
    private lateinit var storeDropdownMenu: CardView
    private var isDropdownVisible = false

    private lateinit var checkSalesButton: Button
    private lateinit var manageItemsButton: Button

    private lateinit var navHome: ImageView
    private lateinit var navLogging: ImageView
    private lateinit var navPOS: ImageView
    private lateinit var navSettings: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_dashboard)

        // Initialize elements
        storeDropdownHeader = findViewById(R.id.store_dropdown_header)
        dropdownArrow = findViewById(R.id.dropdown_arrow)
        storeDropdownMenu = findViewById(R.id.store_dropdown_menu)

        checkSalesButton = findViewById(R.id.check_sales_button)
        manageItemsButton = findViewById(R.id.manage_items_button)

        navHome = findViewById(R.id.navHome)
        navLogging = findViewById(R.id.navLogging)
        navPOS = findViewById(R.id.navPOS)
        navSettings = findViewById(R.id.navSettings)

        // Toggle dropdown visibility
        storeDropdownHeader.setOnClickListener {
            toggleDropdownMenu()
        }

        // Navigate to Weekly Transactions
        checkSalesButton.setOnClickListener {
            startActivity(Intent(this, WeeklyTransactionsActivity::class.java))
        }

        // Manage items (placeholder)
        manageItemsButton.setOnClickListener {
            Snackbar.make(it, "Opening Item Management...", Snackbar.LENGTH_SHORT).show()
        }

        // Bottom navigation
        navHome.setOnClickListener {
            Snackbar.make(it, "Already in Home", Snackbar.LENGTH_SHORT).show()
        }
        navLogging.setOnClickListener {
            startActivity(Intent(this, SalesLoggingActivity::class.java))
        }
        navPOS.setOnClickListener {
            startActivity(Intent(this, PosAllActivity::class.java))
        }
        navSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

         NavigationHandler.setupBottomNavBar(this)
    }

    private fun toggleDropdownMenu() {
        if (isDropdownVisible) {
            storeDropdownMenu.visibility = View.GONE
            dropdownArrow.setImageResource(R.drawable.ic_arrow_down)
        } else {
            storeDropdownMenu.visibility = View.VISIBLE
            dropdownArrow.setImageResource(R.drawable.ic_arrow_up)
        }
        isDropdownVisible = !isDropdownVisible
    }
}
