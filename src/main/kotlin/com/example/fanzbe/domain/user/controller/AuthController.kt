package com.example.fanzbe.domain.user.controller

import com.example.fanzbe.domain.user.dto.SignupRequest
import com.example.fanzbe.domain.user.dto.SignupResponse
import com.example.fanzbe.domain.user.service.AuthService
import com.example.fanzbe.global.common.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
    ): ResponseEntity<ApiResponse<SignupResponse>> {
        val response = authService.signup(request)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response))
    }
}
