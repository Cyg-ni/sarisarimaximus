package com.example.sarisarisales

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.sarisarisales.databinding.PosLoggingCombinedBinding

class PosAllActivity : AppCompatActivity() {

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
        NavigationHandler.setupBottomNavBar(this)
    }
}
