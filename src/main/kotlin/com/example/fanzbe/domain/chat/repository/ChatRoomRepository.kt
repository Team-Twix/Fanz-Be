package com.example.fanzbe.domain.chat.repository

import com.example.fanzbe.domain.chat.entity.ChatRoom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ChatRoomRepository : JpaRepository<ChatRoom, Long> {

    @Query(
        """
        select distinct cr
        from ChatRoom cr
        where (:hashtag is null or :hashtag member of cr.hashtags)
          and (:keyword is null or lower(cr.name) like lower(concat('%', :keyword, '%')))
        order by cr.createdAt desc, cr.id desc
        """,
    )
    fun search(
        @Param("hashtag") hashtag: String?,
        @Param("keyword") keyword: String?,
    ): List<ChatRoom>
}
