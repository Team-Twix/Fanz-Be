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
                category = request.category.trim(),
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
    fun getRooms(category: String?, keyword: String?): List<ChatRoomResponse> =
        chatRoomRepository.search(
            category = category.normalizeNullable(),
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
            category = category,
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
            category = category,
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
}
