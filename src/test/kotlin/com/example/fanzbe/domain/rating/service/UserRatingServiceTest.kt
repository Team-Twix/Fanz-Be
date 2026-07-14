package com.example.fanzbe.domain.rating.service

import com.example.fanzbe.domain.chat.dto.CreateChatRoomRequest
import com.example.fanzbe.domain.chat.service.ChatRoomService
import com.example.fanzbe.domain.profile.service.ProfileService
import com.example.fanzbe.domain.rating.dto.RateUserRequest
import com.example.fanzbe.domain.rating.repository.UserRatingRepository
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
class UserRatingServiceTest(
    @Autowired private val userRatingService: UserRatingService,
    @Autowired private val userRatingRepository: UserRatingRepository,
    @Autowired private val chatRoomService: ChatRoomService,
    @Autowired private val profileService: ProfileService,
    @Autowired private val userRepository: UserRepository,
) {

    @Test
    fun `같은 방 멤버 평점을 갱신하고 프로필 집계에 반영한다`() {
        val rater = createUser("rating-rater")
        val target = createUser("rating-target")
        val roomId = chatRoomService.createRoom(rater.id!!, CreateChatRoomRequest(name = "rating room")).id
        chatRoomService.joinRoom(roomId, target.id!!)

        val first = userRatingService.rate(rater.id!!, target.id!!, RateUserRequest(roomId, 5))
        val updated = userRatingService.rate(rater.id!!, target.id!!, RateUserRequest(roomId, 4))

        assertEquals(46.5, first.mannerScore)
        assertEquals(41.5, updated.mannerScore)
        assertEquals(1, userRatingRepository.count())
        assertEquals(4.0, updated.averageRating)
        val profile = profileService.getUserProfile(target.id!!, rater.id!!)
        assertEquals(4.0, profile.averageRating)
        assertEquals(1, profile.ratingCount)
    }

    @Test
    fun `방에 없는 유저는 평가할 수 없다`() {
        val rater = createUser("rating-host")
        val outsider = createUser("rating-outsider")
        val roomId = chatRoomService.createRoom(rater.id!!, CreateChatRoomRequest(name = "rating room 2")).id

        val exception = assertFailsWith<BusinessException> {
            userRatingService.rate(rater.id!!, outsider.id!!, RateUserRequest(roomId, 5))
        }

        assertEquals(ErrorCode.RATING_NOT_ELIGIBLE, exception.errorCode)
    }

    private fun createUser(username: String): User =
        userRepository.save(User(username = username, password = "encoded", nickname = username))
}
