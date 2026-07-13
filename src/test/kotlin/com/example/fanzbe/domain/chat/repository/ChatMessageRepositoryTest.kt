package com.example.fanzbe.domain.chat.repository

import com.example.fanzbe.domain.chat.entity.ChatMessage
import com.example.fanzbe.domain.chat.entity.ChatRoom
import com.example.fanzbe.domain.chat.entity.ChatRoomMember
import com.example.fanzbe.domain.chat.service.ChatRoomStatistics
import com.example.fanzbe.domain.user.entity.User
import com.example.fanzbe.domain.user.repository.UserRepository
import jakarta.persistence.EntityManager
import kotlin.test.Test
import kotlin.test.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

/**
 * 인기 채팅방 통계의 핵심 집계 쿼리를 실제 H2 로 검증한다.
 * - findActiveSenderIds: 이번 달 30개 이상 보낸 서로 다른 sender 만 집계
 * - countByChatRoom...: 이번 달 메시지 수 (지난 달 제외)
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChatMessageRepositoryTest(
    @Autowired private val chatMessageRepository: ChatMessageRepository,
    @Autowired private val chatRoomRepository: ChatRoomRepository,
    @Autowired private val chatRoomMemberRepository: ChatRoomMemberRepository,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val entityManager: EntityManager,
) {

    @Test
    fun `이번 달 30개 이상 보낸 서로 다른 멤버만 활성으로 집계한다`() {
        val room = createRoom(host = createUser("host@fanz.com"))
        val active = joinUser(room, "active@fanz.com")     // 30개 -> 활성
        val borderline = joinUser(room, "border@fanz.com") // 29개 -> 비활성
        val lastMonth = joinUser(room, "last@fanz.com")    // 30개지만 지난 달 -> 제외

        saveMessages(room, active, count = 30)
        saveMessages(room, borderline, count = 29)
        saveMessages(room, lastMonth, count = 30)
        backdateSenderMessagesToLastMonth(room, lastMonth)

        val activeSenderIds = chatMessageRepository.findActiveSenderIds(
            roomId = room.id!!,
            monthStart = ChatRoomStatistics.currentMonthStart(),
            threshold = ChatRoomStatistics.MONTHLY_ACTIVE_MESSAGE_THRESHOLD.toLong(),
        )

        assertEquals(listOf(active.id), activeSenderIds)
    }

    @Test
    fun `이번 달 메시지 수는 지난 달 메시지를 제외한다`() {
        val room = createRoom(host = createUser("host2@fanz.com"))
        val thisMonth = joinUser(room, "this@fanz.com")
        val prevMonth = joinUser(room, "prev@fanz.com")

        saveMessages(room, thisMonth, count = 5)
        saveMessages(room, prevMonth, count = 7)
        backdateSenderMessagesToLastMonth(room, prevMonth)

        val monthlyCount = chatMessageRepository.countByChatRoomIdAndCreatedAtGreaterThanEqual(
            chatRoomId = room.id!!,
            createdAt = ChatRoomStatistics.currentMonthStart(),
        )

        assertEquals(5L, monthlyCount)
    }

    private fun createUser(email: String): User =
        userRepository.save(
            User(email = email, password = "encoded", nickname = email.substringBefore("@")),
        )

    private fun createRoom(host: User): ChatRoom =
        chatRoomRepository.save(
            ChatRoom(name = "room", description = null, imageUrl = null, category = "anime", host = host),
        )

    private fun joinUser(room: ChatRoom, email: String): User {
        val user = createUser(email)
        chatRoomMemberRepository.save(ChatRoomMember(chatRoom = room, user = user))
        return user
    }

    private fun saveMessages(room: ChatRoom, sender: User, count: Int) {
        repeat(count) {
            chatMessageRepository.save(ChatMessage(chatRoom = room, sender = sender, content = "msg"))
        }
    }

    /** auditing 으로 채워진 created_at 을 네이티브 UPDATE 로 지난 달로 되돌린다. */
    private fun backdateSenderMessagesToLastMonth(room: ChatRoom, sender: User) {
        entityManager.flush()
        val lastMonth = ChatRoomStatistics.currentMonthStart().minusMonths(1).plusDays(1)
        entityManager.createNativeQuery(
            "update chat_messages set created_at = :ts where chat_room_id = :roomId and sender_id = :senderId",
        )
            .setParameter("ts", lastMonth)
            .setParameter("roomId", room.id)
            .setParameter("senderId", sender.id)
            .executeUpdate()
        entityManager.clear()
    }
}
