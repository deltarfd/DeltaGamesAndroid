package com.deltarfd.deltagamesandroid

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.deltarfd.deltagamesandroid.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupEdgeToEdge()

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavView.setupWithNavController(navController)
    }

    private fun setupEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            binding.bottomNavView.updatePadding(bottom = navBar.bottom)
            insets
        }
    }

    /**
     * Called instead of recreating the Activity when locale changes
     * (because configChanges="locale|layoutDirection" is set in the manifest).
     *
     * We update the activity's resources in-place and then re-navigate to
     * the current destination with zero animation — the screen never goes black.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Refresh bottom nav labels with new locale
        binding.bottomNavView.menu.clear()
        binding.bottomNavView.inflateMenu(R.menu.bottom_nav_menu)
        binding.bottomNavView.setupWithNavController(navController)

        // Re-navigate to the current destination with no animation.
        val currentDest = navController.currentDestination?.id ?: return
        val noAnim = NavOptions.Builder()
            .setEnterAnim(0).setExitAnim(0)
            .setPopEnterAnim(0).setPopExitAnim(0)
            .setPopUpTo(currentDest, inclusive = true)
            .build()
        navController.navigate(currentDest, null, noAnim)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
