package com.example.fanzbe.global.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtProvider(
    @Value("\${jwt.secret}")
    private val secret: String,

    @Value("\${jwt.access-token-expiration}")
    private val accessTokenExpiration: Long,

    @Value("\${jwt.refresh-token-expiration}")
    private val refreshTokenExpiration: Long,
) {
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))
    }

    fun createAccessToken(userId: Long): String =
        createToken(userId, accessTokenExpiration)

    fun createRefreshToken(userId: Long): String =
        createToken(userId, refreshTokenExpiration)

    fun validateToken(token: String): Boolean =
        try {
            parseClaims(token)
            true
        } catch (exception: JwtException) {
            false
        } catch (exception: IllegalArgumentException) {
            false
        }

    fun getUserId(token: String): Long =
        parseClaims(token).subject.toLong()

    private fun createToken(userId: Long, expirationMillis: Long): String {
        val now = Date()
        val expiration = Date(now.time + expirationMillis)

        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(now)
            .expiration(expiration)
            .signWith(key, Jwts.SIG.HS256)
            .compact()
    }

    private fun parseClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
}
