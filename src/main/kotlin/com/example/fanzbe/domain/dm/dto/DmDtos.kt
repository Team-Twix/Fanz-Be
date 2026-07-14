package com.example.fanzbe.domain.dm.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.data.domain.Page
import java.time.LocalDateTime

data class CreateDmRoomRequest(
    @field:NotNull
    val targetUserId: Long,
)

data class SendDmMessageRequest(
    @field:NotBlank
    @field:Size(max = 2000)
    val content: String,
)

data class DmUserSummary(
    val userId: Long,
    val nickname: String,
    val handle: String?,
    val profileImageUrl: String?,
)

data class DmRoomResponse(
    val roomId: Long,
    val otherUser: DmUserSummary,
)

data class DmRoomSummaryResponse(
    val roomId: Long,
    val otherUser: DmUserSummary,
    val lastMessageContent: String?,
    val lastMessageAt: LocalDateTime?,
)

data class DmMessageResponse(
    val id: Long,
    val roomId: Long,
    val senderId: Long,
    val content: String,
    val createdAt: LocalDateTime,
)

data class DmMessagePageResponse(
    val content: List<DmMessageResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val hasNext: Boolean,
) {
    companion object {
        fun of(page: Page<DmMessageResponse>): DmMessagePageResponse =
            DmMessagePageResponse(
                content = page.content,
                page = page.number,
                size = page.size,
                totalElements = page.totalElements,
                hasNext = page.hasNext(),
            )
    }
}
