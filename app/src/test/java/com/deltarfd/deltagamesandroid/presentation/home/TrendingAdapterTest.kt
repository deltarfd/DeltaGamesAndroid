package com.deltarfd.deltagamesandroid.presentation.home

import org.junit.Assert.assertEquals
import org.junit.Test

class TrendingAdapterTest {

    private fun platforms(vararg names: String) = names.joinToString(", ")

    // ── blank / empty input ───────────────────────────────────────────────

    @Test
    fun `formatPlatforms returns empty string for blank input`() {
        assertEquals("", TrendingAdapter.formatPlatforms(""))
    }

    @Test
    fun `formatPlatforms returns empty string for whitespace-only input`() {
        assertEquals("", TrendingAdapter.formatPlatforms("   "))
    }

    // ── single known platforms ────────────────────────────────────────────

    @Test
    fun `formatPlatforms maps PlayStation 5 to PlayStation`() {
        assertEquals("PlayStation", TrendingAdapter.formatPlatforms("PlayStation 5"))
    }

    @Test
    fun `formatPlatforms maps PlayStation 4 to PlayStation`() {
        assertEquals("PlayStation", TrendingAdapter.formatPlatforms("PlayStation 4"))
    }

    @Test
    fun `formatPlatforms maps Xbox One to Xbox`() {
        assertEquals("Xbox", TrendingAdapter.formatPlatforms("Xbox One"))
    }

    @Test
    fun `formatPlatforms maps Xbox Series X to Xbox`() {
        assertEquals("Xbox", TrendingAdapter.formatPlatforms("Xbox Series X"))
    }

    @Test
    fun `formatPlatforms maps Nintendo Switch to Nintendo`() {
        assertEquals("Nintendo", TrendingAdapter.formatPlatforms("Nintendo Switch"))
    }

    @Test
    fun `formatPlatforms maps PC to PC`() {
        assertEquals("PC", TrendingAdapter.formatPlatforms("PC"))
    }

    @Test
    fun `formatPlatforms maps Windows to PC`() {
        assertEquals("PC", TrendingAdapter.formatPlatforms("Windows"))
    }

    @Test
    fun `formatPlatforms maps macOS to macOS`() {
        assertEquals("macOS", TrendingAdapter.formatPlatforms("macOS"))
    }

    @Test
    fun `formatPlatforms maps Mac to macOS`() {
        assertEquals("macOS", TrendingAdapter.formatPlatforms("Mac"))
    }

    @Test
    fun `formatPlatforms maps Linux to Linux`() {
        assertEquals("Linux", TrendingAdapter.formatPlatforms("Linux"))
    }

    @Test
    fun `formatPlatforms maps Android to Android`() {
        assertEquals("Android", TrendingAdapter.formatPlatforms("Android"))
    }

    @Test
    fun `formatPlatforms maps iOS to iOS`() {
        assertEquals("iOS", TrendingAdapter.formatPlatforms("iOS"))
    }

    @Test
    fun `formatPlatforms keeps unknown platform name unchanged`() {
        assertEquals("Dreamcast", TrendingAdapter.formatPlatforms("Dreamcast"))
    }

    // ── multiple platforms ────────────────────────────────────────────────

    @Test
    fun `formatPlatforms joins multiple platforms with comma space`() {
        val result = TrendingAdapter.formatPlatforms(platforms("PlayStation 5", "Xbox One", "PC"))
        assertEquals("PlayStation, Xbox, PC", result)
    }

    @Test
    fun `formatPlatforms deduplicates same category platforms`() {
        // PS4 and PS5 should both map to "PlayStation" and be deduped
        val result = TrendingAdapter.formatPlatforms(platforms("PlayStation 4", "PlayStation 5"))
        assertEquals("PlayStation", result)
    }

    @Test
    fun `formatPlatforms deduplicates PC and Windows to single PC`() {
        val result = TrendingAdapter.formatPlatforms(platforms("PC", "Windows"))
        assertEquals("PC", result)
    }

    @Test
    fun `formatPlatforms limits output to 4 platforms`() {
        val input = platforms("PlayStation 5", "Xbox One", "PC", "Nintendo Switch", "Android", "iOS")
        val result = TrendingAdapter.formatPlatforms(input)
        val count = result.split(",").size
        assertEquals(4, count)
    }

    @Test
    fun `formatPlatforms handles mixed case for PlayStation`() {
        assertEquals("PlayStation", TrendingAdapter.formatPlatforms("PLAYSTATION 4"))
    }

    @Test
    fun `formatPlatforms handles mixed case for Xbox`() {
        assertEquals("Xbox", TrendingAdapter.formatPlatforms("XBOX ONE"))
    }

    @Test
    fun `formatPlatforms trims whitespace around platform names`() {
        val result = TrendingAdapter.formatPlatforms("  PlayStation 5  ,  PC  ")
        assertEquals("PlayStation, PC", result)
    }

    @Test
    fun `formatPlatforms handles single platform with no comma`() {
        assertEquals("Nintendo", TrendingAdapter.formatPlatforms("Nintendo Switch"))
    }
}
