package com.example.sarisarisales

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sarisarisales.databinding.SalesLoggingAllBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

// Data Classes
data class IngredientData(val name: String, val cost: Double)
data class ProductData(
    val name: String,
    val originalCost: Double,
    val markup: Double,
    val ingredients: List<IngredientData>,
    val finalCost: Double
)

class SalesLoggingActivity : AppCompatActivity() {
    private lateinit var binding: SalesLoggingAllBinding
    private var activeGrid: GridLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SalesLoggingAllBinding.inflate(layoutInflater)
        setContentView(binding.root)

        activeGrid = binding.gridAll.getChildAt(0) as? GridLayout

        // 1. Setup Tabs
        val tabs = mapOf(
            binding.tvAll to binding.gridAll,
            binding.tvCondiments to binding.gridCondiments,
            binding.tvDrinks to binding.gridDrinks,
            binding.tvJunkFood to binding.gridJunkFood,
            binding.tvBread to binding.gridBread,
            binding.tvCannedGoods to binding.gridCannedGoods
        )

        tabs.forEach { (tab, gridScrollView) ->
            tab.setOnClickListener {
                tabs.values.forEach { it.visibility = View.GONE }
                gridScrollView.visibility = View.VISIBLE
                activeGrid = gridScrollView.getChildAt(0) as? GridLayout
                tabs.keys.forEach { it.setTextAppearance(R.style.CategoryTabNormal) }
                tab.setTextAppearance(R.style.CategoryTabActive)
            }
        }

        // 2. Setup the "Add (+)" Buttons
        setupAddItemButtons()

        // 3. Setup listeners for items that already exist in the XML
        setupClickListenersForExistingItems()

