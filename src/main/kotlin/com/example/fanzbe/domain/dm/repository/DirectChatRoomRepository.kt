package com.example.fanzbe.domain.dm.repository

import com.example.fanzbe.domain.dm.entity.DirectChatRoom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface DirectChatRoomRepository : JpaRepository<DirectChatRoom, Long> {
    fun findByUserOneIdAndUserTwoId(userOneId: Long, userTwoId: Long): DirectChatRoom?

    @Query(
        """
        select dcr from DirectChatRoom dcr
        where dcr.userOne.id = :userId or dcr.userTwo.id = :userId
        order by dcr.id desc
        """,
    )
    fun findByParticipantId(@Param("userId") userId: Long): List<DirectChatRoom>
}
