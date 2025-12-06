package com.example.sarisarisales

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.sarisarisales.databinding.ActivityMainDashboardBinding
import com.google.firebase.auth.FirebaseAuth

class MainDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainDashboardBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding for activity_main_dashboard.xml
        binding = ActivityMainDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // 1. Update the Greeting Text with the User's Name
        updateGreeting()

        // 2. Set up Navigational Click Listeners
        setupActionListeners()

        // 3. Set up Bottom Navigation Bar
        setupBottomNavListeners()
    }

    /**
     * Retrieves the user's name from Firebase and updates the greeting TextView.
     */
    private fun updateGreeting() {
        val user = auth.currentUser
        val userName = user?.displayName

        if (!userName.isNullOrEmpty()) {
            // Uses the string resource defined in strings.xml: <string name="hello_user_placeholder">Hello %1$s!</string>
            // %1$s is the placeholder for the user's name.
            binding.greetingText.text = getString(R.string.hello_user_placeholder, userName)
        } else {
            // Fallback if no display name is set
            binding.greetingText.text = "Hello there!"
        }
    }

    /**
     * Sets listeners for the main action buttons and the menu/logout icon.
     */
    private fun setupActionListeners() {

        // Action Button: Check Sales (Navigate to WeeklyTransactionsActivity for reports)
        binding.checkSalesButton.setOnClickListener {
            // Assuming WeeklyTransactionsActivity is where sales data is reviewed
            val intent = Intent(this, WeeklyTransactionsActivity::class.java)
            startActivity(intent)
        }

        // Action Button: Manage Items (Assuming this leads to an inventory/product management screen)
        binding.manageItemsButton.setOnClickListener {
            // Assuming ProductItemActivity handles inventory management
            val intent = Intent(this, ProductItemActivity::class.java)
            startActivity(intent)
        }

        // Top Icon: Menu/Logout
        binding.menuIcon.setOnClickListener {
            showLogoutConfirmation()
        }

        // Top Icon: Profile Icon (Could also lead to settings/profile)
        binding.profileIcon.setOnClickListener {
            // Assuming SettingsAccountActivity shows user profile details
            val intent = Intent(this, SettingsAccountActivity::class.java)
            startActivity(intent)
        }

        // Store Dropdown Header: Placeholder for multi-store selection logic
        binding.storeDropdownHeader.setOnClickListener {
            // Toggle visibility of the store dropdown menu
            val menu = binding.storeDropdownMenu
            menu.visibility = if (menu.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }

    /**
     * Sets listeners for the bottom navigation icons.
     */
    private fun setupBottomNavListeners() {
        // Home is already the current activity, no action needed on navHome.

        // Logging (Sales Logging)
        binding.navLogging.setOnClickListener {
            val intent = Intent(this, SalesLoggingActivity::class.java)
            startActivity(intent)
        }

        // POS (Point of Sale)
        binding.navPOS.setOnClickListener {
            // Assuming PosDashboardActivity is your Point of Sale interface
            val intent = Intent(this, PosDashboardActivity::class.java)
            startActivity(intent)
        }

        // Settings
        binding.navSettings.setOnClickListener {
            // Assuming SettingsActivity is the main settings screen
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Shows an AlertDialog to confirm user logout.
     */
    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Logout") { _, _ ->
                auth.signOut()
                // Navigate back to the LoginActivity and clear the back stack
                val intent = Intent(this, LoginActivity::class.java)
                // These flags prevent the user from hitting the back button to return to the dashboard
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}