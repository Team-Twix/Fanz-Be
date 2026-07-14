package com.example.fanzbe.domain.auth.service

import com.example.fanzbe.domain.auth.dto.LoginRequest
import com.example.fanzbe.domain.auth.dto.ReissueRequest
import com.example.fanzbe.domain.auth.dto.SignupRequest
import com.example.fanzbe.domain.auth.repository.RefreshTokenRepository
import com.example.fanzbe.domain.user.entity.AgeGroup
import com.example.fanzbe.domain.user.repository.UserRepository
import com.example.fanzbe.global.exception.BusinessException
import com.example.fanzbe.global.exception.ErrorCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceTest(
    @Autowired private val authService: AuthService,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val refreshTokenRepository: RefreshTokenRepository,
    @Autowired private val passwordEncoder: PasswordEncoder,
) {

    private fun signupRequest(
        username: String = "fanuser",
        password: String = "password123",
        passwordConfirm: String = "password123",
        ageGroup: AgeGroup = AgeGroup.TWENTIES,
    ) = SignupRequest(
        username = username,
        password = password,
        passwordConfirm = passwordConfirm,
        nickname = "fan",
        ageGroup = ageGroup,
        interests = listOf("에반게리온", " "),
    )

    @Test
    fun `회원가입 - 비밀번호 인코딩 및 나이대·관심사 저장`() {
        val response = authService.signup(signupRequest())

        val user = userRepository.findById(response.userId).orElseThrow()
        assertTrue(passwordEncoder.matches("password123", user.password))
        assertEquals(AgeGroup.TWENTIES, user.ageGroup)
        assertEquals(setOf("에반게리온"), user.interests) // 공백 항목은 제거
    }

    @Test
    fun `회원가입 - 비밀번호 확인 불일치면 예외`() {
        val ex = assertFailsWith<BusinessException> {
            authService.signup(signupRequest(passwordConfirm = "different"))
        }
        assertEquals(ErrorCode.PASSWORD_MISMATCH, ex.errorCode)
    }

    @Test
    fun `회원가입 - 아이디 중복이면 예외`() {
        authService.signup(signupRequest(username = "dupuser"))
        val ex = assertFailsWith<BusinessException> {
            authService.signup(signupRequest(username = "dupuser"))
        }
        assertEquals(ErrorCode.USERNAME_DUPLICATED, ex.errorCode)
    }

    @Test
    fun `로그인 - 성공 시 토큰 발급 및 refresh 저장`() {
        val userId = authService.signup(signupRequest()).userId

        val tokens = authService.login(LoginRequest(username = "fanuser", password = "password123"))

        assertTrue(tokens.accessToken.isNotBlank())
        assertTrue(tokens.refreshToken.isNotBlank())
        assertNotNull(refreshTokenRepository.findByUserId(userId))
    }

    @Test
    fun `로그인 - 비밀번호 틀리면 INVALID_CREDENTIALS`() {
        authService.signup(signupRequest())
        val ex = assertFailsWith<BusinessException> {
            authService.login(LoginRequest(username = "fanuser", password = "wrongpassword"))
        }
        assertEquals(ErrorCode.INVALID_CREDENTIALS, ex.errorCode)
    }

    @Test
    fun `재발급 - 저장된 refresh 로 새 토큰 발급`() {
        authService.signup(signupRequest())
        val login = authService.login(LoginRequest(username = "fanuser", password = "password123"))

        val reissued = authService.reissue(ReissueRequest(refreshToken = login.refreshToken))

        assertTrue(reissued.accessToken.isNotBlank())
        assertTrue(reissued.refreshToken.isNotBlank())
    }

    @Test
    fun `로그아웃 - refresh 삭제`() {
        val userId = authService.signup(signupRequest()).userId
        authService.login(LoginRequest(username = "fanuser", password = "password123"))

        authService.logout(userId)

        assertNull(refreshTokenRepository.findByUserId(userId))
    }
}
