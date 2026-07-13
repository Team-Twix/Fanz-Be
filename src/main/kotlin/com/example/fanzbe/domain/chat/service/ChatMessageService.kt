package com.example.fanzbe.domain.chat.service

import com.example.fanzbe.domain.chat.dto.ChatMessageResponse
import com.example.fanzbe.domain.chat.entity.ChatMessage
import com.example.fanzbe.domain.chat.entity.ChatRoom
import com.example.fanzbe.domain.chat.repository.ChatMessageRepository
import com.example.fanzbe.domain.chat.repository.ChatRoomMemberRepository
import com.example.fanzbe.domain.chat.repository.ChatRoomRepository
import com.example.fanzbe.domain.user.entity.User
import com.example.fanzbe.domain.user.repository.UserRepository
import com.example.fanzbe.global.exception.BusinessException
import com.example.fanzbe.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatMessageService(
    private val chatMessageRepository: ChatMessageRepository,
    private val chatRoomRepository: ChatRoomRepository,
    private val chatRoomMemberRepository: ChatRoomMemberRepository,
    private val userRepository: UserRepository,
) {

    @Transactional
    fun sendMessage(roomId: Long, senderUserId: Long, content: String): ChatMessageResponse {
        val chatRoom = getChatRoom(roomId)
        val sender = getUser(senderUserId)

        if (!chatRoomMemberRepository.existsByChatRoomAndUser(chatRoom, sender)) {
            throw BusinessException(ErrorCode.NOT_ROOM_MEMBER)
        }

        val chatMessage = chatMessageRepository.save(
            ChatMessage(
                chatRoom = chatRoom,
                sender = sender,
                content = content.trim(),
            ),
        )

        return chatMessage.toResponse()
    }

    private fun ChatMessage.toResponse(): ChatMessageResponse =
        ChatMessageResponse(
            id = requireNotNull(id) { "Chat message id must not be null." },
            roomId = requireNotNull(chatRoom.id) { "Chat room id must not be null." },
            senderId = requireNotNull(sender.id) { "Sender id must not be null." },
            content = content,
            createdAt = requireNotNull(createdAt) { "Chat message createdAt must not be null." },
        )

    private fun getChatRoom(roomId: Long): ChatRoom =
        chatRoomRepository.findById(roomId)
            .orElseThrow { BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND) }

    private fun getUser(userId: Long): User =
        userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
}
