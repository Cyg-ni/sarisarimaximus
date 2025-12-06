package com.example.sarisarisales

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

data class IngredientData(
    val name: String = "",
    val cost: Double = 0.0
)

data class ProductData(
    var id: String = "",
    val name: String = "",
    val originalCost: Double = 0.0,
    val markup: Double = 0.0,
    val ingredients: List<IngredientData> = emptyList(),
    val finalCost: Double = 0.0,
    val category: String = "All"
)

class SalesLoggingActivity : AppCompatActivity() {
    private lateinit var binding: SalesLoggingAllBinding
    private var activeGrid: GridLayout? = null
    private lateinit var db: FirebaseFirestore

    private lateinit var categoryMap: Map<GridLayout, String>
    private lateinit var stringToGridMap: Map<String, GridLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SalesLoggingAllBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = Firebase.firestore

        initializeCategoryMaps()
        setupTabNavigation()
        setupAddItemButtons()
        setupClickListenersForExistingItems()
        fetchProductsFromFirestore()
        NavigationHandler.setupBottomNavBar(this)
    }

    private fun initializeCategoryMaps() {
        val gridCondiments = binding.gridCondiments.getChildAt(0) as GridLayout
        val gridDrinks = binding.gridDrinks.getChildAt(0) as GridLayout
        val gridJunkFood = binding.gridJunkFood.getChildAt(0) as GridLayout
        val gridBread = binding.gridBread.getChildAt(0) as GridLayout
        val gridCanned = binding.gridCannedGoods.getChildAt(0) as GridLayout
        val gridAll = binding.gridAll.getChildAt(0) as GridLayout

        categoryMap = mapOf(
            gridCondiments to "Condiments",
            gridDrinks to "Drinks",
            gridJunkFood to "JunkFood",
            gridBread to "Bread",
            gridCanned to "CannedGoods",
            gridAll to "All"
        )

        stringToGridMap = mapOf(
            "Condiments" to gridCondiments,
            "Drinks" to gridDrinks,
            "JunkFood" to gridJunkFood,
            "Bread" to gridBread,
            "CannedGoods" to gridCanned,
            "All" to gridAll
        )
    }

    private fun setupTabNavigation() {
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
    }

    private fun fetchProductsFromFirestore() {
        db.collection("products")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    try {
                        val product = document.toObject(ProductData::class.java)
                        product.id = document.id

                        val targetGrid = stringToGridMap[product.category] ?: binding.gridAll.getChildAt(0) as GridLayout
                        addItemToGrid(product, targetGrid)
                    } catch (e: Exception) {
                        Log.e("Firestore", "Error parsing product", e)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error loading data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProductToFirestore(product: ProductData, isEdit: Boolean, onSuccess: (String) -> Unit) {
        if (isEdit && product.id.isNotEmpty()) {
            db.collection("products").document(product.id)
                .set(product)
                .addOnSuccessListener { onSuccess(product.id) }
                .addOnFailureListener { e -> Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show() }
        } else {
            db.collection("products")
                .add(product)
                .addOnSuccessListener { documentReference -> onSuccess(documentReference.id) }
                .addOnFailureListener { e -> Toast.makeText(this, "Save failed: ${e.message}", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun setupClickListenersForExistingItems() {
        val grids = listOf(binding.gridAll, binding.gridCondiments, binding.gridDrinks, binding.gridJunkFood, binding.gridBread, binding.gridCannedGoods)

        grids.forEach { scrollView ->
            val grid = scrollView.getChildAt(0) as? GridLayout
            if (grid != null) {
                val hasAddButton = grid.childCount > 0 && grid.getChildAt(grid.childCount - 1).hasOnClickListeners()
                val limit = if (hasAddButton) grid.childCount - 1 else grid.childCount

                for (i in 0 until limit) {
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
                        val dummyData = ProductData(name = productName, category = "Unknown")
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
        val grids = listOf(binding.gridCondiments, binding.gridDrinks, binding.gridJunkFood, binding.gridBread, binding.gridCannedGoods)
        grids.forEach { scrollView ->
            (scrollView.getChildAt(0) as? GridLayout)?.let { grid ->
                if (grid.childCount > 0) {
                    val addButton = grid.getChildAt(grid.childCount - 1)
                    addButton.setOnClickListener {
                        val category = categoryMap[grid] ?: "All"
                        showAddItemDialog(null, null, category)
                    }
                }
            }
        }
    }

    private fun showAddItemDialog(productToEdit: ProductData?, itemContainer: View?, categoryOverride: String? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.modal_add_item, null)
        val dialog = MaterialAlertDialogBuilder(this).setView(dialogView).create()

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

        if (productToEdit != null) {
            etItemName.setText(productToEdit.name)
            etOriginalCost.setText(productToEdit.originalCost.toString())
            etMarkup.setText(productToEdit.markup.toString())
            productToEdit.ingredients.forEach {
                currentIngredients.add(it)
                addIngredientRow(it.name, it.cost)
            }
            calculateTotalCost()
            btnListItem.text = "Save Changes"
        }

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
            }
        }

        btnListItem.setOnClickListener {
            val itemName = etItemName.text.toString().trim()
            val originalCost = etOriginalCost.text.toString().toDoubleOrNull() ?: 0.0
            val markup = etMarkup.text.toString().toDoubleOrNull() ?: 0.0
            val finalCost = etTotalCost.text.toString().toDoubleOrNull() ?: 0.0

            if (itemName.isNotEmpty()) {
                val categoryToSave = categoryOverride ?: productToEdit?.category ?: "All"

                val productData = ProductData(
                    id = productToEdit?.id ?: "",
                    name = itemName,
                    originalCost = originalCost,
                    markup = markup,
                    ingredients = currentIngredients.toList(),
                    finalCost = finalCost,
                    category = categoryToSave
                )

                val isEdit = productToEdit != null
                saveProductToFirestore(productData, isEdit) { savedId ->
                    productData.id = savedId
                    if (!isEdit) {
                        val targetGrid = stringToGridMap[categoryToSave] ?: binding.gridAll.getChildAt(0) as GridLayout
                        addItemToGrid(productData, targetGrid)
                    } else {
                        itemContainer?.tag = productData
                        val titleView = itemContainer?.findViewById<TextView>(R.id.productTitle)
                        titleView?.text = itemName
                    }
                    Toast.makeText(this, if (isEdit) "Item updated" else "Item saved", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            } else {
                etItemName.error = "Item name cannot be empty"
            }
        }
        dialog.show()
    }

    private fun addItemToGrid(productData: ProductData, targetGrid: GridLayout) {
        for (i in 0 until targetGrid.childCount) {
            val child = targetGrid.getChildAt(i)
            val tag = child.tag as? ProductData
            if (tag != null && tag.id == productData.id && productData.id.isNotEmpty()) {
                return
            }
        }

        val newItemView = LayoutInflater.from(this).inflate(R.layout.product_item, targetGrid, false)
        val productTitle = newItemView.findViewById<TextView>(R.id.productTitle)

        productTitle.text = productData.name
        newItemView.tag = productData
        newItemView.setOnClickListener { view ->
            val savedData = view.tag as? ProductData
            if (savedData != null) showProductDetailsDialog(savedData, view)
        }

        val insertIndex = if (targetGrid.childCount > 0) targetGrid.childCount - 1 else 0
        targetGrid.addView(newItemView, insertIndex)
    }

    private fun showProductDetailsDialog(product: ProductData, itemContainer: View) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.modal_product_details, null)
        val dialog = MaterialAlertDialogBuilder(this).setView(dialogView).create()

        val tvName = dialogView.findViewById<TextView>(R.id.tv_view_product_name)
        val tvOriginalCost = dialogView.findViewById<TextView>(R.id.tv_view_original_cost)
        val tvMarkup = dialogView.findViewById<TextView>(R.id.tv_view_markup)
        val tvTotalCost = dialogView.findViewById<TextView>(R.id.tv_view_total_cost)
        val llIngredients = dialogView.findViewById<LinearLayout>(R.id.ll_view_ingredients_list)
        val btnClose = dialogView.findViewById<Button>(R.id.btn_close_details)
        val btnEdit = dialogView.findViewById<Button>(R.id.btn_edit_details)

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

        btnEdit.setOnClickListener {
            dialog.dismiss()
            showAddItemDialog(product, itemContainer)
        }
        btnClose.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}