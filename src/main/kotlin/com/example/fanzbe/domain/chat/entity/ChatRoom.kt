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
@Table(name = "chat_rooms")
open class ChatRoom(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @Column(nullable = false, length = 100)
    open var name: String,

    @Column(length = 500)
    open var description: String? = null,

    @Column(name = "image_url", length = 2048)
    open var imageUrl: String? = null,

    @Column(nullable = false, length = 50)
    open var category: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_id", nullable = false)
    open var host: User,
) : BaseTimeEntity()
