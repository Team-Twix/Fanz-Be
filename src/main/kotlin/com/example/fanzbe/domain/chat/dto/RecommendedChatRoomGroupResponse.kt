package com.example.fanzbe.domain.chat.dto

data class RecommendedChatRoomGroupResponse(
    val interest: String,
    val rooms: List<ChatRoomResponse>,
)
