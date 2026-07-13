package com.example.fanzbe.domain.chat

import com.example.fanzbe.domain.chat.dto.ChatMessageResponse
import com.example.fanzbe.domain.chat.dto.SendMessageRequest
import com.example.fanzbe.domain.chat.entity.ChatRoom
import com.example.fanzbe.domain.chat.entity.ChatRoomMember
import com.example.fanzbe.domain.chat.repository.ChatMessageRepository
import com.example.fanzbe.domain.chat.repository.ChatRoomMemberRepository
import com.example.fanzbe.domain.chat.repository.ChatRoomRepository
import com.example.fanzbe.domain.user.entity.User
import com.example.fanzbe.domain.user.repository.UserRepository
import com.example.fanzbe.global.security.JwtProvider
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment
import org.springframework.messaging.simp.stomp.StompFrameHandler
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.MimeType
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import tools.jackson.databind.ObjectMapper

/**
 * 로컬 작업 전체(인증 + 채팅방 도메인 + 실시간 WebSocket)를 한 번에 관통하는 통합 테스트.
 * JWT 로 STOMP 연결 → /app 으로 전송 → /topic 구독자 수신 → DB 저장까지 검증한다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ChatRealtimeE2eTest(
    @Autowired private val jwtProvider: JwtProvider,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val chatRoomRepository: ChatRoomRepository,
    @Autowired private val chatRoomMemberRepository: ChatRoomMemberRepository,
    @Autowired private val chatMessageRepository: ChatMessageRepository,
    @Autowired private val environment: Environment,
) {

    @Test
    fun `JWT 로 연결해 보낸 메시지가 구독자에게 실시간 전달되고 저장된다`() {
        // 서버가 별도 스레드에서 도므로 데이터는 커밋되어 있어야 한다(@Transactional 미사용).
        val user = userRepository.save(
            User(username ="realtime@fanz.com", password = "encoded", nickname = "rt"),
        )
        val room = chatRoomRepository.save(
            ChatRoom(name = "room", hashtags = mutableSetOf("anime"), host = user),
        )
        chatRoomMemberRepository.save(ChatRoomMember(chatRoom = room, user = user))
        val token = jwtProvider.createAccessToken(user.id!!)

        val stompClient = WebSocketStompClient(StandardWebSocketClient())
        val connectHeaders = StompHeaders().apply { add("Authorization", "Bearer $token") }

        val session = stompClient
            .connectAsync(
                "ws://localhost:${port()}/ws",
                WebSocketHttpHeaders(),
                connectHeaders,
                object : StompSessionHandlerAdapter() {},
            )
            .get(5, TimeUnit.SECONDS)

        try {
            val received = ArrayBlockingQueue<ByteArray>(1)
            session.subscribe(
                "/topic/chat-rooms/${room.id}",
                object : StompFrameHandler {
                    override fun getPayloadType(headers: StompHeaders) = ByteArray::class.java
                    override fun handleFrame(headers: StompHeaders, payload: Any?) {
                        received.add(payload as ByteArray)
                    }
                },
            )
            // 구독 등록이 서버에 반영될 시간을 준다(SUBSCRIBE 가 SEND 보다 먼저 처리되도록).
            Thread.sleep(500)

            val sendHeaders = StompHeaders().apply {
                destination = "/app/chat-rooms/${room.id}"
                contentType = MimeType.valueOf("application/json")
            }
            session.send(sendHeaders, objectMapper.writeValueAsBytes(SendMessageRequest(content = "안녕 실시간")))

            val payload = assertNotNull(received.poll(5, TimeUnit.SECONDS), "브로드캐스트 수신 실패")
            val response = objectMapper.readValue(payload, ChatMessageResponse::class.java)
            assertEquals("안녕 실시간", response.content)
            assertEquals(room.id, response.roomId)
            assertEquals(user.id, response.senderId)

            // DB 저장 확인
            val stored = chatMessageRepository.findAll()
            assertEquals(1, stored.size)
            assertEquals("안녕 실시간", stored.first().content)
        } finally {
            session.disconnect()
            // 이 테스트는 @Transactional 이 아니라 커밋되므로, 공유 H2 오염을 막기 위해 직접 정리한다.
            chatMessageRepository.deleteAll()
            chatRoomMemberRepository.deleteAll()
            chatRoomRepository.deleteAll()
            userRepository.deleteAll()
        }
    }

    private fun port(): Int =
        environment.getProperty("local.server.port")?.toInt()
            ?: error("local.server.port 를 확인할 수 없습니다.")
}
