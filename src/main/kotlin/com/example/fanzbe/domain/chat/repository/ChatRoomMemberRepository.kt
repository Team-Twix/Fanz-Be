package com.example.fanzbe.domain.chat.repository

import com.example.fanzbe.domain.chat.entity.ChatRoom
import com.example.fanzbe.domain.chat.entity.ChatRoomMember
import com.example.fanzbe.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface ChatRoomMemberRepository : JpaRepository<ChatRoomMember, Long> {
    fun existsByChatRoomAndUser(chatRoom: ChatRoom, user: User): Boolean

    fun countByChatRoom(chatRoom: ChatRoom): Long

    fun findByChatRoomAndUser(chatRoom: ChatRoom, user: User): ChatRoomMember?

    // 유저가 참여중인 채팅방 멤버십 목록 (마이프로필의 "참여중인 단체 채팅")
    fun findByUserId(userId: Long): List<ChatRoomMember>
}
