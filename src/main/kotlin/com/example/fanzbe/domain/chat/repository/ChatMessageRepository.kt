package com.example.fanzbe.domain.chat.repository

import com.example.fanzbe.domain.chat.entity.ChatMessage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {
    fun countByChatRoomIdAndCreatedAtGreaterThanEqual(
        chatRoomId: Long,
        createdAt: LocalDateTime,
    ): Long

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
