package com.example.fanzbe.domain.chat.entity

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

@Entity
@Table(name = "chat_messages")
open class ChatMessage(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_id", nullable = false)
    open var chatRoom: ChatRoom,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    open var sender: User,

    @Column(nullable = false, length = 2000)
    open var content: String,
) : BaseTimeEntity()
