package com.example.fanzbe.domain.chat.controller

import com.example.fanzbe.domain.chat.dto.SendMessageRequest
import com.example.fanzbe.domain.chat.service.ChatMessageService
import com.example.fanzbe.global.security.CustomUserDetails
import jakarta.validation.Valid
import java.security.Principal
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller

@Controller
class ChatWebSocketController(
    private val chatMessageService: ChatMessageService,
    private val messagingTemplate: SimpMessagingTemplate,
) {

    @MessageMapping("/chat-rooms/{roomId}")
    fun sendMessage(
        @DestinationVariable("roomId") roomId: Long,
        @Valid @Payload message: SendMessageRequest,
        principal: Principal,
    ) {
        val userId = principal.userId()
        val response = chatMessageService.sendMessage(
            roomId = roomId,
            senderUserId = userId,
            content = message.content,
            messageType = message.messageType,
            attachmentUrl = message.attachmentUrl,
        )

        messagingTemplate.convertAndSend("/topic/chat-rooms/$roomId", response)
    }

    private fun Principal.userId(): Long {
        val authentication = this as? Authentication
            ?: throw IllegalStateException("Authenticated STOMP principal is required.")
        val userDetails = authentication.principal as? CustomUserDetails
            ?: throw IllegalStateException("STOMP principal must be CustomUserDetails.")

        return userDetails.id
    }
}
