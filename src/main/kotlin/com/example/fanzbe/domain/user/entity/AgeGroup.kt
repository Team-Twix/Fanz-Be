package com.example.fanzbe.domain.user.entity

/**
 * 유저 나이대 (회원가입 - 나이대 단계).
 * 디자인: 전체 또는 10대 ~ 70대.
 */
enum class AgeGroup {
    ALL,       // 전체 / 연령 미공개
    TEENS,     // 10대
    TWENTIES,  // 20대
    THIRTIES,  // 30대
    FORTIES,   // 40대
    FIFTIES,   // 50대
    SIXTIES,   // 60대
    SEVENTIES, // 70대
}
