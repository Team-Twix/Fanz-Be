package com.example.fanzbe.domain.user.service

import com.example.fanzbe.domain.follow.repository.FollowRepository
import com.example.fanzbe.domain.profile.repository.ProfileRepository
import com.example.fanzbe.domain.user.dto.UserSearchResponse
import com.example.fanzbe.domain.user.entity.User
import com.example.fanzbe.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserSearchService(
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val followRepository: FollowRepository,
) {

    @Transactional(readOnly = true)
    fun search(currentUserId: Long, nickname: String?, hashtag: String?): List<UserSearchResponse> {
        val normalizedNickname = nickname.normalizeSearchParam()
        val normalizedHashtag = hashtag.normalizeHashtag()

        return userRepository.search(
            currentUserId = currentUserId,
            nickname = normalizedNickname,
            hashtag = normalizedHashtag,
        ).map { it.toSearchResponse(currentUserId) }
    }

    private fun User.toSearchResponse(currentUserId: Long): UserSearchResponse {
        val targetId = requireNotNull(id) { "User id must not be null." }
        val profile = profileRepository.findByUserId(targetId)
        val isFollowing = currentUserId != targetId &&
            followRepository.existsByFollowerIdAndFolloweeId(currentUserId, targetId)

        return UserSearchResponse(
            userId = targetId,
            nickname = nickname,
            handle = profile?.handle,
            profileImageUrl = profile?.profileImageUrl,
            interests = interests.sorted(),
            mannerScore = mannerScore,
            isFollowing = isFollowing,
        )
    }

    private fun String?.normalizeSearchParam(): String? =
        this?.trim()?.takeIf { it.isNotEmpty() }

    private fun String?.normalizeHashtag(): String? =
        this?.trim()?.removePrefix("#")?.lowercase()?.takeIf { it.isNotEmpty() }
}
