package com.example.fanzbe.domain.chat.dto

import org.springframework.data.domain.Page

/**
 * 메시지 이력 페이지 응답. content 는 최신순(id desc).
 */
data class MessagePageResponse(
    val content: List<ChatMessageResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val hasNext: Boolean,
) {
    companion object {
        fun of(page: Page<ChatMessageResponse>): MessagePageResponse =
            MessagePageResponse(
                content = page.content,
                page = page.number,
                size = page.size,
                totalElements = page.totalElements,
                hasNext = page.hasNext(),
            )
    }
}
