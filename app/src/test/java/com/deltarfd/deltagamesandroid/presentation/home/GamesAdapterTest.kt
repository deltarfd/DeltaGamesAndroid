package com.deltarfd.deltagamesandroid.presentation.home

import android.content.Context
import android.os.Build
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.deltarfd.deltagamesandroid.presentation.model.GameItem
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU], application = android.app.Application::class)
class GamesAdapterTest {

    private lateinit var adapter: GamesAdapter
    private lateinit var context: Context

    @Before
    fun setup() {
        adapter = GamesAdapter { }
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        context = androidx.appcompat.view.ContextThemeWrapper(appContext, com.deltarfd.deltagamesandroid.R.style.Theme_DeltaGames)
    }

    @Test
    fun `getItemCount returns correct size`() {
        val games = listOf(
            GameItem(
                id = 1, slug = "game-1", name = "Zelda", releaseDate = "2023",
                coverUrl = "url", rating = 5.0, ratingDisplay = "5.0/5", genres = "Action",
                platforms = "Nintendo", playtime = 20, metacritic = 90, description = "Desc",
                developers = "Nintendo", publishers = "Nintendo", isFavorite = false
            ),
            GameItem(
                id = 2, slug = "game-2", name = "Mario", releaseDate = "2023",
                coverUrl = "url", rating = 4.5, ratingDisplay = "4.5/5", genres = "Platformer",
                platforms = "Nintendo", playtime = 10, metacritic = 85, description = "Desc",
                developers = "Nintendo", publishers = "Nintendo", isFavorite = false
            )
        )
        adapter.submitList(games)
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `onCreateViewHolder creates view holder successfully`() {
        val parent = FrameLayout(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)
        assertEquals(GamesAdapter.GameViewHolder::class.java, viewHolder.javaClass)
    }

    @Test
    fun `onBindViewHolder binds data correctly without crashing`() {
        val games = listOf(
            GameItem(
                id = 1, slug = "game-1", name = "Zelda", releaseDate = "2023",
                coverUrl = "url", rating = 5.0, ratingDisplay = "5.0/5", genres = "Action",
                platforms = "Nintendo", playtime = 20, metacritic = 90, description = "Desc",
                developers = "Nintendo", publishers = "Nintendo", isFavorite = false
            )
        )
        adapter.submitList(games)
        
        val parent = FrameLayout(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)
        
        // This will execute the binding logic including Glide
        adapter.onBindViewHolder(viewHolder, 0)
    }
}
