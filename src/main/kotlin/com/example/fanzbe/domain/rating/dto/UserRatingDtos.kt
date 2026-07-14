package com.example.fanzbe.domain.rating.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class RateUserRequest(
    @field:NotNull
    val chatRoomId: Long,

    @field:Min(1)
    @field:Max(5)
    val score: Int,
)

data class RatingSummaryResponse(
    val userId: Long,
    val averageRating: Double?,
    val ratingCount: Long,
    val mannerScore: Double,
)
