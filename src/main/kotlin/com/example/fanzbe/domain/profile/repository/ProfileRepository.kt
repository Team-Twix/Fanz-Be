package com.example.fanzbe.domain.profile.repository

import com.example.fanzbe.domain.profile.entity.Profile
import org.springframework.data.jpa.repository.JpaRepository

interface ProfileRepository : JpaRepository<Profile, Long> {
    fun findByUserId(userId: Long): Profile?

    fun findByHandle(handle: String): Profile?
}
