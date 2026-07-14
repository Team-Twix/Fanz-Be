package com.example.fanzbe.domain.recommendation.dto

data class RecommendedMateResponse(
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val sharedHashtags: List<String>,
    val mannerScore: Double,
)
