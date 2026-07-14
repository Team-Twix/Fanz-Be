package com.example.fanzbe.domain.chat.service

import com.example.fanzbe.domain.chat.dto.CreateChatRoomRequest
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChatMemberManagementTest(
    @Autowired private val chatRoomService: ChatRoomService,
    @Autowired private val chatMessageService: ChatMessageService,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val chatRoomRepository: ChatRoomRepository,
    @Autowired private val chatRoomMemberRepository: ChatRoomMemberRepository,
    @Autowired private val chatMessageRepository: ChatMessageRepository,
) {

    @Test
    fun `방장이 멤버를 추방한다`() {
        val host = createUser("host")
        val member = createUser("member")
        val roomId = chatRoomService.createRoom(host.id!!, roomRequest()).id
        chatRoomService.joinRoom(roomId, member.id!!)

        chatRoomService.kickMember(roomId = roomId, hostUserId = host.id!!, targetUserId = member.id!!)

        assertFalse(isMember(roomId, member.id!!))
    }

    @Test
    fun `방장이 아니면 추방 불가`() {
        val host = createUser("host")
        val member = createUser("member")
        val other = createUser("other")
        val roomId = chatRoomService.createRoom(host.id!!, roomRequest()).id
        chatRoomService.joinRoom(roomId, member.id!!)
        chatRoomService.joinRoom(roomId, other.id!!)

        val ex = assertFailsWith<BusinessException> {
            chatRoomService.kickMember(roomId = roomId, hostUserId = other.id!!, targetUserId = member.id!!)
        }
        assertEquals(ErrorCode.NOT_ROOM_HOST, ex.errorCode)
    }

    @Test
    fun `멤버가 채팅방을 나간다`() {
        val host = createUser("host")
        val member = createUser("member")
        val roomId = chatRoomService.createRoom(host.id!!, roomRequest()).id
        chatRoomService.joinRoom(roomId, member.id!!)

        chatRoomService.leaveRoom(roomId = roomId, userId = member.id!!)

        assertFalse(isMember(roomId, member.id!!))
    }

    @Test
    fun `방장은 나갈 수 없다`() {
        val host = createUser("host")
        val roomId = chatRoomService.createRoom(host.id!!, roomRequest()).id

        val ex = assertFailsWith<BusinessException> {
            chatRoomService.leaveRoom(roomId = roomId, userId = host.id!!)
        }
        assertEquals(ErrorCode.CANNOT_LEAVE_AS_HOST, ex.errorCode)
    }

    @Test
    fun `방장이 방을 삭제하면 멤버·메시지도 삭제된다`() {
        val host = createUser("host")
        val member = createUser("member")
        val roomId = chatRoomService.createRoom(host.id!!, roomRequest()).id
        chatRoomService.joinRoom(roomId, member.id!!)
        chatMessageService.sendMessage(roomId, member.id!!, "hi")

        chatRoomService.deleteRoom(roomId = roomId, hostUserId = host.id!!)

        assertTrue(chatRoomRepository.findById(roomId).isEmpty)
        assertTrue(chatRoomMemberRepository.findByUserId(member.id!!).isEmpty())
        assertEquals(0, chatMessageRepository.count())
    }

    private fun roomRequest() = CreateChatRoomRequest(name = "room", hashtags = listOf("anime"))

    private fun isMember(roomId: Long, userId: Long): Boolean =
        chatRoomMemberRepository.findByUserId(userId).any { it.chatRoom.id == roomId }

    private fun createUser(username: String): User =
        userRepository.save(User(username = username, password = "encoded", nickname = username))
}
