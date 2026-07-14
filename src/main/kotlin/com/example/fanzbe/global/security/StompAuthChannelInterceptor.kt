package com.example.fanzbe.global.security

import com.example.fanzbe.domain.chat.repository.ChatRoomMemberRepository
import com.example.fanzbe.domain.dm.repository.DirectChatRoomMemberRepository
import org.springframework.http.HttpHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageDeliveryException
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component

@Component
class StompAuthChannelInterceptor(
    private val jwtProvider: JwtProvider,
    private val customUserDetailsService: CustomUserDetailsService,
    private val chatRoomMemberRepository: ChatRoomMemberRepository,
    private val directChatRoomMemberRepository: DirectChatRoomMemberRepository,
) : ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
            ?: return message

        return when (accessor.command) {
            StompCommand.CONNECT -> authenticateConnect(message, accessor)
            StompCommand.SUBSCRIBE -> authorizeSubscription(message, accessor)
            StompCommand.SEND -> authorizeSend(message, accessor)
            else -> message
        }
    }

    private fun authenticateConnect(
        message: Message<*>,
        accessor: StompHeaderAccessor,
    ): Message<*> {
        val token = resolveToken(accessor)
            ?: reject(message, "Missing STOMP Authorization bearer token.")

        if (!jwtProvider.validateToken(token)) {
            reject(message, "Invalid STOMP Authorization bearer token.")
        }

        val userId = runCatching { jwtProvider.getUserId(token) }
            .getOrElse { reject(message, "Invalid STOMP token subject.", it) }
        val userDetails = try {
            customUserDetailsService.loadUserById(userId)
        } catch (exception: UsernameNotFoundException) {
            reject(message, "STOMP token user was not found.", exception)
        }

        accessor.user = UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.authorities,
        )

        return message
    }

    private fun authorizeSubscription(
        message: Message<*>,
        accessor: StompHeaderAccessor,
    ): Message<*> {
        val authentication = accessor.user as? Authentication
            ?: reject(message, "Authenticated STOMP principal is required for subscription.")
        val userDetails = authentication.principal as? CustomUserDetails
            ?: reject(message, "Invalid STOMP principal for subscription.")
        val destination = accessor.destination
            ?: reject(message, "STOMP subscription destination is required.")

        val allowed = CHAT_ROOM_TOPIC.matchEntire(destination)?.groupValues?.get(1)?.toLongOrNull()?.let { roomId ->
            chatRoomMemberRepository.existsByChatRoomIdAndUserId(roomId, userDetails.id)
        } ?: DM_ROOM_TOPIC.matchEntire(destination)?.groupValues?.get(1)?.toLongOrNull()?.let { roomId ->
            directChatRoomMemberRepository.existsByDirectChatRoomIdAndUserId(roomId, userDetails.id)
        } ?: false

        if (!allowed) {
            reject(message, "STOMP topic subscription is not allowed.")
        }
        return message
    }

    private fun authorizeSend(
        message: Message<*>,
        accessor: StompHeaderAccessor,
    ): Message<*> {
        val userDetails = authenticatedUser(message, accessor)
        val destination = accessor.destination
            ?: reject(message, "STOMP send destination is required.")
        val allowed = APP_CHAT_ROOM.matchEntire(destination)?.roomId()?.let { roomId ->
            chatRoomMemberRepository.existsByChatRoomIdAndUserId(roomId, userDetails.id)
        } ?: APP_DM_ROOM.matchEntire(destination)?.roomId()?.let { roomId ->
            directChatRoomMemberRepository.existsByDirectChatRoomIdAndUserId(roomId, userDetails.id)
        } ?: false

        if (!allowed) {
            reject(message, "STOMP application send is not allowed.")
        }
        return message
    }

    private fun authenticatedUser(message: Message<*>, accessor: StompHeaderAccessor): CustomUserDetails {
        val authentication = accessor.user as? Authentication
            ?: reject(message, "Authenticated STOMP principal is required.")
        return authentication.principal as? CustomUserDetails
            ?: reject(message, "Invalid STOMP principal.")
    }

    private fun MatchResult.roomId(): Long? =
        groupValues.getOrNull(1)?.toLongOrNull()

    private fun resolveToken(accessor: StompHeaderAccessor): String? {
        val authorization = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION)
            ?: return null

        return authorization
            .takeIf { it.startsWith(BEARER_PREFIX) }
            ?.substring(BEARER_PREFIX.length)
            ?.takeIf { it.isNotBlank() }
    }

    private fun reject(message: Message<*>, description: String, cause: Throwable? = null): Nothing {
        if (cause == null) {
            throw MessageDeliveryException(message, description)
        }

        throw MessageDeliveryException(message, description, cause)
    }

    companion object {
        private const val BEARER_PREFIX = "Bearer "
        private val CHAT_ROOM_TOPIC = Regex("^/topic/chat-rooms/(\\d+)$")
        private val DM_ROOM_TOPIC = Regex("^/topic/dm-rooms/(\\d+)$")
        private val APP_CHAT_ROOM = Regex("^/app/chat-rooms/(\\d+)$")
        private val APP_DM_ROOM = Regex("^/app/dm-rooms/(\\d+)$")
    }
}
