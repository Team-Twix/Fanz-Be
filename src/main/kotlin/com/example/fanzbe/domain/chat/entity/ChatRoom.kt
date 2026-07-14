package com.example.fanzbe.domain.chat.entity

import com.example.fanzbe.domain.user.entity.User
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

    // 소갯말
    @Column(length = 500)
    open var description: String? = null,

    @Column(name = "image_url", length = 2048)
    open var imageUrl: String? = null,

    // 해시태그 (여러 개)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "chat_room_hashtags", joinColumns = [JoinColumn(name = "chat_room_id")])
    @Column(name = "hashtag", length = 50)
    open var hashtags: MutableSet<String> = mutableSetOf(),

    // 가입조건 - 연령대 (비어 있으면 "전체": 연령 제한 없음)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "chat_room_age_conditions", joinColumns = [JoinColumn(name = "chat_room_id")])
    @Enumerated(EnumType.STRING)
    @Column(name = "age_group", length = 20)
    open var ageConditions: MutableSet<AgeGroup> = mutableSetOf(),

    // 가입조건 - 성별 (ANY = 전체)
    @Enumerated(EnumType.STRING)
    @Column(name = "gender_condition", nullable = false, length = 10)
    open var genderCondition: Gender = Gender.ANY,

    // 가입조건 - 추가 조건 (자유 태그)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "chat_room_extra_conditions", joinColumns = [JoinColumn(name = "chat_room_id")])
    @Column(name = "condition", length = 100)
    open var extraConditions: MutableSet<String> = mutableSetOf(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_id", nullable = false)
    open var host: User,
) : BaseTimeEntity()
