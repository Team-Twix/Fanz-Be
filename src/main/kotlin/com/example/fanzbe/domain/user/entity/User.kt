package com.example.fanzbe.domain.user.entity

import com.example.fanzbe.global.common.BaseTimeEntity
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table

@Entity
@Table(name = "users")
open class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @Column(nullable = false, length = 255)
    open var password: String,

    @Column(nullable = false, unique = true, length = 50)
    open var nickname: String,

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "user_hashtags",
        joinColumns = [JoinColumn(name = "user_id")],
    )
    @Column(name = "hashtag", nullable = false, length = 50)
    open var hashtags: MutableSet<String> = mutableSetOf(),

    @Column(nullable = false)
    open var mannerScore: Double = DEFAULT_MANNER_SCORE,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    open var role: Role = Role.USER,
) : BaseTimeEntity() {
    companion object {
        const val DEFAULT_MANNER_SCORE = 36.5
    }
}
