package com.example.fanzbe.domain.profile.dto

data class JoinedChatRoomResponse(
    val id: Long,
    val name: String,
    val imageUrl: String?,
    val description: String?,
)
