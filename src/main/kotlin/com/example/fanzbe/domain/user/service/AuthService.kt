package com.example.fanzbe.domain.user.service

import com.example.fanzbe.domain.user.dto.SignupRequest
import com.example.fanzbe.domain.user.dto.SignupResponse
import com.example.fanzbe.domain.user.entity.User
import com.example.fanzbe.domain.user.repository.UserRepository
import com.example.fanzbe.global.exception.BusinessException
import com.example.fanzbe.global.exception.ErrorCode
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    @Transactional
    fun signup(request: SignupRequest): SignupResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw BusinessException(ErrorCode.EMAIL_DUPLICATED)
        }

        val encodedPassword = requireNotNull(passwordEncoder.encode(request.password)) {
            "Encoded password must not be null."
        }
        val user = User(
            email = request.email,
            password = encodedPassword,
            nickname = request.nickname,
        )
        val savedUser = userRepository.save(user)

        return SignupResponse(
            userId = checkNotNull(savedUser.id),
        )
    }
}
