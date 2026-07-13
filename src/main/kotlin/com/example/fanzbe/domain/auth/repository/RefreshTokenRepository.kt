package com.example.fanzbe.domain.auth.repository

import com.example.fanzbe.domain.auth.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByToken(token: String): RefreshToken?

    fun findByUserId(userId: Long): RefreshToken?

    fun deleteByUserId(userId: Long)
}
