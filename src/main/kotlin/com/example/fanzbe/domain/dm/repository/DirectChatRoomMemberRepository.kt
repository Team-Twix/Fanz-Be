package com.example.fanzbe.domain.dm.repository

import com.example.fanzbe.domain.dm.entity.DirectChatRoomMember
import org.springframework.data.jpa.repository.JpaRepository

interface DirectChatRoomMemberRepository : JpaRepository<DirectChatRoomMember, Long> {
    fun existsByDirectChatRoomIdAndUserId(directChatRoomId: Long, userId: Long): Boolean

    fun findByDirectChatRoomIdAndUserId(directChatRoomId: Long, userId: Long): DirectChatRoomMember?
}
