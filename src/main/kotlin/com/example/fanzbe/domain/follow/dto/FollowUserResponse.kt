package com.example.fanzbe.domain.follow.dto

/**
 * 팔로워/팔로잉 목록의 유저 항목.
 * isFollowing = 현재 로그인 유저가 이 유저를 팔로우 중인지 (팔로우 버튼 상태).
 */
data class FollowUserResponse(
    val userId: Long,
    val nickname: String,
    val handle: String?,
    val profileImageUrl: String?,
    val isFollowing: Boolean,
)
