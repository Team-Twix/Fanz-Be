package com.example.fanzbe.domain.chat.entity

/**
 * 채팅방 가입조건 - 연령대.
 * 디자인의 "전체"는 별도 값이 아니라 ageConditions 를 비워 두는 것(연령 제한 없음)으로 표현한다.
 */
enum class AgeGroup {
    TEENS,      // 10대
    TWENTIES,   // 20대
    THIRTIES,   // 30대
    FORTIES,    // 40대
    FIFTIES,    // 50대
    SIXTIES,    // 60대
    SEVENTIES,  // 70대
}
