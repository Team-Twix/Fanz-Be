package com.example.fanzbe.domain.profile.service

import com.example.fanzbe.domain.chat.repository.ChatRoomMemberRepository
import com.example.fanzbe.domain.follow.repository.FollowRepository
import com.example.fanzbe.domain.profile.dto.JoinedChatRoomResponse
import com.example.fanzbe.domain.profile.dto.MyProfileResponse
import com.example.fanzbe.domain.profile.dto.UpdateProfileRequest
import com.example.fanzbe.domain.profile.dto.UserProfileResponse
import com.example.fanzbe.domain.profile.entity.Profile
import com.example.fanzbe.domain.profile.repository.ProfileRepository
import com.example.fanzbe.domain.user.repository.UserRepository
import com.example.fanzbe.global.exception.BusinessException
import com.example.fanzbe.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProfileService(
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val followRepository: FollowRepository,
    private val chatRoomMemberRepository: ChatRoomMemberRepository,
) {

    // 프로필이 없으면 기본 프로필을 만들어 반환하므로 읽기 전용이 아니다.
    @Transactional
    fun getMyProfile(userId: Long): MyProfileResponse =
        getOrCreateProfile(userId).toResponse()

    @Transactional
    fun getUserProfile(targetUserId: Long, currentUserId: Long): UserProfileResponse {
        val profile = getOrCreateProfile(targetUserId)
        val isFollowing = currentUserId != targetUserId &&
            followRepository.existsByFollowerIdAndFolloweeId(currentUserId, targetUserId)
        val base = profile.toResponse()

        return UserProfileResponse(
            userId = base.userId,
            nickname = base.nickname,
            handle = base.handle,
            bio = base.bio,
            profileImageUrl = base.profileImageUrl,
            coverImageUrl = base.coverImageUrl,
            followerCount = base.followerCount,
            followingCount = base.followingCount,
            joinedChatRooms = base.joinedChatRooms,
            isFollowing = isFollowing,
        )
    }

    @Transactional
    fun updateMyProfile(userId: Long, request: UpdateProfileRequest): MyProfileResponse {
        val profile = getOrCreateProfile(userId)

        request.handle?.trim()?.takeIf { it.isNotEmpty() }?.let { newHandle ->
            val owner = profileRepository.findByHandle(newHandle)
            if (owner != null && owner.user.id != userId) {
                throw BusinessException(ErrorCode.HANDLE_DUPLICATED)
            }
            profile.handle = newHandle
        }
        request.bio?.let { profile.bio = it }
        request.profileImageUrl?.let { profile.profileImageUrl = it }
        request.coverImageUrl?.let { profile.coverImageUrl = it }

        return profile.toResponse()
    }

    private fun getOrCreateProfile(userId: Long): Profile =
        profileRepository.findByUserId(userId)
            ?: run {
                val user = userRepository.findById(userId)
                    .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
                profileRepository.save(Profile(user = user))
            }

    private fun Profile.toResponse(): MyProfileResponse {
        val ownerId = requireNotNull(user.id) { "Profile user id must not be null." }
        val joinedChatRooms = chatRoomMemberRepository.findByUserId(ownerId)
            .map { member ->
                val room = member.chatRoom
                JoinedChatRoomResponse(
                    id = requireNotNull(room.id) { "Chat room id must not be null." },
                    name = room.name,
                    imageUrl = room.imageUrl,
                    description = room.description,
                )
            }

        return MyProfileResponse(
            userId = ownerId,
            nickname = user.nickname,
            handle = handle,
            bio = bio,
            profileImageUrl = profileImageUrl,
            coverImageUrl = coverImageUrl,
            followerCount = followRepository.countByFolloweeId(ownerId),
            followingCount = followRepository.countByFollowerId(ownerId),
            joinedChatRooms = joinedChatRooms,
        )
    }
}
