package com.example.fanzbe.domain.auth.service

import com.example.fanzbe.domain.auth.dto.LoginRequest
import com.example.fanzbe.domain.auth.dto.ReissueRequest
import com.example.fanzbe.domain.auth.dto.SignupRequest
import com.example.fanzbe.domain.auth.dto.SignupResponse
import com.example.fanzbe.domain.auth.dto.TokenResponse
import com.example.fanzbe.domain.auth.dto.UsernameAvailabilityResponse
import com.example.fanzbe.domain.auth.entity.RefreshToken
import com.example.fanzbe.domain.auth.repository.RefreshTokenRepository
import com.example.fanzbe.domain.profile.entity.Profile
import com.example.fanzbe.domain.profile.repository.ProfileRepository
import com.example.fanzbe.domain.user.entity.User
import com.example.fanzbe.domain.user.repository.UserRepository
import com.example.fanzbe.global.exception.BusinessException
import com.example.fanzbe.global.exception.ErrorCode
import com.example.fanzbe.global.security.JwtProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider,
    @Value("\${jwt.refresh-token-expiration}")
    private val refreshTokenExpiration: Long,
) {

    @Transactional
    fun signup(request: SignupRequest): SignupResponse {
        if (request.password != request.passwordConfirm) {
            throw BusinessException(ErrorCode.PASSWORD_MISMATCH)
        }
        val username = request.username.trim()
        if (userRepository.existsByUsername(username)) {
            throw BusinessException(ErrorCode.USERNAME_DUPLICATED)
        }

        val encodedPassword = requireNotNull(passwordEncoder.encode(request.password)) {
            "Encoded password must not be null."
        }
        val user = userRepository.save(
            User(
                username = username,
                password = encodedPassword,
                nickname = request.nickname.trim(),
                ageGroup = request.ageGroup,
                gender = request.gender,
                interests = request.interests.normalizeTags(),
            ),
        )

        profileRepository.save(
            Profile(
                user = user,
                bio = request.bio.normalizeNullable(),
                profileImageUrl = request.profileImageUrl.normalizeNullable(),
                coverImageUrl = request.coverImageUrl.normalizeNullable(),
            ),
        )

        return SignupResponse(userId = requireNotNull(user.id))
    }

    @Transactional(readOnly = true)
    fun getUsernameAvailability(username: String): UsernameAvailabilityResponse {
        val normalized = username.trim()
        val available = USERNAME_PATTERN.matches(normalized) && !userRepository.existsByUsername(normalized)
        return UsernameAvailabilityResponse(username = normalized, available = available)
    }

    @Transactional
    fun login(request: LoginRequest): TokenResponse {
        val user = userRepository.findByUsername(request.username.trim())
            ?: throw BusinessException(ErrorCode.INVALID_CREDENTIALS)
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw BusinessException(ErrorCode.INVALID_CREDENTIALS)
        }

        return issueTokens(requireNotNull(user.id))
    }

    @Transactional
    fun reissue(request: ReissueRequest): TokenResponse {
        if (!jwtProvider.validateToken(request.refreshToken)) {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }
        val stored = refreshTokenRepository.findByToken(request.refreshToken)
            ?: throw BusinessException(ErrorCode.INVALID_TOKEN)
        if (stored.expiresAt.isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(stored)
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }

        return issueTokens(stored.userId)
    }

    @Transactional
    fun logout(userId: Long) {
        refreshTokenRepository.deleteByUserId(userId)
    }

    /** access/refresh 발급 + refresh 저장(유저당 1개, 회전). */
    private fun issueTokens(userId: Long): TokenResponse {
        val accessToken = jwtProvider.createAccessToken(userId)
        val refreshToken = jwtProvider.createRefreshToken(userId)
        val expiresAt = LocalDateTime.now().plusNanos(refreshTokenExpiration * 1_000_000)

        val existing = refreshTokenRepository.findByUserId(userId)
        if (existing != null) {
            existing.token = refreshToken
            existing.expiresAt = expiresAt
        } else {
            refreshTokenRepository.save(
                RefreshToken(userId = userId, token = refreshToken, expiresAt = expiresAt),
            )
        }

        return TokenResponse(accessToken = accessToken, refreshToken = refreshToken)
    }

    private fun String?.normalizeNullable(): String? =
        this?.trim()?.takeIf { it.isNotEmpty() }

    private fun List<String>.normalizeTags(): MutableSet<String> =
        mapNotNull { tag ->
            tag.trim().removePrefix("#").lowercase().takeIf(String::isNotEmpty)
        }.toMutableSet()

    companion object {
        private val USERNAME_PATTERN = Regex("^[A-Za-z0-9._-]{4,20}$")
    }
}
