package com.example.fanzbe.domain.dm.controller

import com.example.fanzbe.domain.chat.dto.SendMessageRequest
import com.example.fanzbe.domain.dm.service.DirectChatService
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
class DirectChatWebSocketController(
    private val directChatService: DirectChatService,
    private val messagingTemplate: SimpMessagingTemplate,
) {

    @MessageMapping("/dm-rooms/{roomId}")
    fun sendMessage(
        @DestinationVariable("roomId") roomId: Long,
        @Valid @Payload message: SendMessageRequest,
        principal: Principal,
    ) {
        val authentication = principal as? Authentication
            ?: throw IllegalStateException("Authenticated STOMP principal is required.")
        val userDetails = authentication.principal as? CustomUserDetails
            ?: throw IllegalStateException("STOMP principal must be CustomUserDetails.")
        val response = directChatService.sendMessage(roomId, userDetails.id, message)
        messagingTemplate.convertAndSend("/topic/dm-rooms/$roomId", response)
    }
}
