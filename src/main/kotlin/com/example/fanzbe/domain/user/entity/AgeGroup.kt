package com.example.fanzbe.domain.user.entity

/**
 * 유저 나이대 (회원가입 - 나이대 단계).
 * 디자인: 10대 ~ 60대.
 */
enum class AgeGroup {
    TEENS,     // 10대
    TWENTIES,  // 20대
    THIRTIES,  // 30대
    FORTIES,   // 40대
    FIFTIES,   // 50대
    SIXTIES,   // 60대
    ;

    companion object {
        /** 실제 나이(숫자)를 나이대 그룹으로 변환. 60세 이상은 SIXTIES 로 캡. */
        fun fromAge(age: Int): AgeGroup = when {
            age in 10..19 -> TEENS
            age in 20..29 -> TWENTIES
            age in 30..39 -> THIRTIES
            age in 40..49 -> FORTIES
            age in 50..59 -> FIFTIES
            age >= 60 -> SIXTIES
            else -> throw IllegalArgumentException("지원하지 않는 나이입니다: $age")
        }
    }
}
