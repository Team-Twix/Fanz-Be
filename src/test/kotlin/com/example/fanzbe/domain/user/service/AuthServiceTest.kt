package com.example.fanzbe.domain.user.service

import com.example.fanzbe.domain.user.dto.SignupRequest
import com.example.fanzbe.domain.user.entity.Role
import com.example.fanzbe.domain.user.entity.User
import com.example.fanzbe.domain.user.repository.UserRepository
import com.example.fanzbe.global.exception.BusinessException
import com.example.fanzbe.global.exception.ErrorCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.springframework.security.crypto.password.PasswordEncoder

class AuthServiceTest {

    private val userRepository = Mockito.mock(UserRepository::class.java)
    private val passwordEncoder = Mockito.mock(PasswordEncoder::class.java)
    private val authService = AuthService(userRepository, passwordEncoder)

    @Test
    fun `signup encodes password and saves user`() {
        val request = SignupRequest(
            email = "fan@example.com",
            password = "password123",
            nickname = "fan",
        )
        Mockito.`when`(userRepository.existsByEmail(request.email)).thenReturn(false)
        Mockito.`when`(passwordEncoder.encode(request.password)).thenReturn("encoded-password")
        Mockito.`when`(userRepository.save(Mockito.any(User::class.java))).thenAnswer { invocation ->
            invocation.getArgument<User>(0).apply {
                id = 1L
            }
        }

        val response = authService.signup(request)

        assertEquals(1L, response.userId)

        val userCaptor = ArgumentCaptor.forClass(User::class.java)
        Mockito.verify(userRepository).save(userCaptor.capture())
        val savedUser = userCaptor.value
        assertEquals(request.email, savedUser.email)
        assertEquals("encoded-password", savedUser.password)
        assertEquals(request.nickname, savedUser.nickname)
        assertEquals(User.DEFAULT_MANNER_SCORE, savedUser.mannerScore)
        assertEquals(Role.USER, savedUser.role)
        Mockito.verify(passwordEncoder).encode(request.password)
    }

    @Test
    fun `signup throws business exception when email is duplicated`() {
        val request = SignupRequest(
            email = "fan@example.com",
            password = "password123",
            nickname = "fan",
        )
        Mockito.`when`(userRepository.existsByEmail(request.email)).thenReturn(true)

        val exception = assertFailsWith<BusinessException> {
            authService.signup(request)
        }

        assertEquals(ErrorCode.EMAIL_DUPLICATED, exception.errorCode)
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User::class.java))
        Mockito.verify(passwordEncoder, Mockito.never()).encode(Mockito.anyString())
    }
}
