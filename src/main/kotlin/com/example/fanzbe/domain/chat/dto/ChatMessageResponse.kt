package com.example.fanzbe.domain.chat.dto

import com.example.fanzbe.domain.chat.entity.MessageType
import java.time.LocalDateTime

data class ChatMessageResponse(
    val id: Long,
    val roomId: Long,
    val senderId: Long,
    val senderNickname: String,
    val senderProfileImageUrl: String?,
    val content: String,
    val messageType: MessageType,
    val attachmentUrl: String?,
    val createdAt: LocalDateTime,
)
