package com.example.fanzbe.domain.chat.repository

import com.example.fanzbe.domain.chat.entity.ChatRoom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ChatRoomRepository : JpaRepository<ChatRoom, Long> {

    @Query(
        """
        select cr
        from ChatRoom cr
        where (:category is null or cr.category = :category)
          and (:keyword is null or lower(cr.name) like lower(concat('%', :keyword, '%')))
        order by cr.createdAt desc, cr.id desc
        """,
    )
    fun search(
        @Param("category") category: String?,
        @Param("keyword") keyword: String?,
    ): List<ChatRoom>
}
