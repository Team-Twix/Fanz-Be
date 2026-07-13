package com.example.fanzbe.domain.auth.dto

import jakarta.validation.constraints.NotBlank

data class SignupResponse(
    val userId: Long,
)

data class LoginRequest(
    @field:NotBlank
    val username: String,

    @field:NotBlank
    val password: String,
)

data class ReissueRequest(
    @field:NotBlank
    val refreshToken: String,
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
)
