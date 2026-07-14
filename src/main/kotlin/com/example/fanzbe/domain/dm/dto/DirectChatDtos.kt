package com.example.fanzbe.domain.dm.dto

import com.example.fanzbe.domain.chat.entity.MessageType
import java.time.LocalDateTime
import org.springframework.data.domain.Page

data class DirectChatRoomResponse(
    val id: Long,
    val otherUserId: Long,
    val otherUserNickname: String,
    val otherUserHandle: String?,
    val otherUserProfileImageUrl: String?,
    val lastMessage: String?,
    val lastMessageType: MessageType?,
    val lastMessageAt: LocalDateTime?,
    val unreadCount: Long,
)

data class DirectMessageResponse(
    val id: Long,
    val dmRoomId: Long,
    val senderId: Long,
    val senderNickname: String,
    val senderProfileImageUrl: String?,
    val content: String,
    val messageType: MessageType,
    val attachmentUrl: String?,
    val createdAt: LocalDateTime,
)

data class DirectMessagePageResponse(
    val content: List<DirectMessageResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val hasNext: Boolean,
) {
    companion object {
        fun of(page: Page<DirectMessageResponse>): DirectMessagePageResponse =
            DirectMessagePageResponse(
                content = page.content,
                page = page.number,
                size = page.size,
                totalElements = page.totalElements,
                hasNext = page.hasNext(),
            )
    }
}
