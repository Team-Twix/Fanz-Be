package com.example.fanzbe.domain.dm.service

import com.example.fanzbe.domain.chat.dto.SendMessageRequest
import com.example.fanzbe.domain.chat.entity.MessageType
import com.example.fanzbe.domain.dm.dto.DirectChatRoomResponse
import com.example.fanzbe.domain.dm.dto.DirectMessagePageResponse
import com.example.fanzbe.domain.dm.dto.DirectMessageResponse
import com.example.fanzbe.domain.dm.entity.DirectChatRoom
import com.example.fanzbe.domain.dm.entity.DirectChatRoomMember
import com.example.fanzbe.domain.dm.entity.DirectMessage
import com.example.fanzbe.domain.dm.repository.DirectChatRoomMemberRepository
import com.example.fanzbe.domain.dm.repository.DirectChatRoomRepository
import com.example.fanzbe.domain.dm.repository.DirectMessageRepository
import com.example.fanzbe.domain.profile.repository.ProfileRepository
import com.example.fanzbe.domain.user.entity.User
import com.example.fanzbe.domain.user.repository.UserRepository
import com.example.fanzbe.global.exception.BusinessException
import com.example.fanzbe.global.exception.ErrorCode
import java.time.LocalDateTime
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DirectChatService(
    private val directChatRoomRepository: DirectChatRoomRepository,
    private val directChatRoomMemberRepository: DirectChatRoomMemberRepository,
    private val directMessageRepository: DirectMessageRepository,
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
) {

    @Transactional
    fun openRoom(currentUserId: Long, targetUserId: Long): DirectChatRoomResponse {
        if (currentUserId == targetUserId) {
            throw BusinessException(ErrorCode.CANNOT_DM_SELF)
        }
        val currentUser = getUser(currentUserId)
        val targetUser = getUser(targetUserId)
        val (userOne, userTwo) = orderedPair(currentUser, targetUser)
        val room = directChatRoomRepository.findByUserOneIdAndUserTwoId(
            requireNotNull(userOne.id),
            requireNotNull(userTwo.id),
        ) ?: createRoom(userOne, userTwo)

        return room.toRoomResponse(currentUserId)
    }

    @Transactional(readOnly = true)
    fun getRooms(userId: Long): List<DirectChatRoomResponse> {
        getUser(userId)
        return directChatRoomRepository.findByParticipantId(userId)
            .map { it.toRoomResponse(userId) }
            .sortedWith(
                compareByDescending<DirectChatRoomResponse> { it.lastMessageAt ?: LocalDateTime.MIN }
                    .thenByDescending { it.id },
            )
    }

    @Transactional(readOnly = true)
    fun getRoom(roomId: Long, userId: Long): DirectChatRoomResponse =
        requireMember(roomId, userId).directChatRoom.toRoomResponse(userId)

    @Transactional
    fun sendMessage(roomId: Long, senderId: Long, request: SendMessageRequest): DirectMessageResponse {
        val membership = requireMember(roomId, senderId)
        val sender = membership.user
        val normalizedContent = request.content?.trim().orEmpty()
        val normalizedAttachmentUrl = request.attachmentUrl?.trim()?.takeIf { it.isNotEmpty() }
        validateMessage(normalizedContent, request.messageType, normalizedAttachmentUrl)

        val message = directMessageRepository.save(
            DirectMessage(
                directChatRoom = membership.directChatRoom,
                sender = sender,
                content = normalizedContent,
                messageType = request.messageType,
                attachmentUrl = normalizedAttachmentUrl,
            ),
        )
        membership.lastReadMessageId = requireNotNull(message.id)
        return message.toResponse()
    }

    @Transactional
    fun getMessages(roomId: Long, userId: Long, page: Int, size: Int): DirectMessagePageResponse {
        val membership = requireMember(roomId, userId)
        val pageable = PageRequest.of(
            page.coerceAtLeast(0),
            size.coerceIn(1, MAX_PAGE_SIZE),
            Sort.by(Sort.Direction.DESC, "id"),
        )
        val messages = directMessageRepository.findByDirectChatRoomId(roomId, pageable)
            .map { it.toResponse() }
        directMessageRepository.findTopByDirectChatRoomIdOrderByIdDesc(roomId)?.id?.let {
            membership.lastReadMessageId = it
        }
        return DirectMessagePageResponse.of(messages)
    }

    private fun createRoom(userOne: User, userTwo: User): DirectChatRoom {
        val room = directChatRoomRepository.save(DirectChatRoom(userOne = userOne, userTwo = userTwo))
        directChatRoomMemberRepository.saveAll(
            listOf(
                DirectChatRoomMember(directChatRoom = room, user = userOne),
                DirectChatRoomMember(directChatRoom = room, user = userTwo),
            ),
        )
        return room
    }

    private fun DirectChatRoom.toRoomResponse(currentUserId: Long): DirectChatRoomResponse {
        val roomId = requireNotNull(id) { "DM room id must not be null." }
        val otherUser = if (userOne.id == currentUserId) userTwo else userOne
        val otherUserId = requireNotNull(otherUser.id) { "DM participant id must not be null." }
        val otherProfile = profileRepository.findByUserId(otherUserId)
        val membership = directChatRoomMemberRepository.findByDirectChatRoomIdAndUserId(roomId, currentUserId)
            ?: throw BusinessException(ErrorCode.NOT_DM_MEMBER)
        val lastMessage = directMessageRepository.findTopByDirectChatRoomIdOrderByIdDesc(roomId)

        return DirectChatRoomResponse(
            id = roomId,
            otherUserId = otherUserId,
            otherUserNickname = otherUser.nickname,
            otherUserHandle = otherProfile?.handle,
            otherUserProfileImageUrl = otherProfile?.profileImageUrl,
            lastMessage = lastMessage?.content?.takeIf { it.isNotBlank() },
            lastMessageType = lastMessage?.messageType,
            lastMessageAt = lastMessage?.createdAt,
            unreadCount = directMessageRepository.countByDirectChatRoomIdAndIdGreaterThan(
                directChatRoomId = roomId,
                id = membership.lastReadMessageId ?: 0L,
            ),
        )
    }

    private fun DirectMessage.toResponse(): DirectMessageResponse {
        val senderId = requireNotNull(sender.id) { "DM sender id must not be null." }
        return DirectMessageResponse(
            id = requireNotNull(id) { "DM message id must not be null." },
            dmRoomId = requireNotNull(directChatRoom.id) { "DM room id must not be null." },
            senderId = senderId,
            senderNickname = sender.nickname,
            senderProfileImageUrl = profileRepository.findByUserId(senderId)?.profileImageUrl,
            content = content,
            messageType = messageType,
            attachmentUrl = attachmentUrl,
            createdAt = requireNotNull(createdAt) { "DM message createdAt must not be null." },
        )
    }

    private fun requireMember(roomId: Long, userId: Long): DirectChatRoomMember {
        if (!directChatRoomRepository.existsById(roomId)) {
            throw BusinessException(ErrorCode.DM_ROOM_NOT_FOUND)
        }
        return directChatRoomMemberRepository.findByDirectChatRoomIdAndUserId(roomId, userId)
            ?: throw BusinessException(ErrorCode.NOT_DM_MEMBER)
    }

    private fun validateMessage(content: String, messageType: MessageType, attachmentUrl: String?) {
        val valid = when (messageType) {
            MessageType.TEXT -> content.isNotEmpty()
            MessageType.IMAGE, MessageType.FILE -> attachmentUrl != null
        }
        if (!valid) {
            throw BusinessException(ErrorCode.INVALID_CHAT_MESSAGE)
        }
    }

    private fun orderedPair(first: User, second: User): Pair<User, User> =
        if (requireNotNull(first.id) < requireNotNull(second.id)) first to second else second to first

    private fun getUser(userId: Long): User =
        userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

    companion object {
        private const val MAX_PAGE_SIZE = 100
    }
}
