package com.example.fanzbe.domain.dm.entity

import com.example.fanzbe.global.common.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

/**
 * 1:1 DM 대화방. 두 참여자를 (작은 id, 큰 id)로 정규화 저장해 쌍당 1개를 보장한다.
 */
@Entity
@Table(
    name = "dm_rooms",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_dm_rooms_user_low_high",
            columnNames = ["user_low_id", "user_high_id"],
        ),
    ],
)
open class DmRoom(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @Column(name = "user_low_id", nullable = false)
    open var userLowId: Long,

    @Column(name = "user_high_id", nullable = false)
    open var userHighId: Long,
) : BaseTimeEntity() {

    fun otherUserId(userId: Long): Long =
        if (userId == userLowId) userHighId else userLowId

    fun hasParticipant(userId: Long): Boolean =
        userId == userLowId || userId == userHighId

    companion object {
        fun of(userIdA: Long, userIdB: Long): DmRoom =
            DmRoom(
                userLowId = minOf(userIdA, userIdB),
                userHighId = maxOf(userIdA, userIdB),
            )
    }
}
