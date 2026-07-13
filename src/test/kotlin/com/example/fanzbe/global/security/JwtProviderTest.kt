package com.example.fanzbe.global.security

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JwtProviderTest {

    private val secret = "fanz-be-test-secret-key-with-at-least-256-bits-of-entropy"
    private val jwtProvider = JwtProvider(
        secret = secret,
        accessTokenExpiration = 60_000,
        refreshTokenExpiration = 120_000,
    )

    @Test
    fun `creates valid access token with user id subject`() {
        val token = jwtProvider.createAccessToken(42L)

        assertTrue(jwtProvider.validateToken(token))
        assertEquals(42L, jwtProvider.getUserId(token))
    }

    @Test
    fun `creates valid refresh token with user id subject`() {
        val token = jwtProvider.createRefreshToken(42L)

        assertTrue(jwtProvider.validateToken(token))
        assertEquals(42L, jwtProvider.getUserId(token))
    }

    @Test
    fun `invalid token is rejected`() {
        assertFalse(jwtProvider.validateToken("not-a-token"))
    }
}
