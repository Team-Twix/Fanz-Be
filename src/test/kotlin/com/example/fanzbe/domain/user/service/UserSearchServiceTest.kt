package com.example.fanzbe.domain.user.service

import com.example.fanzbe.domain.follow.entity.Follow
import com.example.fanzbe.domain.follow.repository.FollowRepository
import com.example.fanzbe.domain.profile.entity.Profile
import com.example.fanzbe.domain.profile.repository.ProfileRepository
import com.example.fanzbe.domain.user.entity.User
import com.example.fanzbe.domain.user.repository.UserRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserSearchServiceTest(
    @Autowired private val userSearchService: UserSearchService,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val profileRepository: ProfileRepository,
    @Autowired private val followRepository: FollowRepository,
) {

    @Test
    fun `닉네임 부분검색은 본인을 제외하고 닉네임에 검색어가 포함된 유저만 반환한다`() {
        val currentUser = createUser(username = "current-nickname@fanz.com", nickname = "kim current")
        val matchedUser = createUser(username = "matched-nickname@fanz.com", nickname = "Kim anime")
        createUser(username = "unmatched-nickname@fanz.com", nickname = "lee anime")

        val responses = userSearchService.search(
            currentUserId = currentUser.id!!,
            nickname = " kim ",
            hashtag = null,
        )

        assertEquals(listOf(matchedUser.id!!), responses.map { it.userId })
        assertEquals("Kim anime", responses.single().nickname)
        assertFalse(responses.single().isFollowing)
    }

    @Test
    fun `해시태그 검색은 관심사에 동일 태그가 있는 유저만 반환한다`() {
        val currentUser = createUser(
            username = "current-hashtag@fanz.com",
            nickname = "current",
            interests = mutableSetOf("에반게리온"),
        )
        val matchedUser = createUser(
            username = "matched-hashtag@fanz.com",
            nickname = "matched",
            interests = mutableSetOf("에반게리온", "애니"),
        )
        createUser(
            username = "unmatched-hashtag@fanz.com",
            nickname = "unmatched",
            interests = mutableSetOf("주술회전"),
        )

        val responses = userSearchService.search(
            currentUserId = currentUser.id!!,
            nickname = null,
            hashtag = " 에반게리온 ",
        )

        assertEquals(listOf(matchedUser.id!!), responses.map { it.userId })
    }

    @Test
    fun `검색 결과는 프로필과 매너점수와 팔로우 여부를 포함한다`() {
        val currentUser = createUser(username = "current-follow@fanz.com", nickname = "current")
        val followedUser = createUser(
            username = "followed-search@fanz.com",
            nickname = "eva kim",
            interests = mutableSetOf("에반게리온"),
        )
        createUser(
            username = "and-filtered-out@fanz.com",
            nickname = "eva kim",
            interests = mutableSetOf("슬램덩크"),
        )
        profileRepository.save(
            Profile(
                user = followedUser,
                handle = "eva_kim",
                profileImageUrl = "https://example.com/eva.png",
            ),
        )
        followRepository.save(Follow(follower = currentUser, followee = followedUser))

        val responses = userSearchService.search(
            currentUserId = currentUser.id!!,
            nickname = "eva",
            hashtag = "에반게리온",
        )

        val response = responses.single()
        assertEquals(followedUser.id!!, response.userId)
        assertEquals("eva kim", response.nickname)
        assertEquals("eva_kim", response.handle)
        assertEquals("https://example.com/eva.png", response.profileImageUrl)
        assertEquals(User.DEFAULT_MANNER_SCORE, response.mannerScore)
        assertTrue(response.isFollowing)
    }

    private fun createUser(
        username: String,
        nickname: String,
        interests: MutableSet<String> = mutableSetOf(),
    ): User =
        userRepository.save(
            User(
                username = username,
                password = "encoded",
                nickname = nickname,
                interests = interests,
            ),
        )
}
