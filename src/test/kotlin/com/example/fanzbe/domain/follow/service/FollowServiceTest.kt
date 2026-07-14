package com.example.fanzbe.domain.follow.service

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
class FollowServiceTest(
    @Autowired private val followService: FollowService,
    @Autowired private val userRepository: UserRepository,
) {

    @Test
    fun `자기 자신을 팔로우하면 CANNOT_FOLLOW_SELF 예외가 발생한다`() {
        val user = userRepository.save(
            User(username = "self", password = "encoded", nickname = "self"),
        )

        val exception = assertFailsWith<BusinessException> {
            followService.follow(followerId = user.id!!, followeeId = user.id!!)
        }

        assertEquals(ErrorCode.CANNOT_FOLLOW_SELF, exception.errorCode)
    }
}
