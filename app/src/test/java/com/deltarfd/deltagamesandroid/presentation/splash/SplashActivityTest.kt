package com.deltarfd.deltagamesandroid.presentation.splash

import android.os.Build
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.deltarfd.deltagamesandroid.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU], application = android.app.Application::class)
class SplashActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(SplashActivity::class.java)

    @Test
    fun `SplashActivity launches MainActivity after delay`() {
        activityRule.scenario.onActivity { activity ->
            // Advance Robolectric's main looper by the splash delay (1800ms)
            @Suppress("DEPRECATION")
            ShadowLooper.idleMainLooper(2000)

            // Verify MainActivity was started
            val shadowActivity = shadowOf(activity)
            val nextIntent = shadowActivity.nextStartedActivity
            
            assertTrue(nextIntent != null)
            assertEquals(MainActivity::class.java.name, nextIntent.component?.className)
            
            // Verify SplashActivity finished itself
            assertTrue(activity.isFinishing)
        }
    }
}
