package com.example.fanzbe.global.security

import com.example.fanzbe.domain.user.entity.User
import com.example.fanzbe.domain.user.repository.UserRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.MessageDeliveryException
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ExecutorSubscribableChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.core.Authentication
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StompAuthChannelInterceptorTest(
    @Autowired private val jwtProvider: JwtProvider,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val interceptor: StompAuthChannelInterceptor,
) {

    private val channel = ExecutorSubscribableChannel()

    @Test
    fun `CONNECT with valid bearer token sets authenticated user`() {
        val user = userRepository.save(
            User(email = "stomp-user@fanz.com", password = "encoded", nickname = "stomp"),
        )
        val token = jwtProvider.createAccessToken(user.id!!)
        val message = connectMessage("Bearer $token")

        val result = assertNotNull(interceptor.preSend(message, channel))
        val accessor = assertNotNull(MessageHeaderAccessor.getAccessor(result, StompHeaderAccessor::class.java))
        val authentication = assertIs<Authentication>(accessor.user)
        val principal = assertIs<CustomUserDetails>(authentication.principal)

        assertEquals(user.id, principal.id)
        assertEquals("stomp-user@fanz.com", principal.username)
    }

    @Test
    fun `CONNECT without bearer token is rejected`() {
        val message = connectMessage(authorization = null)

        assertFailsWith<MessageDeliveryException> {
            interceptor.preSend(message, channel)
        }
    }

    private fun connectMessage(authorization: String?): Message<ByteArray> {
        val accessor = StompHeaderAccessor.create(StompCommand.CONNECT)
        // 실제 STOMP inbound 채널처럼 헤더를 mutable 로 유지해야 인터셉터가 setUser 할 수 있다.
        accessor.setLeaveMutable(true)
        if (authorization != null) {
            accessor.setNativeHeader(HttpHeaders.AUTHORIZATION, authorization)
        }

        return MessageBuilder.createMessage(ByteArray(0), accessor.messageHeaders)
    }
}
