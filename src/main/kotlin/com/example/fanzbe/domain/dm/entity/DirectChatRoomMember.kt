package com.example.fanzbe.domain.dm.entity

import com.example.fanzbe.domain.user.entity.User
import com.example.fanzbe.global.common.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Table(
    name = "direct_chat_room_members",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_direct_chat_room_members_room_user",
            columnNames = ["direct_chat_room_id", "user_id"],
        ),
    ],
)
open class DirectChatRoomMember(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "direct_chat_room_id", nullable = false)
    open var directChatRoom: DirectChatRoom,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    open var user: User,

    @Column(nullable = false)
    open var joinedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "last_read_message_id")
    open var lastReadMessageId: Long? = null,
) : BaseTimeEntity()
