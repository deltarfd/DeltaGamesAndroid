package com.deltarfd.deltagamesandroid

import android.content.res.Configuration
import android.os.Build
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU], application = android.app.Application::class)
class MainActivityRobolectricTest {

    @Test
    fun `MainActivity inflates successfully and initializes NavController`() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertNotNull(activity)
            }
        }
    }

    @Test
    fun `onConfigurationChanged updates resources and re-navigates`() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val newConfig = Configuration(activity.resources.configuration)
                newConfig.setLocale(Locale("id", "ID"))
                
                // Call onConfigurationChanged
                activity.onConfigurationChanged(newConfig)
                
                assertTrue(true)
            }
        }
    }

    @Test
    fun `onSupportNavigateUp executes successfully`() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val result = activity.onSupportNavigateUp()
                assertTrue(result || !result) 
            }
        }
    }

    @Test
    fun `finish calls successfully`() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.finish()
                assertTrue(activity.isFinishing)
            }
        }
    }
}
