package com.example.fanzbe.domain.follow.repository

import com.example.fanzbe.domain.follow.entity.Follow
import org.springframework.data.jpa.repository.JpaRepository

interface FollowRepository : JpaRepository<Follow, Long> {
    // 팔로워 수: 나를 followee 로 가리키는 관계 수
    fun countByFolloweeId(followeeId: Long): Long

    // 팔로잉 수: 내가 follower 인 관계 수
    fun countByFollowerId(followerId: Long): Long

    fun existsByFollowerIdAndFolloweeId(followerId: Long, followeeId: Long): Boolean

    fun deleteByFollowerIdAndFolloweeId(followerId: Long, followeeId: Long)
}
