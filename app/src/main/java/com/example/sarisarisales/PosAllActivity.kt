package com.example.sarisarisales

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.example.sarisarisales.databinding.PosLoggingCombinedBinding

class
PosAllActivity : AppCompatActivity() {

    private lateinit var binding: PosLoggingCombinedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PosLoggingCombinedBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                // Hide all grids
                tabs.values.forEach { it.visibility = View.GONE }

                // Show selected grid
                grid.visibility = View.VISIBLE

                // Reset all tabs to normal style
                tabs.keys.forEach { it.setTextAppearance(R.style.CategoryTabNormal) }

                // Highlight selected tab
                tab.setTextAppearance(R.style.CategoryTabActive)
            }
        }

        setupProductClickListeners()

        binding.btnCart.setOnClickListener {
            val intent = Intent(this, TransactionsActivity::class.java)
            startActivity(intent)
        }
        NavigationHandler.setupBottomNavBar(this)
    }
    private fun setupProductClickListeners() {
        // A list of all the parent layouts (ScrollViews) that contain our product grids
        val categoryGrids = listOf(
            binding.gridAll,
            binding.gridCondiments,
            binding.gridDrinks,
            binding.gridJunkFood,
            binding.gridBread,
            binding.gridCannedGoods
        )

        // Loop through each category's scroll view
        categoryGrids.forEach { scrollView ->
            // The GridLayout is the first (and only) child of the ScrollView
            val gridLayout = scrollView.getChildAt(0) as? ViewGroup ?: return@forEach

            // Loop through each item inside the GridLayout (these are the LinearLayouts)
            gridLayout.children.forEach { productContainer ->
                // Check if the container is a LinearLayout
                if (productContainer is LinearLayout) {
                    // We identify a "product" if its first child is an ImageView.
                    // The "Add New" button's first child is a FrameLayout, so it will be ignored.
                    if (productContainer.childCount > 0 && productContainer.getChildAt(0) is ImageView) {
                        productContainer.setOnClickListener {
                            Toast.makeText(this, "Item added to cart!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}
