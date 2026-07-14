package com.example.fanzbe.global.security

import org.springframework.http.HttpHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageDeliveryException
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component

@Component
class StompAuthChannelInterceptor(
    private val jwtProvider: JwtProvider,
    private val customUserDetailsService: CustomUserDetailsService,
) : ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
            ?: return message

        if (accessor.command != StompCommand.CONNECT) {
            return message
        }

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
    }
}
