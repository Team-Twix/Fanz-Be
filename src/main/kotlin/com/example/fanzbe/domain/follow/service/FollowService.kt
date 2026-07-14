package com.example.fanzbe.domain.follow.service

import com.example.fanzbe.domain.follow.dto.FollowUserResponse
import com.example.fanzbe.domain.follow.entity.Follow
import com.example.fanzbe.domain.follow.repository.FollowRepository
import com.example.fanzbe.domain.profile.repository.ProfileRepository
import com.example.fanzbe.domain.user.entity.User
import com.example.fanzbe.domain.user.repository.UserRepository
import com.example.fanzbe.global.exception.BusinessException
import com.example.fanzbe.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FollowService(
    private val followRepository: FollowRepository,
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
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

    /** targetUserId 를 팔로우하는 유저 목록. */
    @Transactional(readOnly = true)
    fun getFollowers(targetUserId: Long, currentUserId: Long): List<FollowUserResponse> =
        followRepository.findByFolloweeId(targetUserId)
            .map { it.follower.toFollowUser(currentUserId) }

    /** targetUserId 가 팔로우하는 유저 목록. */
    @Transactional(readOnly = true)
    fun getFollowings(targetUserId: Long, currentUserId: Long): List<FollowUserResponse> =
        followRepository.findByFollowerId(targetUserId)
            .map { it.followee.toFollowUser(currentUserId) }

    private fun User.toFollowUser(currentUserId: Long): FollowUserResponse {
        val targetId = requireNotNull(id) { "User id must not be null." }
        val profile = profileRepository.findByUserId(targetId)
        val isFollowing = currentUserId != targetId &&
            followRepository.existsByFollowerIdAndFolloweeId(currentUserId, targetId)

        return FollowUserResponse(
            userId = targetId,
            nickname = nickname,
            handle = profile?.handle,
            profileImageUrl = profile?.profileImageUrl,
            isFollowing = isFollowing,
        )
    }

    private fun getUser(userId: Long) =
        userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
}
