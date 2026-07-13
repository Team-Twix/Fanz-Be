package com.example.fanzbe.domain.chat.dto

data class ChatRoomResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val category: String,
    val hostId: Long,
    val memberCount: Long,
)
