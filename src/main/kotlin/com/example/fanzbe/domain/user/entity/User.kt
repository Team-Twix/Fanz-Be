package com.example.fanzbe.domain.user.entity

import com.example.fanzbe.global.common.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "users")
open class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @Column(nullable = false, unique = true, length = 255)
    open var email: String,

    @Column(nullable = false, length = 255)
    open var password: String,

    @Column(nullable = false, length = 50)
    open var nickname: String,

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
