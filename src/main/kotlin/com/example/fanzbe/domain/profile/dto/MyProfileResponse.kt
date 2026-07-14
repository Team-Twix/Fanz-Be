package com.example.fanzbe.domain.profile.dto

data class MyProfileResponse(
    val userId: Long,
    val nickname: String,
    val handle: String?,
    val bio: String?,
    val profileImageUrl: String?,
    val coverImageUrl: String?,
    val followerCount: Long,
    val followingCount: Long,
    val joinedChatRooms: List<JoinedChatRoomResponse>,
)
