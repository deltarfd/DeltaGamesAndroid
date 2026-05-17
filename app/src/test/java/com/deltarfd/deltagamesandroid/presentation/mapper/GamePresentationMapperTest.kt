package com.deltarfd.deltagamesandroid.presentation.mapper

import com.deltarfd.deltagamesandroid.core.domain.model.Game
import org.junit.Assert.assertEquals
import org.junit.Test

class GamePresentationMapperTest {

    @Test
    fun `mapDomainToPresentation should map correctly`() {
        val domain = Game(
            id = 1, slug = "game", name = "Test Game", released = "2024",
            backgroundImage = "img", rating = 4.0, ratingsCount = 10,
            metacritic = 80, playtime = 10, description = "Desc",
            genres = "Action", platforms = "PC", developers = "Dev",
            publishers = "Pub", isFavorite = true
        )

        val presentation = GamePresentationMapper.mapDomainToPresentation(domain)

        assertEquals(1, presentation.id)
        assertEquals("Test Game", presentation.name)
        assertEquals("Action", presentation.genres)
        assertEquals(true, presentation.isFavorite)
    }

    @Test
    fun `mapListDomainToPresentation should map correctly`() {
        val domains = listOf(
            Game(
                id = 1, slug = "game", name = "Test Game", released = "2024",
                backgroundImage = "img", rating = 4.0, ratingsCount = 10,
                metacritic = 80, playtime = 10, description = "Desc",
                genres = "Action", platforms = "PC", developers = "Dev",
                publishers = "Pub", isFavorite = false
            )
        )

        val presentations = GamePresentationMapper.mapListDomainToPresentation(domains)

        assertEquals(1, presentations.size)
        assertEquals("Test Game", presentations[0].name)
    }

    @Test
    fun `mapPresentationToDomain should map correctly`() {
        val presentation = com.deltarfd.deltagamesandroid.presentation.model.GameItem(
            id = 2, name = "Pres Game", slug = "slug", releaseDate = "2025", coverUrl = "bg",
            rating = 5.0, ratingDisplay = "5.0", genres = "RPG", platforms = "PC", playtime = 20, metacritic = 90, description = "Desc", developers = "Dev", publishers = "Pub", isFavorite = false
        )

        val domain = GamePresentationMapper.mapPresentationToDomain(presentation)

        assertEquals(2, domain.id)
        assertEquals("Pres Game", domain.name)
        assertEquals("RPG", domain.genres)
        assertEquals(false, domain.isFavorite)
    }
}
