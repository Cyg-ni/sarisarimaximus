package com.example.sarisarisales

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.sarisarisales.databinding.ActivityMainDashboardBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Data class to represent a store
data class Store(
    val id: String = "",
    val name: String = "",
    val ownerId: String = ""
)

class MainDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainDashboardBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val userStores = mutableListOf<Store>()
    private var currentStoreId: String? = null // Must be 'var' for reassignment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityMainDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        updateGreeting()
        loadUserStores()
        setupActionListeners()
    }

    private fun updateGreeting() {
        val user = auth.currentUser
        val email = user?.email?.split("@")?.get(0) ?: "User"
        binding.greetingText.text = "Hello $email!"
    }

    private fun loadUserStores() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("stores")
            .whereEqualTo("ownerId", userId)
            .get()
            .addOnSuccessListener { result ->
                userStores.clear()

                for (document in result) {
                    val store = document.toObject(Store::class.java).copy(id = document.id)
                    userStores.add(store)
                }

                if (userStores.isNotEmpty()) {
                    currentStoreId = userStores.first().id
                }

                updateStoreDisplay()
                populateDropdownMenu()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load stores: ${e.message}", Toast.LENGTH_LONG).show()
                updateStoreDisplay()
            }
    }

    private fun updateStoreDisplay() {
        val currentStore = userStores.find { it.id == currentStoreId }

        if (currentStore != null) {
            binding.storeNameText.text = "Store: ${currentStore.name}"
            binding.storeDropdownHeader.isEnabled = true
            binding.dropdownArrow.visibility = View.VISIBLE
        } else {
            binding.storeNameText.text = "Store: Set Up Your First Store"
            binding.storeDropdownHeader.isEnabled = true
            binding.dropdownArrow.visibility = View.INVISIBLE
        }
    }

    // --- Store Dropdown Logic ---

    private fun populateDropdownMenu() {
        binding.dropdownOptionsContainer.removeAllViews()

        val context = this

        userStores.forEach { store ->
            val storeView = createDropdownOption(
                context,
                store.name,
                R.color.colorPrimary,
                R.dimen.padding_12dp
            )
            storeView.setOnClickListener {
                currentStoreId = store.id
                updateStoreDisplay()
                binding.storeDropdownMenu.visibility = View.GONE
            }
            binding.dropdownOptionsContainer.addView(storeView)

            if (userStores.indexOf(store) < userStores.size - 1) {
                binding.dropdownOptionsContainer.addView(createSeparator(context))
            }
        }

        if (userStores.isNotEmpty()) {
            binding.dropdownOptionsContainer.addView(createSeparator(context))
        }

        // The "Add New Store" button
        val addStoreView = createDropdownOption(
            context,
            "Add New Store",
            R.color.colorPrimary,
            R.dimen.padding_12dp,
            R.drawable.ic_add_store
        )
        addStoreView.setOnClickListener {
            binding.storeDropdownMenu.visibility = View.GONE
            showAddStoreDialog()
        }
        binding.dropdownOptionsContainer.addView(addStoreView)
    }

    private fun createDropdownOption(
        context: Context,
        optionText: String, // Corrected parameter name
        textColorResId: Int,
        paddingResId: Int,
        iconResId: Int? = null
    ): TextView {
        return TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = optionText // Correct assignment
            setPadding(resources.getDimensionPixelSize(paddingResId), 0, resources.getDimensionPixelSize(paddingResId), 0)
            setTextColor(context.getColor(textColorResId))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            gravity = Gravity.CENTER_VERTICAL
            minHeight = resources.getDimensionPixelSize(R.dimen.padding_12dp) * 2

            iconResId?.let {
                setCompoundDrawablesWithIntrinsicBounds(it, 0, 0, 0)
                compoundDrawablePadding = resources.getDimensionPixelSize(R.dimen.padding_12dp) / 2
            }
        }
    }

    private fun createSeparator(context: Context): View {
        return View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(R.dimen.separator_height)
            )
            setBackgroundColor(context.getColor(R.color.light_gray_separator))
        }
    }

    // Function that displays the dialog to collect the new store name
    private fun showAddStoreDialog() {
        val input = EditText(this).apply {
            setHint("Enter Store Name (e.g., Sari-Sari Max)")
            setSingleLine(true)
        }

        AlertDialog.Builder(this)
            .setTitle("Add New Store")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val storeName = input.text.toString().trim()
                if (storeName.isNotEmpty()) {
                    addNewStore(storeName)
                } else {
                    Toast.makeText(this, "Store name cannot be empty.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Function that saves the new store to Firestore
    private fun addNewStore(name: String) {
        val userId = auth.currentUser?.uid ?: return

        val newStore = Store(name = name, ownerId = userId)

        db.collection("stores")
            .add(newStore)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Store '$name' added successfully!", Toast.LENGTH_SHORT).show()
                val createdStore = newStore.copy(id = documentReference.id)
                userStores.add(createdStore)
                currentStoreId = createdStore.id

                updateStoreDisplay()
                populateDropdownMenu()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error adding store: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // --- Action Button & Navigation Listeners ---

    private fun setupActionListeners() {

        binding.menuIcon.setOnClickListener {
            showLogoutConfirmation()
        }

        binding.profileIcon.setOnClickListener {
            val intent = Intent(this, SettingsAccountActivity::class.java)
            startActivity(intent)
        }

        // Toggles the visibility of the floating menu card
        binding.storeDropdownHeader.setOnClickListener {
            if (binding.storeDropdownHeader.isEnabled) {
                val menu = binding.storeDropdownMenu
                menu.visibility = if (menu.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }
        }

        binding.checkSalesButton.setOnClickListener {
            if (currentStoreId != null) {
                val intent = Intent(this, WeeklyTransactionsActivity::class.java)
                intent.putExtra("STORE_ID", currentStoreId)
                startActivity(intent)
            } else {
                Snackbar.make(it, "Please set up or select a store first.", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.manageItemsButton.setOnClickListener {
            if (currentStoreId != null) {
                val intent = Intent(this, ProductItemActivity::class.java)
                intent.putExtra("STORE_ID", currentStoreId)
                startActivity(intent)
            } else {
                Snackbar.make(it, "Please set up or select a store first.", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.navHome.setOnClickListener {
            Snackbar.make(it, "Already on Dashboard/Home", Snackbar.LENGTH_SHORT).show()
        }
        binding.navLogging.setOnClickListener {
            val intent = Intent(this, SalesLoggingActivity::class.java)
            startActivity(intent)
        }
        binding.navPOS.setOnClickListener {
            val intent = Intent(this, PosDashboardActivity::class.java)
            startActivity(intent)
        }
        binding.navSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    // --- Logout Function ---

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Logout") { _, _ ->
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}