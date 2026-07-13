package com.example.fanzbe.global.security

import com.example.fanzbe.domain.user.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails =
        userRepository.findByEmail(username)?.let(::CustomUserDetails)
            ?: throw UsernameNotFoundException("User not found by email: $username")

    fun loadUserById(userId: Long): CustomUserDetails =
        userRepository.findById(userId)
            .map(::CustomUserDetails)
            .orElseThrow { UsernameNotFoundException("User not found by id: $userId") }
}
