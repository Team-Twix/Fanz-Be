package com.example.fanzbe.global.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ApiResponseTest {

    @Test
    fun `success creates successful response with data`() {
        val response = ApiResponse.success("created")

        assertTrue(response.success)
        assertEquals("created", response.data)
        assertNull(response.message)
        assertNull(response.errorCode)
    }

    @Test
    fun `failure creates error response with code and message`() {
        val response = ApiResponse.failure("COMMON_001", "Invalid input")

        assertFalse(response.success)
        assertNull(response.data)
        assertEquals("Invalid input", response.message)
        assertEquals("COMMON_001", response.errorCode)
    }
}
