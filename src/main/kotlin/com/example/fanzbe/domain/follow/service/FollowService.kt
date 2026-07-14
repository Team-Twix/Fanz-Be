package com.example.fanzbe.domain.follow.service

import com.example.fanzbe.domain.follow.entity.Follow
import com.example.fanzbe.domain.follow.repository.FollowRepository
import com.example.fanzbe.domain.user.repository.UserRepository
import com.example.fanzbe.global.exception.BusinessException
import com.example.fanzbe.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FollowService(
    private val followRepository: FollowRepository,
    private val userRepository: UserRepository,
) {

    @Transactional
    fun follow(followerId: Long, followeeId: Long) {
        if (followerId == followeeId) {
            throw BusinessException(ErrorCode.CANNOT_FOLLOW_SELF)
        }
        if (followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            return
        }

        val follower = getUser(followerId)
        val followee = getUser(followeeId)
        followRepository.save(Follow(follower = follower, followee = followee))
    }

    @Transactional
    fun unfollow(followerId: Long, followeeId: Long) {
        followRepository.deleteByFollowerIdAndFolloweeId(followerId, followeeId)
    }

    private fun getUser(userId: Long) =
        userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
}
