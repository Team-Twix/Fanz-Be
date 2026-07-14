package com.example.fanzbe.domain.rating.entity

import com.example.fanzbe.domain.chat.entity.ChatRoom
import com.example.fanzbe.domain.user.entity.User
import com.example.fanzbe.global.common.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "user_ratings",
    indexes = [Index(name = "idx_user_ratings_target_id", columnList = "target_id")],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_user_ratings_rater_target_room",
            columnNames = ["rater_id", "target_id", "chat_room_id"],
        ),
    ],
)
open class UserRating(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rater_id", nullable = false)
    open var rater: User,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_id", nullable = false)
    open var target: User,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_id", nullable = false)
    open var chatRoom: ChatRoom,

    @Column(nullable = false)
    open var score: Int,
) : BaseTimeEntity()
