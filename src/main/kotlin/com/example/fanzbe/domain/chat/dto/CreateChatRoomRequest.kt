package com.example.fanzbe.domain.chat.dto

import com.example.fanzbe.domain.chat.entity.AgeGroup
import com.example.fanzbe.domain.chat.entity.Gender
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateChatRoomRequest(

    @field:NotBlank
    @field:Size(max = 100)
    val name: String,

    // 목록 카드에 표시하는 한 줄 소개
    @field:Size(max = 200)
    val summary: String? = null,

    // 채팅방 상세 설명
    @field:Size(max = 2000)
    val description: String? = null,

    @field:Size(max = 2048)
    val imageUrl: String? = null,

    // 해시태그 (여러 개)
    @field:Size(max = 20)
    val hashtags: List<String> = emptyList(),

    // 가입조건 - 연령대 (비어 있으면 "전체")
    val ageConditions: List<AgeGroup> = emptyList(),

    // 가입조건 - 성별 (기본 ANY = 전체)
    val gender: Gender = Gender.ANY,

    // 가입조건 - 추가 조건 (자유 태그)
    @field:Size(max = 20)
    val extraConditions: List<String> = emptyList(),
)
