package com.example.fanzbe.domain.chat.service

import com.example.fanzbe.domain.chat.dto.ChatRoomResponse
import com.example.fanzbe.domain.chat.dto.CreateChatRoomRequest
import com.example.fanzbe.domain.chat.dto.PopularChatRoomResponse
import com.example.fanzbe.domain.chat.entity.ChatRoom
import com.example.fanzbe.domain.chat.entity.ChatRoomMember
import com.example.fanzbe.domain.chat.repository.ChatMessageRepository
import com.example.fanzbe.domain.chat.repository.ChatRoomMemberRepository
import com.example.fanzbe.domain.chat.repository.ChatRoomRepository
import com.example.fanzbe.domain.user.entity.User
import com.example.fanzbe.domain.user.repository.UserRepository
import com.example.fanzbe.global.exception.BusinessException
import com.example.fanzbe.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ChatRoomService(
    private val chatRoomRepository: ChatRoomRepository,
    private val chatRoomMemberRepository: ChatRoomMemberRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val userRepository: UserRepository,
) {

    @Transactional
    fun createRoom(hostUserId: Long, request: CreateChatRoomRequest): ChatRoomResponse {
        val host = getUser(hostUserId)
        val chatRoom = chatRoomRepository.save(
            ChatRoom(
                name = request.name.trim(),
                description = request.description.normalizeNullable(),
                imageUrl = request.imageUrl.normalizeNullable(),
                hashtags = request.hashtags.normalizeTags(),
                ageConditions = request.ageConditions.toMutableSet(),
                genderCondition = request.gender,
                extraConditions = request.extraConditions.normalizeTags(),
                host = host,
            ),
        )

        chatRoomMemberRepository.save(
            ChatRoomMember(
                chatRoom = chatRoom,
                user = host,
            ),
        )

        return chatRoom.toResponse()
    }

    @Transactional(readOnly = true)
    fun getRooms(hashtag: String?, keyword: String?): List<ChatRoomResponse> =
        chatRoomRepository.search(
            hashtag = hashtag.normalizeNullable(),
            keyword = keyword.normalizeNullable(),
        ).map { chatRoom ->
            chatRoom.toResponse()
        }

    @Transactional
    fun joinRoom(roomId: Long, userId: Long) {
        val chatRoom = getChatRoom(roomId)
        val user = getUser(userId)

        if (chatRoomMemberRepository.existsByChatRoomAndUser(chatRoom, user)) {
            throw BusinessException(ErrorCode.ALREADY_JOINED)
        }

        chatRoomMemberRepository.save(
            ChatRoomMember(
                chatRoom = chatRoom,
                user = user,
            ),
        )
    }

    /** 방장이 멤버를 추방. */
    @Transactional
    fun kickMember(roomId: Long, hostUserId: Long, targetUserId: Long) {
        val chatRoom = getChatRoom(roomId)
        requireHost(chatRoom, hostUserId)
        if (targetUserId == chatRoom.host.id) {
            throw BusinessException(ErrorCode.CANNOT_KICK_HOST)
        }
        val target = getUser(targetUserId)
        val membership = chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, target)
            ?: throw BusinessException(ErrorCode.NOT_ROOM_MEMBER)
        chatRoomMemberRepository.delete(membership)
    }

    /** 멤버가 채팅방을 나감. 방장은 나갈 수 없음(방 삭제 필요). */
    @Transactional
    fun leaveRoom(roomId: Long, userId: Long) {
        val chatRoom = getChatRoom(roomId)
        if (chatRoom.host.id == userId) {
            throw BusinessException(ErrorCode.CANNOT_LEAVE_AS_HOST)
        }
        val user = getUser(userId)
        val membership = chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, user)
            ?: throw BusinessException(ErrorCode.NOT_ROOM_MEMBER)
        chatRoomMemberRepository.delete(membership)
    }

    /** 방장이 채팅방을 삭제(멤버·메시지 함께 정리). */
    @Transactional
    fun deleteRoom(roomId: Long, hostUserId: Long) {
        val chatRoom = getChatRoom(roomId)
        requireHost(chatRoom, hostUserId)
        chatMessageRepository.deleteByChatRoomId(roomId)
        chatRoomMemberRepository.deleteByChatRoomId(roomId)
        chatRoomRepository.delete(chatRoom)
    }

    private fun requireHost(chatRoom: ChatRoom, userId: Long) {
        if (chatRoom.host.id != userId) {
            throw BusinessException(ErrorCode.NOT_ROOM_HOST)
        }
    }

    @Transactional(readOnly = true)
    fun getPopularRooms(limit: Int): List<PopularChatRoomResponse> {
        val monthStart = ChatRoomStatistics.currentMonthStart()
        val normalizedLimit = limit.coerceAtLeast(0)

        return chatRoomRepository.findAll()
            .map { chatRoom ->
                chatRoom to chatRoomMemberRepository.countByChatRoom(chatRoom)
            }
            .sortedWith(
                compareByDescending<Pair<ChatRoom, Long>> { (_, memberCount) -> memberCount }
                    .thenByDescending { (chatRoom, _) -> chatRoom.id ?: 0L },
            )
            .take(normalizedLimit)
            .map { (chatRoom, memberCount) ->
                chatRoom.toPopularResponse(
                    memberCount = memberCount,
                    monthStart = monthStart,
                )
            }
    }

    private fun ChatRoom.toResponse(): ChatRoomResponse =
        ChatRoomResponse(
            id = requireNotNull(id) { "Chat room id must not be null." },
            name = name,
            description = description,
            imageUrl = imageUrl,
            hashtags = hashtags.toList(),
            ageConditions = ageConditions.toList(),
            gender = genderCondition,
            extraConditions = extraConditions.toList(),
            hostId = requireNotNull(host.id) { "Chat room host id must not be null." },
            memberCount = chatRoomMemberRepository.countByChatRoom(this),
        )

    private fun ChatRoom.toPopularResponse(
        memberCount: Long,
        monthStart: LocalDateTime,
    ): PopularChatRoomResponse {
        val roomId = requireNotNull(id) { "Chat room id must not be null." }
        val monthlyMessageCount = chatMessageRepository.countByChatRoomIdAndCreatedAtGreaterThanEqual(
            chatRoomId = roomId,
            createdAt = monthStart,
        )
        val activeMemberCount = chatMessageRepository.findActiveSenderIds(
            roomId = roomId,
            monthStart = monthStart,
            threshold = ChatRoomStatistics.MONTHLY_ACTIVE_MESSAGE_THRESHOLD.toLong(),
        ).size.toLong()

        return PopularChatRoomResponse(
            id = roomId,
            name = name,
            imageUrl = imageUrl,
            hashtags = hashtags.toList(),
            memberCount = memberCount,
            monthlyMessageCount = monthlyMessageCount,
            activityRate = ChatRoomStatistics.calculateActivityRate(
                memberCount = memberCount,
                activeMemberCount = activeMemberCount,
            ),
        )
    }

    private fun getChatRoom(roomId: Long): ChatRoom =
        chatRoomRepository.findById(roomId)
            .orElseThrow { BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND) }

    private fun getUser(userId: Long): User =
        userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

    private fun String?.normalizeNullable(): String? =
        this?.trim()?.takeIf { it.isNotEmpty() }

    private fun List<String>.normalizeTags(): MutableSet<String> =
        mapNotNull { it.trim().takeIf(String::isNotEmpty) }.toMutableSet()
}
