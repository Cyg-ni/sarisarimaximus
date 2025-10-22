package com.example.sarisarisales

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
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

class SalesLoggingActivity : AppCompatActivity() {
    private lateinit var binding: SalesLoggingAllBinding
    private var activeGrid: GridLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SalesLoggingAllBinding.inflate(layoutInflater)
        setContentView(binding.root)

        activeGrid = binding.gridAll.getChildAt(0) as? GridLayout

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

        setupAddItemButtons()
        NavigationHandler.setupBottomNavBar(this)
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
                val addButton = grid.getChildAt(grid.childCount - 1)
                addButton.setOnClickListener {
                    showAddItemDialog()
                }
            }
        }
    }

    private fun showAddItemDialog() {
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
        val ingredientCosts = mutableListOf<Double>()

        val calculateTotalCost = {
            val originalCost = etOriginalCost.text.toString().toDoubleOrNull() ?: 0.0
            val markupPercent = etMarkup.text.toString().toDoubleOrNull() ?: 0.0
            val ingredientsTotal = ingredientCosts.sum()
            val baseCost = originalCost + ingredientsTotal
            val finalCost = baseCost * (1 + (markupPercent / 100.0))
            etTotalCost.setText(String.format(Locale.US, "%.2f", finalCost))
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calculateTotalCost()
            }
        }
        etOriginalCost.addTextChangedListener(textWatcher)
        etMarkup.addTextChangedListener(textWatcher)

        btnAddIngredient.setOnClickListener {
            val name = etIngredientName.text.toString().trim()
            val costStr = etIngredientCost.text.toString().trim()

            if (name.isNotEmpty() && costStr.isNotEmpty()) {
                val cost = costStr.toDoubleOrNull()
                if (cost != null) {
                    ingredientCosts.add(cost)

                    val rowLayout = LinearLayout(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        orientation = LinearLayout.HORIZONTAL
                    }

                    val nameTextView = TextView(this, null, 0, R.style.TableCell).apply {
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
                        text = name
                    }

                    val dividerView = View(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            resources.getDimensionPixelSize(R.dimen.table_divider_width),
                            LinearLayout.LayoutParams.MATCH_PARENT
                        )
                        setBackgroundColor(resources.getColor(R.color.table_divider_color, null))
                    }

                    val costTextView = TextView(this, null, 0, R.style.TableCell).apply {
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        text = String.format(Locale.US, "%.2f", cost)
                    }

                    rowLayout.addView(nameTextView)
                    rowLayout.addView(dividerView)
                    rowLayout.addView(costTextView)
                    llIngredientList.addView(rowLayout)

                    etIngredientName.text.clear()
                    etIngredientCost.text.clear()
                    etIngredientName.requestFocus()
                    calculateTotalCost()
                } else {
                    etIngredientCost.error = "Invalid cost"
                }
            } else {
                if (name.isEmpty()) etIngredientName.error = "Name required"
                if (costStr.isEmpty()) etIngredientCost.error = "Cost required"
            }
        }

        // --- MODIFICATION IS HERE ---
        btnListItem.setOnClickListener {
            val itemName = etItemName.text.toString().trim()
            if (itemName.isNotEmpty()) {
                activeGrid?.let { grid ->
                    addItemToGrid(itemName, grid)
                }

                // Show the toast notification
                Toast.makeText(this, "Item has been listed", Toast.LENGTH_SHORT).show()

                dialog.dismiss()
            } else {
                etItemName.error = "Item name cannot be empty"
            }
        }

        dialog.show()
    }

    private fun addItemToGrid(itemName: String, targetGrid: GridLayout) {
        val newItemView = LayoutInflater.from(this).inflate(R.layout.product_item, targetGrid, false)
        val productTitle = newItemView.findViewById<TextView>(R.id.productTitle)
        productTitle.text = itemName
        val insertIndex = targetGrid.childCount - 1
        targetGrid.addView(newItemView, insertIndex)
    }
}