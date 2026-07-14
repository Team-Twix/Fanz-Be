package com.example.fanzbe.domain.chat.service

import com.example.fanzbe.domain.chat.entity.ChatMessage
import com.example.fanzbe.domain.chat.entity.ChatRoom
import com.example.fanzbe.domain.chat.entity.ChatRoomMember
import com.example.fanzbe.domain.chat.repository.ChatMessageRepository
import com.example.fanzbe.domain.chat.repository.ChatRoomMemberRepository
import com.example.fanzbe.domain.chat.repository.ChatRoomRepository
import com.example.fanzbe.domain.user.entity.User
import com.example.fanzbe.domain.user.repository.UserRepository
import com.example.fanzbe.global.exception.BusinessException
import com.example.fanzbe.global.exception.ErrorCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChatMessageHistoryTest(
    @Autowired private val chatMessageService: ChatMessageService,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val chatRoomRepository: ChatRoomRepository,
    @Autowired private val chatRoomMemberRepository: ChatRoomMemberRepository,
    @Autowired private val chatMessageRepository: ChatMessageRepository,
) {

    @Test
    fun `메시지 이력을 최신순으로 페이지네이션 조회한다`() {
        val host = createUser("host")
        val room = chatRoomRepository.save(
            ChatRoom(name = "room", hashtags = mutableSetOf("anime"), host = host),
        )
        chatRoomMemberRepository.save(ChatRoomMember(chatRoom = room, user = host))
        val saved = (1..5).map {
            chatMessageRepository.save(ChatMessage(chatRoom = room, sender = host, content = "msg$it"))
        }

        val firstPage = chatMessageService.getMessages(room.id!!, host.id!!, page = 0, size = 3)

        assertEquals(3, firstPage.content.size)
        assertEquals(5, firstPage.totalElements)
        assertTrue(firstPage.hasNext)
        // 최신순(id desc): 마지막으로 저장한 메시지가 첫 항목
        assertEquals(saved.last().id, firstPage.content.first().id)
    }

    @Test
    fun `방 멤버가 아니면 이력 조회 시 NOT_ROOM_MEMBER`() {
        val host = createUser("host2")
        val outsider = createUser("outsider")
        val room = chatRoomRepository.save(
            ChatRoom(name = "room", hashtags = mutableSetOf("anime"), host = host),
        )
        chatRoomMemberRepository.save(ChatRoomMember(chatRoom = room, user = host))

        val ex = assertFailsWith<BusinessException> {
            chatMessageService.getMessages(room.id!!, outsider.id!!, page = 0, size = 30)
        }
        assertEquals(ErrorCode.NOT_ROOM_MEMBER, ex.errorCode)
    }

    private fun createUser(username: String): User =
        userRepository.save(User(username = username, password = "encoded", nickname = username))
}
