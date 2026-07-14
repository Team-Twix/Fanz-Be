package com.example.fanzbe.domain.recommendation.ai

import com.example.fanzbe.domain.user.entity.AgeGroup

data class RankInput(
    val userId: Long,
    val nickname: String,
    val interests: Set<String>,
    val ageGroup: AgeGroup?,
    val mannerScore: Double,
)
