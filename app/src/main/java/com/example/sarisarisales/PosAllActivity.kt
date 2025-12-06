package com.example.sarisarisales

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sarisarisales.databinding.PosLoggingCombinedBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


data class CartItem(
    val product: ProductData,
    var quantity: Int
)

object CartRepository {
    var currentCart: List<CartItem> = emptyList()
}

class PosAllActivity : AppCompatActivity() {

    private lateinit var binding: PosLoggingCombinedBinding
    private val db = Firebase.firestore

    private val cartList = mutableListOf<CartItem>()
    private lateinit var stringToGridMap: Map<String, GridLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PosLoggingCombinedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeGridMap()
        setupTabNavigation()
        fetchProductsFromFirestore()
        setupClickListenersForExistingItems()

        binding.btnCart.setOnClickListener {
            if (cartList.isNotEmpty()) {
                val intent = Intent(this, TransactionsActivity::class.java)
                CartRepository.currentCart = cartList
                startActivity(intent)
            } else {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show()
            }
        }

        NavigationHandler.setupBottomNavBar(this)
    }

    private fun initializeGridMap() {
        stringToGridMap = mapOf(
            "Condiments" to (binding.gridCondiments.getChildAt(0) as GridLayout),
            "Drinks" to (binding.gridDrinks.getChildAt(0) as GridLayout),
            "JunkFood" to (binding.gridJunkFood.getChildAt(0) as GridLayout),
            "Bread" to (binding.gridBread.getChildAt(0) as GridLayout),
            "CannedGoods" to (binding.gridCannedGoods.getChildAt(0) as GridLayout),
            "All" to (binding.gridAll.getChildAt(0) as GridLayout)
        )
    }

    private fun setupTabNavigation() {
        val tabs = mapOf(
            binding.tvAll to binding.gridAll,
            binding.tvCondiments to binding.gridCondiments,
            binding.tvDrinks to binding.gridDrinks,
            binding.tvJunkFood to binding.gridJunkFood,
            binding.tvBread to binding.gridBread,
            binding.tvCannedGoods to binding.gridCannedGoods
        )

        tabs.forEach { (tab, grid) ->
            tab.setOnClickListener {
                tabs.values.forEach { it.visibility = View.GONE }
                grid.visibility = View.VISIBLE
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

                        if (product.category != "All") {
                            val specificGrid = stringToGridMap[product.category]
                            if (specificGrid != null) {
                                addItemToGrid(product, specificGrid)
                            }
                        }

                        val allGrid = stringToGridMap["All"]
                        if (allGrid != null) {
                            addItemToGrid(product, allGrid)
                        }

                    } catch (e: Exception) {
                        Log.e("Firestore", "Error parsing product", e)
                    }
                }
            }
    }

    private fun addItemToGrid(product: ProductData, grid: GridLayout) {
        for (i in 0 until grid.childCount) {
            val child = grid.getChildAt(i)
            val tag = child.tag as? ProductData
            if (tag != null && tag.id == product.id) return
        }

        val itemView = LayoutInflater.from(this).inflate(R.layout.product_item, grid, false)
        val title = itemView.findViewById<TextView>(R.id.productTitle)
        title.text = product.name

        itemView.tag = product

        itemView.setOnClickListener {
            addToCart(product)
        }

        grid.addView(itemView)
    }

    private fun setupClickListenersForExistingItems() {
        stringToGridMap.values.forEach { grid ->
            for (i in 0 until grid.childCount) {
                val child = grid.getChildAt(i)
                if (child.tag == null && child is LinearLayout) {

                    var extractedName = "Unknown Item"

                    fun findTextView(view: View): TextView? {
                        if (view is TextView) return view
                        if (view is ViewGroup) {
                            for (k in 0 until view.childCount) {
                                val result = findTextView(view.getChildAt(k))
                                if (result != null) return result
                            }
                        }
                        return null
                    }

                    val nameTv = findTextView(child)
                    if (nameTv != null) {
                        extractedName = nameTv.text.toString()
                    }

                    val dummyProduct = ProductData(name = extractedName, finalCost = 0.0, id = "")

                    child.setOnClickListener {
                        addToCart(dummyProduct)
                    }
                }
            }
        }
    }

    private fun addToCart(product: ProductData) {
        val existingItem = cartList.find { it.product.id == product.id && product.id.isNotEmpty() }
            ?: cartList.find { it.product.name == product.name && product.id.isEmpty() }

        if (existingItem != null) {
            existingItem.quantity++
            Toast.makeText(this, "${product.name} added (Qty: ${existingItem.quantity})", Toast.LENGTH_SHORT).show()
        } else {
            cartList.add(CartItem(product, quantity = 1))
            Toast.makeText(this, "${product.name} added (Qty: 1)", Toast.LENGTH_SHORT).show()
        }
    }
}