        NavigationHandler.setupBottomNavBar(this)
    }

    private fun setupClickListenersForExistingItems() {
        val grids = listOf(
            binding.gridAll,
            binding.gridCondiments,
            binding.gridDrinks,
            binding.gridJunkFood,
            binding.gridBread,
            binding.gridCannedGoods
        )

        grids.forEach { scrollView ->
            val grid = scrollView.getChildAt(0) as? GridLayout
            if (grid != null) {
                val itemCount = if (grid.childCount > 0) grid.childCount - 1 else 0

                for (i in 0 until itemCount) {
                    val itemContainer = grid.getChildAt(i)
                    if (itemContainer.tag == null) {
                        var productName = "Unknown Item"
                        if (itemContainer is ViewGroup) {
                            for (j in 0 until itemContainer.childCount) {
                                val child = itemContainer.getChildAt(j)
                                if (child is TextView) {
                                    productName = child.text.toString()
                                    break
                                }
                            }
                        }

                        val dummyData = ProductData(
                            name = productName,
                            originalCost = 0.0,
                            markup = 0.0,
                            ingredients = emptyList(),
                            finalCost = 0.0
                        )

                        itemContainer.tag = dummyData
                        itemContainer.setOnClickListener { view ->
                            val data = view.tag as? ProductData
                            if (data != null) showProductDetailsDialog(data, view)
                        }
                    }
                }
            }
        }
    }

    private fun setupAddItemButtons() {
        val grids = listOf(
            binding.gridCondiments,
            binding.gridDrinks,
            binding.gridJunkFood,
            binding.gridBread,
            binding.gridCannedGoods
        )

        grids.forEach { scrollView ->
            (scrollView.getChildAt(0) as? GridLayout)?.let { grid ->
                if (grid.childCount > 0) {
                    val addButton = grid.getChildAt(grid.childCount - 1)
                    addButton.setOnClickListener {
                        // Pass null for edit mode args to indicate "Add New"
                        showAddItemDialog(null, null)
                    }
                }
            }
        }
    }

    /**
     * This function handles both ADDING new items and EDITING existing items.
     * @param productToEdit: If provided, pre-fills the form.
     * @param itemContainer: If provided, updates this view instead of creating a new one.
     */
    private fun showAddItemDialog(productToEdit: ProductData?, itemContainer: View?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.modal_add_item, null)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create()

        val etItemName = dialogView.findViewById<EditText>(R.id.et_item_name)
        val etOriginalCost = dialogView.findViewById<EditText>(R.id.et_original_cost)
        val etMarkup = dialogView.findViewById<EditText>(R.id.et_markup)
        val etTotalCost = dialogView.findViewById<EditText>(R.id.et_total_cost)
        val llIngredientList = dialogView.findViewById<LinearLayout>(R.id.ll_ingredient_list)
        val etIngredientName = dialogView.findViewById<EditText>(R.id.et_ingredient_name)
        val etIngredientCost = dialogView.findViewById<EditText>(R.id.et_ingredient_cost)
        val btnAddIngredient = dialogView.findViewById<Button>(R.id.btn_add_ingredient)
        val btnListItem = dialogView.findViewById<Button>(R.id.btn_list_item)

        val currentIngredients = mutableListOf<IngredientData>()

        // --- Helper to add row to UI ---
        fun addIngredientRow(name: String, cost: Double) {
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            val nameTextView = TextView(this, null, 0, R.style.TableCell).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
                text = name
            }
            val costTextView = TextView(this, null, 0, R.style.TableCell).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = String.format(Locale.US, "%.2f", cost)
            }
            rowLayout.addView(nameTextView)
            rowLayout.addView(costTextView)
            llIngredientList.addView(rowLayout)
        }

        // --- CALCULATION LOGIC ---
        val calculateTotalCost = {
            val originalCost = etOriginalCost.text.toString().toDoubleOrNull() ?: 0.0
            val markupPercent = etMarkup.text.toString().toDoubleOrNull() ?: 0.0
            val ingredientsTotal = currentIngredients.sumOf { it.cost }
            val baseCost = originalCost + ingredientsTotal
            val finalCost = baseCost * (1 + (markupPercent / 100.0))
            etTotalCost.setText(String.format(Locale.US, "%.2f", finalCost))
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { calculateTotalCost() }
        }
        etOriginalCost.addTextChangedListener(textWatcher)
        etMarkup.addTextChangedListener(textWatcher)

        // --- PRE-FILL DATA IF EDITING ---
        if (productToEdit != null) {
            etItemName.setText(productToEdit.name)
            etOriginalCost.setText(productToEdit.originalCost.toString())
            etMarkup.setText(productToEdit.markup.toString())

            // Re-populate ingredients
            productToEdit.ingredients.forEach {
                currentIngredients.add(it)
                addIngredientRow(it.name, it.cost)
            }
            calculateTotalCost()

            // Change button text
            btnListItem.text = "Save Changes"
        }

        // --- ADD INGREDIENT BUTTON ---
        btnAddIngredient.setOnClickListener {
            val name = etIngredientName.text.toString().trim()
            val costStr = etIngredientCost.text.toString().trim()

            if (name.isNotEmpty() && costStr.isNotEmpty()) {
                val cost = costStr.toDoubleOrNull()
                if (cost != null) {
                    currentIngredients.add(IngredientData(name, cost))
                    addIngredientRow(name, cost)

                    etIngredientName.text.clear()
                    etIngredientCost.text.clear()
                    etIngredientName.requestFocus()
                    calculateTotalCost()
                } else { etIngredientCost.error = "Invalid cost" }
            } else {
                if (name.isEmpty()) etIngredientName.error = "Name required"
                if (costStr.isEmpty()) etIngredientCost.error = "Cost required"
            }
        }

        // --- SAVE/LIST ITEM BUTTON ---
        btnListItem.setOnClickListener {
            val itemName = etItemName.text.toString().trim()
            val originalCost = etOriginalCost.text.toString().toDoubleOrNull() ?: 0.0
            val markup = etMarkup.text.toString().toDoubleOrNull() ?: 0.0
            val finalCost = etTotalCost.text.toString().toDoubleOrNull() ?: 0.0

            if (itemName.isNotEmpty()) {
                val newProductData = ProductData(
                    name = itemName,
                    originalCost = originalCost,
                    markup = markup,
                    ingredients = currentIngredients.toList(),
                    finalCost = finalCost
                )

                if (productToEdit == null) {
                    // MODE: ADD NEW
                    activeGrid?.let { grid ->
                        addItemToGrid(newProductData, grid)
                        Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // MODE: EDIT EXISTING
                    // Update the tag data
                    itemContainer?.tag = newProductData
                    // Update the text in the UI
                    val titleView = itemContainer?.findViewById<TextView>(R.id.productTitle)
                    titleView?.text = itemName
                    Toast.makeText(this, "Item updated", Toast.LENGTH_SHORT).show()
                }

                dialog.dismiss()
            } else {
                etItemName.error = "Item name cannot be empty"
            }
        }

        dialog.show()
    }

    private fun addItemToGrid(productData: ProductData, targetGrid: GridLayout) {
        val newItemView = LayoutInflater.from(this).inflate(R.layout.product_item, targetGrid, false)
        val productTitle = newItemView.findViewById<TextView>(R.id.productTitle)

        productTitle.text = productData.name
        newItemView.tag = productData

        newItemView.setOnClickListener { view ->
            val savedData = view.tag as? ProductData
            if (savedData != null) {
                showProductDetailsDialog(savedData, view)
            }
        }

        val insertIndex = targetGrid.childCount - 1
        targetGrid.addView(newItemView, insertIndex)
    }

    private fun showProductDetailsDialog(product: ProductData, itemContainer: View) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.modal_product_details, null)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create()

        val tvName = dialogView.findViewById<TextView>(R.id.tv_view_product_name)
        val tvOriginalCost = dialogView.findViewById<TextView>(R.id.tv_view_original_cost)
        val tvMarkup = dialogView.findViewById<TextView>(R.id.tv_view_markup)
        val tvTotalCost = dialogView.findViewById<TextView>(R.id.tv_view_total_cost)
        val llIngredients = dialogView.findViewById<LinearLayout>(R.id.ll_view_ingredients_list)
        val btnClose = dialogView.findViewById<Button>(R.id.btn_close_details)
        val btnEdit = dialogView.findViewById<Button>(R.id.btn_edit_details) // New Button

        tvName.text = product.name
        tvOriginalCost.text = String.format(Locale.US, "%.2f", product.originalCost)
        tvMarkup.text = "${product.markup}%"
        tvTotalCost.text = String.format(Locale.US, "%.2f", product.finalCost)

        if (product.ingredients.isEmpty()) {
            val emptyMsg = TextView(this)
            emptyMsg.text = "No ingredients data available"
            emptyMsg.setPadding(10, 10, 10, 10)
            llIngredients.addView(emptyMsg)
        } else {
            product.ingredients.forEach { ing ->
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    setPadding(0, 5, 0, 5)
                }
                val nameTv = TextView(this).apply {
                    text = "• ${ing.name}"
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
                }
                val costTv = TextView(this).apply {
                    text = String.format(Locale.US, "%.2f", ing.cost)
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
                row.addView(nameTv)
                row.addView(costTv)
                llIngredients.addView(row)
            }
        }

        // EDIT BUTTON CLICK LISTENER
        btnEdit.setOnClickListener {
            dialog.dismiss() // Close the View Details dialog
            // Open the Add/Edit dialog, passing the current data and the view to be updated
            showAddItemDialog(product, itemContainer)
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}