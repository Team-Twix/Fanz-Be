package com.example.fanzbe.domain.user.entity

/**
 * 유저가 프로필에 설정하는 성별. UNSPECIFIED는 미설정 상태이며
 * 성별 제한이 있는 채팅방에는 참여할 수 없다.
 */
enum class UserGender {
    MALE,
    FEMALE,
    UNSPECIFIED,
}
