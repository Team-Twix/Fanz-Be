package com.example.fanzbe.domain.chat.dto

import java.time.LocalDateTime

data class ChatMessageResponse(
    val id: Long,
    val roomId: Long,
    val senderId: Long,
    val content: String,
    val createdAt: LocalDateTime,
)
