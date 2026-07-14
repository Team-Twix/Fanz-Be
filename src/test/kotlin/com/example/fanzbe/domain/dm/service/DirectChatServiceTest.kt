package com.example.fanzbe.domain.dm.service

import com.example.fanzbe.domain.chat.dto.SendMessageRequest
import com.example.fanzbe.domain.chat.entity.MessageType
import com.example.fanzbe.domain.profile.entity.Profile
import com.example.fanzbe.domain.profile.repository.ProfileRepository
import com.example.fanzbe.domain.user.entity.User
import com.example.fanzbe.domain.user.repository.UserRepository
import com.example.fanzbe.global.exception.BusinessException
import com.example.fanzbe.global.exception.ErrorCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DirectChatServiceTest(
    @Autowired private val directChatService: DirectChatService,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val profileRepository: ProfileRepository,
) {

    @Test
    fun `같은 상대와 DM 방을 멱등하게 열고 메시지와 안 읽은 수를 관리한다`() {
        val first = createUser("dm-first", "first")
        val second = createUser("dm-second", "second")
        profileRepository.save(Profile(user = first, profileImageUrl = "/uploads/first.png"))

        val opened = directChatService.openRoom(first.id!!, second.id!!)
        val reopened = directChatService.openRoom(second.id!!, first.id!!)
        assertEquals(opened.id, reopened.id)

        val sent = directChatService.sendMessage(
            opened.id,
            first.id!!,
            SendMessageRequest(content = "안녕하세요"),
        )
        assertEquals("first", sent.senderNickname)
        assertEquals("/uploads/first.png", sent.senderProfileImageUrl)
        assertEquals(1, directChatService.getRooms(second.id!!).single().unreadCount)

        val messages = directChatService.getMessages(opened.id, second.id!!, page = 0, size = 30)
        assertEquals("안녕하세요", messages.content.single().content)
        assertEquals(0, directChatService.getRooms(second.id!!).single().unreadCount)

        val image = directChatService.sendMessage(
            opened.id,
            second.id!!,
            SendMessageRequest(
                messageType = MessageType.IMAGE,
                attachmentUrl = "/uploads/dm.png",
            ),
        )
        assertEquals(MessageType.IMAGE, image.messageType)
        assertEquals("/uploads/dm.png", image.attachmentUrl)
    }

    @Test
    fun `DM 참여자가 아니면 방을 조회할 수 없다`() {
        val first = createUser("dm-member-one", "one")
        val second = createUser("dm-member-two", "two")
        val outsider = createUser("dm-outsider", "outsider")
        val room = directChatService.openRoom(first.id!!, second.id!!)

        val exception = assertFailsWith<BusinessException> {
            directChatService.getRoom(room.id, outsider.id!!)
        }

        assertEquals(ErrorCode.NOT_DM_MEMBER, exception.errorCode)
    }

    private fun createUser(username: String, nickname: String): User =
        userRepository.save(User(username = username, password = "encoded", nickname = nickname))
}
