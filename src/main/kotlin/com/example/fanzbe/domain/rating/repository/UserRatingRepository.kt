package com.example.fanzbe.domain.rating.repository

import com.example.fanzbe.domain.rating.entity.UserRating
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserRatingRepository : JpaRepository<UserRating, Long> {
    fun findByRaterIdAndTargetIdAndChatRoomId(raterId: Long, targetId: Long, chatRoomId: Long): UserRating?

    fun countByTargetId(targetId: Long): Long

    @Query("select avg(r.score) from UserRating r where r.target.id = :targetId")
    fun findAverageByTargetId(@Param("targetId") targetId: Long): Double?

    fun deleteByChatRoomId(chatRoomId: Long)
}
