package com.example.fanzbe.domain.profile.service

import com.example.fanzbe.domain.chat.entity.ChatRoom
import com.example.fanzbe.domain.chat.entity.ChatRoomMember
import com.example.fanzbe.domain.chat.repository.ChatRoomMemberRepository
import com.example.fanzbe.domain.chat.repository.ChatRoomRepository
import com.example.fanzbe.domain.follow.entity.Follow
import com.example.fanzbe.domain.follow.repository.FollowRepository
import com.example.fanzbe.domain.profile.repository.ProfileRepository
import com.example.fanzbe.domain.user.entity.User
import com.example.fanzbe.domain.user.repository.UserRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProfileServiceTest(
    @Autowired private val profileService: ProfileService,
    @Autowired private val profileRepository: ProfileRepository,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val followRepository: FollowRepository,
    @Autowired private val chatRoomRepository: ChatRoomRepository,
    @Autowired private val chatRoomMemberRepository: ChatRoomMemberRepository,
) {

    @Test
    fun `프로필이 없으면 기본 프로필을 생성하고 팔로워 팔로잉 수와 참여중인 채팅방을 반환한다`() {
        val owner = createUser("owner@fanz.com", "owner")
        val follower = createUser("follower@fanz.com", "follower")
        val following = createUser("following@fanz.com", "following")
        followRepository.save(Follow(follower = follower, followee = owner))
        followRepository.save(Follow(follower = owner, followee = following))

        val joinedRoom = chatRoomRepository.save(
            ChatRoom(
                name = "anime room",
                description = "anime fans",
                imageUrl = "https://example.com/room.png",
                host = owner,
            ),
        )
        chatRoomMemberRepository.save(ChatRoomMember(chatRoom = joinedRoom, user = owner))

        val response = profileService.getMyProfile(owner.id!!)

        assertNotNull(profileRepository.findByUserId(owner.id!!))
        assertEquals(owner.id!!, response.userId)
        assertEquals("owner", response.nickname)
        assertNull(response.handle)
        assertNull(response.bio)
        assertEquals(1L, response.followerCount)
        assertEquals(1L, response.followingCount)
        assertEquals(1, response.joinedChatRooms.size)
        assertEquals(joinedRoom.id!!, response.joinedChatRooms[0].id)
        assertEquals("anime room", response.joinedChatRooms[0].name)
        assertEquals("https://example.com/room.png", response.joinedChatRooms[0].imageUrl)
        assertEquals("anime fans", response.joinedChatRooms[0].description)
    }

    private fun createUser(email: String, nickname: String): User =
        userRepository.save(
            User(email = email, password = "encoded", nickname = nickname),
        )
}
