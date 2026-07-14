package com.example.fanzbe.domain.dm.service

import com.example.fanzbe.domain.dm.dto.DmMessagePageResponse
import com.example.fanzbe.domain.dm.dto.DmMessageResponse
import com.example.fanzbe.domain.dm.dto.DmRoomResponse
import com.example.fanzbe.domain.dm.dto.DmRoomSummaryResponse
import com.example.fanzbe.domain.dm.dto.DmUserSummary
import com.example.fanzbe.domain.dm.entity.DmMessage
import com.example.fanzbe.domain.dm.entity.DmRoom
import com.example.fanzbe.domain.dm.repository.DmMessageRepository
import com.example.fanzbe.domain.dm.repository.DmRoomRepository
import com.example.fanzbe.domain.profile.repository.ProfileRepository
import com.example.fanzbe.domain.user.entity.User
import com.example.fanzbe.domain.user.repository.UserRepository
import com.example.fanzbe.global.exception.BusinessException
import com.example.fanzbe.global.exception.ErrorCode
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DmService(
    private val dmRoomRepository: DmRoomRepository,
    private val dmMessageRepository: DmMessageRepository,
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
) {

    /** 상대와의 대화방 get-or-create. */
    @Transactional
    fun getOrCreateRoom(currentUserId: Long, targetUserId: Long): DmRoomResponse {
        if (currentUserId == targetUserId) {
            throw BusinessException(ErrorCode.CANNOT_DM_SELF)
        }
        getUser(targetUserId) // 존재 검증

        val low = minOf(currentUserId, targetUserId)
        val high = maxOf(currentUserId, targetUserId)
        val room = dmRoomRepository.findByUserLowIdAndUserHighId(low, high)
            ?: dmRoomRepository.save(DmRoom.of(currentUserId, targetUserId))

        return DmRoomResponse(
            roomId = requireNotNull(room.id),
            otherUser = userSummary(targetUserId),
        )
    }

    /** 내 대화 목록 (상대 + 마지막 메시지), 최근 메시지 순. */
    @Transactional(readOnly = true)
    fun getMyRooms(currentUserId: Long): List<DmRoomSummaryResponse> =
        dmRoomRepository.findByUserLowIdOrUserHighId(currentUserId, currentUserId)
            .map { room ->
                val roomId = requireNotNull(room.id)
                val lastMessage = dmMessageRepository.findFirstByDmRoomIdOrderByIdDesc(roomId)
                DmRoomSummaryResponse(
                    roomId = roomId,
                    otherUser = userSummary(room.otherUserId(currentUserId)),
                    lastMessageContent = lastMessage?.content,
                    lastMessageAt = lastMessage?.createdAt,
                )
            }
            .sortedByDescending { it.lastMessageAt }

    /** 대화 메시지 이력 (최신순). 참여자만. */
    @Transactional(readOnly = true)
    fun getMessages(roomId: Long, userId: Long, page: Int, size: Int): DmMessagePageResponse {
        requireParticipant(getRoom(roomId), userId)
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))
        val messages = dmMessageRepository.findByDmRoomId(roomId, pageable)
            .map { it.toResponse() }
        return DmMessagePageResponse.of(messages)
    }

    /** 메시지 전송 (REST/실시간 공용). 참여자만. */
    @Transactional
    fun sendMessage(roomId: Long, senderId: Long, content: String): DmMessageResponse {
        val room = getRoom(roomId)
        requireParticipant(room, senderId)
        val message = dmMessageRepository.save(
            DmMessage(dmRoom = room, senderId = senderId, content = content.trim()),
        )
        return message.toResponse()
    }

    private fun DmMessage.toResponse(): DmMessageResponse =
        DmMessageResponse(
            id = requireNotNull(id),
            roomId = requireNotNull(dmRoom.id),
            senderId = senderId,
            content = content,
            createdAt = requireNotNull(createdAt),
        )

    private fun userSummary(userId: Long): DmUserSummary {
        val user = getUser(userId)
        val profile = profileRepository.findByUserId(userId)
        return DmUserSummary(
            userId = requireNotNull(user.id),
            nickname = user.nickname,
            handle = profile?.handle,
            profileImageUrl = profile?.profileImageUrl,
        )
    }

    private fun requireParticipant(room: DmRoom, userId: Long) {
        if (!room.hasParticipant(userId)) {
            throw BusinessException(ErrorCode.NOT_DM_PARTICIPANT)
        }
    }

    private fun getRoom(roomId: Long): DmRoom =
        dmRoomRepository.findById(roomId)
            .orElseThrow { BusinessException(ErrorCode.DM_ROOM_NOT_FOUND) }

    private fun getUser(userId: Long): User =
        userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
}
