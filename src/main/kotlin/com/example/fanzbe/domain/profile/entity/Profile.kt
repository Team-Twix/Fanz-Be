package com.example.fanzbe.domain.profile.entity

import com.example.fanzbe.domain.user.entity.User
import com.example.fanzbe.global.common.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "profiles")
open class Profile(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    open var user: User,

    // "@핸들" 의 핸들 문자열 (@ 제외 저장), 유니크
    @Column(unique = true, length = 30)
    open var handle: String? = null,

    // 자기소개
    @Column(length = 500)
    open var bio: String? = null,

    @Column(name = "profile_image_url", length = 2048)
    open var profileImageUrl: String? = null,

    @Column(name = "cover_image_url", length = 2048)
    open var coverImageUrl: String? = null,
) : BaseTimeEntity()
