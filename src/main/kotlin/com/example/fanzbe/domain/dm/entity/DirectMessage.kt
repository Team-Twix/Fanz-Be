package com.example.fanzbe.domain.dm.entity

import com.example.fanzbe.domain.chat.entity.MessageType
import com.example.fanzbe.domain.user.entity.User
import com.example.fanzbe.global.common.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(
    name = "direct_messages",
    indexes = [Index(name = "idx_direct_messages_room_id", columnList = "direct_chat_room_id,id")],
)
open class DirectMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "direct_chat_room_id", nullable = false)
    open var directChatRoom: DirectChatRoom,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    open var sender: User,

    @Column(nullable = false, length = 2000)
    open var content: String,

    @Enumerated(EnumType.STRING)
    @Column(
        name = "message_type",
        nullable = false,
        length = 10,
        columnDefinition = "varchar(10) default 'TEXT'",
    )
    open var messageType: MessageType = MessageType.TEXT,

    @Column(name = "attachment_url", length = 2048)
    open var attachmentUrl: String? = null,
) : BaseTimeEntity()
