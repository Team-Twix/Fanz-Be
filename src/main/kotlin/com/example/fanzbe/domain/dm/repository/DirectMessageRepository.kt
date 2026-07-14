package com.example.fanzbe.domain.dm.repository

import com.example.fanzbe.domain.dm.entity.DirectMessage
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface DirectMessageRepository : JpaRepository<DirectMessage, Long> {
    fun findByDirectChatRoomId(directChatRoomId: Long, pageable: Pageable): Page<DirectMessage>

    fun findTopByDirectChatRoomIdOrderByIdDesc(directChatRoomId: Long): DirectMessage?

    fun countByDirectChatRoomIdAndIdGreaterThan(directChatRoomId: Long, id: Long): Long
}
