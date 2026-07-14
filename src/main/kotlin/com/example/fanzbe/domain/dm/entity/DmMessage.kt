package com.example.fanzbe.domain.dm.entity

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
@Table(name = "dm_messages")
open class DmMessage(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dm_room_id", nullable = false)
    open var dmRoom: DmRoom,

    @Column(name = "sender_id", nullable = false)
    open var senderId: Long,

    @Column(nullable = false, length = 2000)
    open var content: String,
) : BaseTimeEntity()
