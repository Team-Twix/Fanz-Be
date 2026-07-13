package com.example.fanzbe.domain.auth.entity

import com.example.fanzbe.global.common.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 * 리프레시 토큰 (유저당 1개, 재발급 시 회전).
 * Redis 미도입이라 DB 저장.
 */
@Entity
@Table(name = "refresh_tokens")
open class RefreshToken(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @Column(name = "user_id", nullable = false, unique = true)
    open var userId: Long,

    @Column(nullable = false, length = 512)
    open var token: String,

    @Column(nullable = false)
    open var expiresAt: LocalDateTime,
) : BaseTimeEntity()
