package com.example.fanzbe.global.security

import com.example.fanzbe.domain.user.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(
    val user: User,
) : UserDetails {

    val id: Long
        get() = requireNotNull(user.id) { "Authenticated user id must not be null." }

    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))

    override fun getPassword(): String =
        user.password

    override fun getUsername(): String =
        user.username
}
