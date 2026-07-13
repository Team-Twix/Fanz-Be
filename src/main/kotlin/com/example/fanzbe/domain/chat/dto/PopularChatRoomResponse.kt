package com.example.fanzbe.domain.chat.dto

data class PopularChatRoomResponse(
    val id: Long,
    val name: String,
    val imageUrl: String?,
    val category: String,
    val memberCount: Long,
    val monthlyMessageCount: Long,
    val activityRate: Double,
)
