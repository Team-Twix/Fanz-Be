package com.example.fanzbe.domain.user.repository

import com.example.fanzbe.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserRepository : JpaRepository<User, Long> {
    fun findByNickname(nickname: String): User?

    fun existsByNickname(nickname: String): Boolean

    fun findByNicknameContainingIgnoreCase(nickname: String): List<User>

    @Query(
        """
        select distinct user
        from User user
        join user.hashtags hashtag
        where lower(hashtag) = lower(:hashtag)
        """,
    )
    fun findByHashtagIgnoreCase(@Param("hashtag") hashtag: String): List<User>
}
