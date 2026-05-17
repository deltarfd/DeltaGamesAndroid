package com.deltarfd.deltagamesandroid.core.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResourceTest {

    @Test
    fun `Resource Loading should hold optional data`() {
        val loadingWithData = Resource.Loading("Test")
        assertEquals("Test", loadingWithData.data)
        assertNull(loadingWithData.message)

        val loadingWithoutData = Resource.Loading<String>()
        assertNull(loadingWithoutData.data)
    }

    @Test
    fun `Resource Success should hold data`() {
        val success = Resource.Success("SuccessData")
        assertEquals("SuccessData", success.data)
        assertNull(success.message)
    }

    @Test
    fun `Resource Error should hold message and optional data`() {
        val errorWithData = Resource.Error("Error Message", "FallbackData")
        assertEquals("FallbackData", errorWithData.data)
        assertEquals("Error Message", errorWithData.message)

        val errorWithoutData = Resource.Error<String>("Error Only")
        assertNull(errorWithoutData.data)
        assertEquals("Error Only", errorWithoutData.message)
    }
}
