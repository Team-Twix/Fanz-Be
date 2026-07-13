package com.example.fanzbe.domain.auth.controller

import com.example.fanzbe.domain.auth.dto.LoginRequest
import com.example.fanzbe.domain.auth.dto.ReissueRequest
import com.example.fanzbe.domain.auth.dto.SignupRequest
import com.example.fanzbe.domain.auth.dto.SignupResponse
import com.example.fanzbe.domain.auth.dto.TokenResponse
import com.example.fanzbe.domain.auth.service.AuthService
import com.example.fanzbe.global.common.ApiResponse
import com.example.fanzbe.global.security.CustomUserDetails
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
) {

    @PostMapping("/signup")
    fun signup(
        @Valid @RequestBody request: SignupRequest,
    ): ResponseEntity<ApiResponse<SignupResponse>> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(authService.signup(request)))

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ApiResponse<TokenResponse> =
        ApiResponse.success(authService.login(request))

    @PostMapping("/reissue")
    fun reissue(
        @Valid @RequestBody request: ReissueRequest,
    ): ApiResponse<TokenResponse> =
        ApiResponse.success(authService.reissue(request))

    @PostMapping("/logout")
    fun logout(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<ApiResponse<Nothing>> {
        authService.logout(userDetails.id)
        return ResponseEntity.noContent().build()
    }
}
