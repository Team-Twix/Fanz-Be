package com.example.fanzbe.domain.dm

import com.example.fanzbe.domain.dm.dto.DmMessageResponse
import com.example.fanzbe.domain.dm.dto.SendDmMessageRequest
import com.example.fanzbe.domain.dm.repository.DmMessageRepository
import com.example.fanzbe.domain.dm.repository.DmRoomRepository
import com.example.fanzbe.domain.dm.service.DmService
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
 * DM 실시간(STOMP) 왕복 검증: JWT 연결 → /app/dm/rooms/{id} 전송 → /topic/dm/rooms/{id} 수신.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DmRealtimeE2eTest(
    @Autowired private val jwtProvider: JwtProvider,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val dmService: DmService,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val dmRoomRepository: DmRoomRepository,
    @Autowired private val dmMessageRepository: DmMessageRepository,
    @Autowired private val environment: Environment,
) {

    @Test
    fun `DM 메시지가 실시간으로 상대에게 전달된다`() {
        val sender = userRepository.save(User(username = "dm-a", password = "encoded", nickname = "a"))
        val other = userRepository.save(User(username = "dm-b", password = "encoded", nickname = "b"))
        val roomId = dmService.getOrCreateRoom(sender.id!!, other.id!!).roomId
        val token = jwtProvider.createAccessToken(sender.id!!)

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
                "/topic/dm/rooms/$roomId",
                object : StompFrameHandler {
                    override fun getPayloadType(headers: StompHeaders) = ByteArray::class.java
                    override fun handleFrame(headers: StompHeaders, payload: Any?) {
                        received.add(payload as ByteArray)
                    }
                },
            )
            Thread.sleep(500)

            val sendHeaders = StompHeaders().apply {
                destination = "/app/dm/rooms/$roomId"
                contentType = MimeType.valueOf("application/json")
            }
            session.send(sendHeaders, objectMapper.writeValueAsBytes(SendDmMessageRequest(content = "안녕 DM")))

            val payload = assertNotNull(received.poll(5, TimeUnit.SECONDS), "DM 브로드캐스트 수신 실패")
            val response = objectMapper.readValue(payload, DmMessageResponse::class.java)
            assertEquals("안녕 DM", response.content)
            assertEquals(sender.id, response.senderId)
        } finally {
            session.disconnect()
            dmMessageRepository.deleteAll()
            dmRoomRepository.deleteAll()
            userRepository.deleteAll()
        }
    }

    private fun port(): Int =
        environment.getProperty("local.server.port")?.toInt()
            ?: error("local.server.port 를 확인할 수 없습니다.")
}
