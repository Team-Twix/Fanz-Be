package com.example.fanzbe.domain.profile

import com.example.fanzbe.domain.follow.service.FollowService
import com.example.fanzbe.domain.profile.service.ProfileService
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
class UserProfileSocialTest(
    @Autowired private val profileService: ProfileService,
    @Autowired private val followService: FollowService,
    @Autowired private val userRepository: UserRepository,
) {

    @Test
    fun `타 유저 프로필의 isFollowing 이 팔로우 상태를 반영한다`() {
        val a = createUser("a")
        val b = createUser("b")
        val c = createUser("c")
        followService.follow(followerId = a.id!!, followeeId = b.id!!)

        val bProfile = profileService.getUserProfile(targetUserId = b.id!!, currentUserId = a.id!!)
        val cProfile = profileService.getUserProfile(targetUserId = c.id!!, currentUserId = a.id!!)

        assertTrue(bProfile.isFollowing)   // A는 B를 팔로우
        assertEquals(1, bProfile.followerCount)
        assertFalse(cProfile.isFollowing)  // A는 C를 팔로우 안 함
    }

    @Test
    fun `팔로워·팔로잉 목록을 조회한다`() {
        val a = createUser("a")
        val b = createUser("b")
        followService.follow(followerId = a.id!!, followeeId = b.id!!)

        // B의 팔로워 = [A]
        val bFollowers = followService.getFollowers(targetUserId = b.id!!, currentUserId = a.id!!)
        assertEquals(listOf(a.id), bFollowers.map { it.userId })

        // A의 팔로잉 = [B], A가 B를 팔로우 중이므로 isFollowing true
        val aFollowings = followService.getFollowings(targetUserId = a.id!!, currentUserId = a.id!!)
        assertEquals(listOf(b.id), aFollowings.map { it.userId })
        assertTrue(aFollowings.first().isFollowing)
    }

    private fun createUser(username: String): User =
        userRepository.save(User(username = username, password = "encoded", nickname = username))
}
