package com.deltarfd.deltagamesandroid.core.utils

import com.deltarfd.deltagamesandroid.core.data.local.entity.GameEntity
import com.deltarfd.deltagamesandroid.core.data.remote.response.GameDetailResponse
import com.deltarfd.deltagamesandroid.core.data.remote.response.GameResponse
import com.deltarfd.deltagamesandroid.core.data.remote.response.GenreResponse
import com.deltarfd.deltagamesandroid.core.data.remote.response.PlatformWrapper
import com.deltarfd.deltagamesandroid.core.data.remote.response.PlatformResponse
import com.deltarfd.deltagamesandroid.core.data.remote.response.DeveloperResponse
import com.deltarfd.deltagamesandroid.core.data.remote.response.PublisherResponse
import com.deltarfd.deltagamesandroid.core.domain.model.Game
import org.junit.Assert.assertEquals
import org.junit.Test

class DataMapperTest {

    @Test
    fun `mapResponseToEntities should map correctly`() {
        val responses = listOf(
            GameResponse(
                id = 1,
                slug = "test-slug",
                name = "Test Game",
                released = "2023-01-01",
                backgroundImage = "img.jpg",
                rating = 4.5,
                ratingsCount = 100,
                metacritic = 90,
                playtime = 10,
                genres = listOf(GenreResponse(1, "Action", "action")),
                platforms = listOf(PlatformWrapper(PlatformResponse(1, "PC", "pc"))),
                tags = emptyList(),
                screenshots = emptyList()
            )
        )
        
        val entities = DataMapper.mapResponseToEntities(responses)
        
        assertEquals(1, entities.size)
        val entity = entities[0]
        assertEquals(1, entity.id)
        assertEquals("Test Game", entity.name)
        assertEquals("Action", entity.genres)
        assertEquals("PC", entity.platforms)
        assertEquals(false, entity.isFavorite)
        assertEquals(null, entity.description)
        assertEquals(null, entity.developers)
        assertEquals(null, entity.publishers)
    }

    @Test
    fun `mapDetailResponseToEntity should map correctly`() {
        val response = GameDetailResponse(
            id = 2,
            slug = "test-slug-2",
            name = "Test Detail",
            released = "2023-02-02",
            backgroundImage = "img2.jpg",
            rating = 4.8,
            ratingsCount = 200,
            metacritic = 95,
            playtime = 20,
            descriptionRaw = "A detailed description",
            genres = listOf(GenreResponse(2, "RPG", "rpg")),
            platforms = listOf(PlatformWrapper(PlatformResponse(2, "PS5", "ps5"))),
            developers = listOf(DeveloperResponse(1, "Dev")),
            publishers = listOf(PublisherResponse(1, "Pub")),
            website = "url",
            tags = emptyList()
        )

        val entity = DataMapper.mapDetailResponseToEntity(response, isFavorite = true)

        assertEquals(2, entity.id)
        assertEquals("Test Detail", entity.name)
        assertEquals("A detailed description", entity.description)
        assertEquals("RPG", entity.genres)
        assertEquals("PS5", entity.platforms)
        assertEquals("Dev", entity.developers)
        assertEquals("Pub", entity.publishers)
        assertEquals(true, entity.isFavorite)
    }

    @Test
    fun `mapDetailResponseToEntity default argument isFavorite`() {
        val response = GameDetailResponse(
            id = 3, slug = "s", name = "N", released = "R", backgroundImage = "bg",
            rating = 1.0, ratingsCount = 1, metacritic = null, playtime = 1,
            descriptionRaw = null, genres = null, platforms = null, developers = null,
            publishers = null, website = null, tags = null
        )
        val entity = DataMapper.mapDetailResponseToEntity(response)
        assertEquals(false, entity.isFavorite)
    }

    @Test
    fun `mapEntitiesToDomain should map correctly`() {
        val entities = listOf(
            GameEntity(
                id = 1,
                slug = "slug",
                name = "name",
                released = null,
                backgroundImage = null,
                rating = 4.0,
                ratingsCount = 10,
                metacritic = null,
                playtime = 5,
                description = null,
                genres = null,
                platforms = null,
                developers = null,
                publishers = null,
                isFavorite = true
            )
        )

        val domainList = DataMapper.mapEntitiesToDomain(entities)

        assertEquals(1, domainList.size)
        val domain = domainList[0]
        assertEquals(1, domain.id)
        assertEquals("N/A", domain.released) // Tests the fallback
        assertEquals("", domain.backgroundImage) // Tests the fallback
        assertEquals(0, domain.metacritic) // Tests the fallback
        assertEquals("", domain.description)
        assertEquals("", domain.genres)
        assertEquals("", domain.platforms)
        assertEquals("", domain.developers)
        assertEquals("", domain.publishers)
        assertEquals(true, domain.isFavorite)
    }

    @Test
    fun `mapDomainToEntity should map correctly`() {
        val domain = Game(
            id = 3,
            slug = "domain-slug",
            name = "Domain Game",
            released = "2024",
            backgroundImage = "bg.png",
            rating = 5.0,
            ratingsCount = 500,
            metacritic = 100,
            playtime = 50,
            description = "Desc",
            genres = "Action",
            platforms = "PC",
            developers = "Devs",
            publishers = "Pubs",
            isFavorite = false
        )

        val entity = DataMapper.mapDomainToEntity(domain)

        assertEquals(3, entity.id)
        assertEquals("domain-slug", entity.slug)
        assertEquals("Domain Game", entity.name)
        assertEquals(false, entity.isFavorite)
    }
}
