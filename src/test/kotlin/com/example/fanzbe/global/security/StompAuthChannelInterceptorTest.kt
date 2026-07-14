package com.example.fanzbe.global.security

import com.example.fanzbe.domain.chat.entity.ChatRoom
import com.example.fanzbe.domain.chat.entity.ChatRoomMember
import com.example.fanzbe.domain.chat.repository.ChatRoomMemberRepository
import com.example.fanzbe.domain.chat.repository.ChatRoomRepository
import com.example.fanzbe.domain.dm.service.DirectChatService
import com.example.fanzbe.domain.dm.service.DmService
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StompAuthChannelInterceptorTest(
    @Autowired private val jwtProvider: JwtProvider,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val interceptor: StompAuthChannelInterceptor,
    @Autowired private val chatRoomRepository: ChatRoomRepository,
    @Autowired private val chatRoomMemberRepository: ChatRoomMemberRepository,
    @Autowired private val directChatService: DirectChatService,
    @Autowired private val dmService: DmService,
) {

    private val channel = ExecutorSubscribableChannel()

    @Test
    fun `CONNECT with valid bearer token sets authenticated user`() {
        val user = userRepository.save(
            User(username = "stomp-user", password = "encoded", nickname = "stomp"),
        )
        val token = jwtProvider.createAccessToken(user.id!!)
        val message = connectMessage("Bearer $token")

        val result = assertNotNull(interceptor.preSend(message, channel))
        val accessor = assertNotNull(MessageHeaderAccessor.getAccessor(result, StompHeaderAccessor::class.java))
        val authentication = assertIs<Authentication>(accessor.user)
        val principal = assertIs<CustomUserDetails>(authentication.principal)

        assertEquals(user.id, principal.id)
        assertEquals("stomp-user", principal.username)
    }

    @Test
    fun `CONNECT without bearer token is rejected`() {
        val message = connectMessage(authorization = null)

        assertFailsWith<MessageDeliveryException> {
            interceptor.preSend(message, channel)
        }
    }

    @Test
    fun `단체 채팅 멤버만 topic을 구독할 수 있다`() {
        val member = createUser("subscribe-member")
        val outsider = createUser("subscribe-outsider")
        val room = chatRoomRepository.save(ChatRoom(name = "subscribe room", host = member))
        chatRoomMemberRepository.save(ChatRoomMember(chatRoom = room, user = member))

        val allowed = subscribeMessage("/topic/chat-rooms/${room.id}", member)
        assertNotNull(interceptor.preSend(allowed, channel))

        val denied = subscribeMessage("/topic/chat-rooms/${room.id}", outsider)
        assertFailsWith<MessageDeliveryException> {
            interceptor.preSend(denied, channel)
        }
    }

    @Test
    fun `DM 멤버만 topic을 구독할 수 있다`() {
        val first = createUser("dm-subscribe-first")
        val second = createUser("dm-subscribe-second")
        val outsider = createUser("dm-subscribe-outsider")
        val room = directChatService.openRoom(first.id!!, second.id!!)

        assertNotNull(
            interceptor.preSend(
                subscribeMessage("/topic/dm-rooms/${room.id}", second),
                channel,
            ),
        )
        assertFailsWith<MessageDeliveryException> {
            interceptor.preSend(subscribeMessage("/topic/dm-rooms/${room.id}", outsider), channel)
        }
    }

    @Test
    fun `기존 DM 경로도 대화 참여자만 구독하고 전송할 수 있다`() {
        val first = createUser("legacy-dm-first")
        val second = createUser("legacy-dm-second")
        val outsider = createUser("legacy-dm-outsider")
        val roomId = dmService.getOrCreateRoom(first.id!!, second.id!!).roomId

        assertNotNull(
            interceptor.preSend(
                subscribeMessage("/topic/dm/rooms/$roomId", second),
                channel,
            ),
        )
        assertNotNull(
            interceptor.preSend(
                stompMessage(StompCommand.SEND, "/app/dm/rooms/$roomId", first),
                channel,
            ),
        )
        assertFailsWith<MessageDeliveryException> {
            interceptor.preSend(subscribeMessage("/topic/dm/rooms/$roomId", outsider), channel)
        }
    }

    @Test
    fun `멤버만 app destination으로 전송할 수 있고 topic 직접 전송은 거부한다`() {
        val member = createUser("send-member")
        val outsider = createUser("send-outsider")
        val room = chatRoomRepository.save(ChatRoom(name = "send room", host = member))
        chatRoomMemberRepository.save(ChatRoomMember(chatRoom = room, user = member))

        assertNotNull(
            interceptor.preSend(stompMessage(StompCommand.SEND, "/app/chat-rooms/${room.id}", member), channel),
        )
        assertFailsWith<MessageDeliveryException> {
            interceptor.preSend(stompMessage(StompCommand.SEND, "/app/chat-rooms/${room.id}", outsider), channel)
        }
        assertFailsWith<MessageDeliveryException> {
            interceptor.preSend(stompMessage(StompCommand.SEND, "/topic/chat-rooms/${room.id}", member), channel)
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

    private fun subscribeMessage(destination: String, user: User): Message<ByteArray> {
        return stompMessage(StompCommand.SUBSCRIBE, destination, user)
    }

    private fun stompMessage(command: StompCommand, destination: String, user: User): Message<ByteArray> {
        val accessor = StompHeaderAccessor.create(command)
        val userDetails = CustomUserDetails(user)
        accessor.destination = destination
        accessor.user = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
        accessor.setLeaveMutable(true)
        return MessageBuilder.createMessage(ByteArray(0), accessor.messageHeaders)
    }

    private fun createUser(username: String): User =
        userRepository.save(User(username = username, password = "encoded", nickname = username))
}
