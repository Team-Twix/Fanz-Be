package com.example.fanzbe.domain.chat.dto

import java.time.LocalDateTime

data class ChatRoomMemberResponse(
    val userId: Long,
    val nickname: String,
    val handle: String?,
    val profileImageUrl: String?,
    val interests: List<String>,
    val mannerScore: Double,
    val isHost: Boolean,
    val joinedAt: LocalDateTime,
)
