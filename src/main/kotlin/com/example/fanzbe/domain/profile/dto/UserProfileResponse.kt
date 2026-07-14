package com.example.fanzbe.domain.profile.dto

import com.example.fanzbe.domain.user.entity.AgeGroup
import com.example.fanzbe.domain.user.entity.UserGender

/**
 * 타 유저 프로필 조회 응답 (마이프로필 + isFollowing).
 */
data class UserProfileResponse(
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
    // 현재 로그인 유저가 이 유저를 팔로우 중인지
    val isFollowing: Boolean,
)
