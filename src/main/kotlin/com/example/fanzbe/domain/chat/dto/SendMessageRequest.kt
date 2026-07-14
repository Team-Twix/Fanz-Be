package com.example.fanzbe.domain.chat.dto

import com.example.fanzbe.domain.chat.entity.MessageType
import jakarta.validation.constraints.Size

data class SendMessageRequest(

    @field:Size(max = 2000)
    val content: String? = null,

    val messageType: MessageType = MessageType.TEXT,

    @field:Size(max = 2048)
    val attachmentUrl: String? = null,
)
