package com.deltarfd.deltagamesandroid

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
        overridePendingTransition(0, 0)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavView.setupWithNavController(navController)
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

        // Apply the new configuration to this activity's resources immediately
        @Suppress("DEPRECATION")
        resources.updateConfiguration(newConfig, resources.displayMetrics)

        // Re-navigate to the current destination with no animation.
        // This forces the current fragment to re-inflate its views using the
        // updated locale, so all @string/ references pick up the new language.
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

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }
}