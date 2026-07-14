package com.example.fanzbe.domain.user.dto

data class UserSearchResponse(
    val userId: Long,
    val nickname: String,
    val handle: String?,
    val profileImageUrl: String?,
    val mannerScore: Double,
    val isFollowing: Boolean,
)
