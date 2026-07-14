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

    // 아이디 (로그인 ID)
    @Column(nullable = false, unique = true, length = 50)
    open var username: String,

    @Column(nullable = false, length = 255)
    open var password: String,

    // 게임 내 닉네임
    @Column(nullable = false, length = 50)
    open var nickname: String,

    // 나이대 (회원가입에서 설정, 엔티티 레벨은 optional)
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    open var ageGroup: AgeGroup? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'UNSPECIFIED'")
    open var gender: UserGender = UserGender.UNSPECIFIED,

    // 관심 카테고리 (회원가입 - 카테고리 단계)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_interests", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "interest", length = 50)
    open var interests: MutableSet<String> = mutableSetOf(),

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
