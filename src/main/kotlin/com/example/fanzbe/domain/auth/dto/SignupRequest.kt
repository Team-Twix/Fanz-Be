package com.example.fanzbe.domain.auth.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 회원가입 (4단계: 아이디/비번 · 게임 내 닉네임 · 나이대 · 카테고리).
 * 프론트가 다단계로 모아 한 번에 전송.
 */
data class SignupRequest(

    @field:NotBlank
    @field:Size(min = 4, max = 20)
    val username: String,

    @field:NotBlank
    @field:Size(min = 8, max = 64)
    val password: String,

    @field:NotBlank
    val passwordConfirm: String,

    @field:NotBlank
    @field:Size(max = 50)
    val nickname: String,

    // 실제 나이 (숫자 또는 문자열). 서버에서 AgeGroup 으로 변환.
    @field:NotBlank
    val age: String,

    // 관심 카테고리
    val interests: List<String> = emptyList(),
)
