package com.example.fanzbe.domain.chat.service

import com.example.fanzbe.domain.chat.dto.CreateChatRoomRequest
import com.example.fanzbe.domain.chat.entity.AgeGroup
import com.example.fanzbe.domain.chat.entity.Gender
import com.example.fanzbe.domain.chat.entity.MessageType
import com.example.fanzbe.domain.profile.entity.Profile
import com.example.fanzbe.domain.profile.repository.ProfileRepository
import com.example.fanzbe.domain.user.entity.User
import com.example.fanzbe.domain.user.entity.UserGender
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
class ChatFeatureCompletionTest(
    @Autowired private val chatRoomService: ChatRoomService,
    @Autowired private val chatMessageService: ChatMessageService,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val profileRepository: ProfileRepository,
) {

    @Test
    fun `방 상세 멤버 추천 인박스와 읽음 상태를 제공한다`() {
        val host = createUser("host-complete", "host", com.example.fanzbe.domain.user.entity.AgeGroup.TWENTIES)
        val member = createUser("member-complete", "member", com.example.fanzbe.domain.user.entity.AgeGroup.TWENTIES)
        profileRepository.save(Profile(user = host, profileImageUrl = "/uploads/host.png"))
        profileRepository.save(Profile(user = member, handle = "member-handle"))
        val room = chatRoomService.createRoom(host.id!!, restrictedRoomRequest())
        chatMessageService.sendMessage(room.id, host.id!!, "참여 전 메시지")
        chatRoomService.joinRoom(room.id, member.id!!)
        assertEquals(0, chatRoomService.getMyRooms(member.id!!).single().unreadCount)

        val detail = chatRoomService.getRoom(room.id)
        assertEquals("한 줄 소개", detail.summary)
        assertEquals("상세 설명", detail.description)
        assertEquals(2, detail.memberCount)

        val members = chatRoomService.getMembers(room.id, member.id!!)
        assertEquals(listOf(host.id, member.id), members.map { it.userId })
        assertTrue(members.first { it.userId == host.id }.isHost)
        assertEquals("member-handle", members.first { it.userId == member.id }.handle)

        val recommendations = chatRoomService.getRecommendedRooms(member.id!!)
        assertEquals("anime", recommendations.single().interest)
        assertEquals(room.id, recommendations.single().rooms.single().id)

        val sent = chatMessageService.sendMessage(room.id, host.id!!, "새 메시지")
        assertEquals("host", sent.senderNickname)
        assertEquals("/uploads/host.png", sent.senderProfileImageUrl)
        assertEquals(1, chatRoomService.getMyRooms(member.id!!).single().unreadCount)

        chatMessageService.getMessages(room.id, member.id!!, page = 0, size = 30)
        assertEquals(0, chatRoomService.getMyRooms(member.id!!).single().unreadCount)

        val image = chatMessageService.sendMessage(
            roomId = room.id,
            senderUserId = member.id!!,
            content = null,
            messageType = MessageType.IMAGE,
            attachmentUrl = "/uploads/chat.webp",
        )
        assertEquals(MessageType.IMAGE, image.messageType)
        assertEquals("/uploads/chat.webp", image.attachmentUrl)
    }

    @Test
    fun `연령 성별 관심사 조건을 모두 충족해야 참여할 수 있다`() {
        val host = createUser("condition-host", "host", com.example.fanzbe.domain.user.entity.AgeGroup.TWENTIES)
        val roomId = chatRoomService.createRoom(host.id!!, restrictedRoomRequest()).id
        val wrongAge = createUser(
            "wrong-age",
            "wrongAge",
            com.example.fanzbe.domain.user.entity.AgeGroup.THIRTIES,
        )
        val wrongGender = createUser(
            "wrong-gender",
            "wrongGender",
            com.example.fanzbe.domain.user.entity.AgeGroup.TWENTIES,
            gender = UserGender.MALE,
        )
        val wrongInterest = createUser(
            "wrong-interest",
            "wrongInterest",
            com.example.fanzbe.domain.user.entity.AgeGroup.TWENTIES,
            interests = mutableSetOf("game"),
        )

        listOf(wrongAge, wrongGender, wrongInterest).forEach { user ->
            val exception = assertFailsWith<BusinessException> {
                chatRoomService.joinRoom(roomId, user.id!!)
            }
            assertEquals(ErrorCode.CHAT_JOIN_CONDITION_NOT_MET, exception.errorCode)
        }
    }

    private fun restrictedRoomRequest() = CreateChatRoomRequest(
        name = "anime room",
        summary = "한 줄 소개",
        description = "상세 설명",
        hashtags = listOf("anime"),
        ageConditions = listOf(AgeGroup.TWENTIES),
        gender = Gender.FEMALE,
    )

    private fun createUser(
        username: String,
        nickname: String,
        ageGroup: com.example.fanzbe.domain.user.entity.AgeGroup,
        gender: UserGender = UserGender.FEMALE,
        interests: MutableSet<String> = mutableSetOf("anime"),
    ): User = userRepository.save(
        User(
            username = username,
            password = "encoded",
            nickname = nickname,
            ageGroup = ageGroup,
            gender = gender,
            interests = interests,
        ),
    )
}
