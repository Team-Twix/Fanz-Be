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
          and i in :interests
        """,
    )
    fun findCandidatesByInterests(
        @Param("userId") userId: Long,
        @Param("interests") interests: Collection<String>,
    ): List<User>
}
