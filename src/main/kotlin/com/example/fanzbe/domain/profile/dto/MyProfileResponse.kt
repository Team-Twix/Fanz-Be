package com.example.fanzbe.domain.profile.dto

import com.example.fanzbe.domain.user.entity.AgeGroup
import com.example.fanzbe.domain.user.entity.UserGender

data class MyProfileResponse(
    val userId: Long,
    val nickname: String,
    val handle: String?,
    val bio: String?,
    val profileImageUrl: String?,
    val coverImageUrl: String?,
    val ageGroup: AgeGroup?,
    val gender: UserGender,
    val interests: List<String>,
    val mannerScore: Double,
    val averageRating: Double?,
    val ratingCount: Long,
    val followerCount: Long,
    val followingCount: Long,
    val joinedChatRooms: List<JoinedChatRoomResponse>,
)
