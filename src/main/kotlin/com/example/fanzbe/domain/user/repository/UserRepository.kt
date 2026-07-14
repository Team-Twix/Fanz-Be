package com.example.fanzbe.domain.user.repository

import com.example.fanzbe.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): User?

    fun existsByUsername(username: String): Boolean

    @Query(
        """
        select distinct u
        from User u
        join u.interests i
        where u.id <> :userId
          and lower(i) in :interests
        """,
    )
    fun findCandidatesByInterests(
        @Param("userId") userId: Long,
        @Param("interests") interests: Collection<String>,
    ): List<User>

    @Query(
        """
        select distinct u from User u
        left join u.interests i
        where u.id <> :currentUserId
          and (:nickname is null or lower(u.nickname) like lower(concat('%', :nickname, '%')))
          and (:hashtag is null or lower(i) = :hashtag)
        order by u.id desc
        """,
    )
    fun search(
        @Param("currentUserId") currentUserId: Long,
        @Param("nickname") nickname: String?,
        @Param("hashtag") hashtag: String?,
    ): List<User>
}
