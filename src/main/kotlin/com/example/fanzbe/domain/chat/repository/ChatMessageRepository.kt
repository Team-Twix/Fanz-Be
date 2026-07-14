package com.example.fanzbe.domain.chat.repository

import com.example.fanzbe.domain.chat.entity.ChatMessage
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {
    fun countByChatRoomIdAndCreatedAtGreaterThanEqual(
        chatRoomId: Long,
        createdAt: LocalDateTime,
    ): Long

    // 메시지 이력 조회 (페이지네이션). 정렬은 Pageable 로 전달.
    fun findByChatRoomId(chatRoomId: Long, pageable: Pageable): Page<ChatMessage>

    @Query(
        """
        select cm.sender.id
        from ChatMessage cm
        where cm.chatRoom.id = :roomId
          and cm.createdAt >= :monthStart
        group by cm.sender.id
        having count(cm) >= :threshold
        """,
    )
    fun findActiveSenderIds(
        @Param("roomId") roomId: Long,
        @Param("monthStart") monthStart: LocalDateTime,
        @Param("threshold") threshold: Long,
    ): List<Long>
}
