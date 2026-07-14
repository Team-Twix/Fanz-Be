package com.example.fanzbe.domain.chat.dto

import com.example.fanzbe.domain.chat.entity.MessageType
import java.time.LocalDateTime

data class MyChatRoomResponse(
    val id: Long,
    val name: String,
    val imageUrl: String?,
    val summary: String?,
    val hashtags: List<String>,
    val memberCount: Long,
    val lastMessage: String?,
    val lastMessageType: MessageType?,
    val lastMessageAt: LocalDateTime?,
    val unreadCount: Long,
)
