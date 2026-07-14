package com.example.fanzbe.domain.dm.controller

import com.example.fanzbe.domain.dm.dto.SendDmMessageRequest
import com.example.fanzbe.domain.dm.service.DmService
import com.example.fanzbe.global.security.CustomUserDetails
import jakarta.validation.Valid
import java.security.Principal
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller

/**
 * 실시간 DM. 클라이언트 send: /app/dm/rooms/{roomId}, subscribe: /topic/dm/rooms/{roomId}.
 * 저장/검증은 REST 와 동일한 DmService.sendMessage 재사용.
 */
@Controller
class DmWebSocketController(
    private val dmService: DmService,
    private val messagingTemplate: SimpMessagingTemplate,
) {

    @MessageMapping("/dm/rooms/{roomId}")
    fun sendMessage(
        @DestinationVariable("roomId") roomId: Long,
        @Valid @Payload message: SendDmMessageRequest,
        principal: Principal,
    ) {
        val response = dmService.sendMessage(
            roomId = roomId,
            senderId = principal.userId(),
            content = message.content,
        )
        messagingTemplate.convertAndSend("/topic/dm/rooms/$roomId", response)
    }

    private fun Principal.userId(): Long {
        val authentication = this as? Authentication
            ?: throw IllegalStateException("Authenticated STOMP principal is required.")
        val userDetails = authentication.principal as? CustomUserDetails
            ?: throw IllegalStateException("STOMP principal must be CustomUserDetails.")
        return userDetails.id
    }
}
