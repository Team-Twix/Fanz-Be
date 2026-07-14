package com.example.fanzbe.domain.profile.dto

import com.example.fanzbe.domain.chat.entity.AgeGroup
import com.example.fanzbe.domain.chat.entity.Gender

data class JoinedChatRoomResponse(
    val id: Long,
    val name: String,
    val imageUrl: String?,
    val summary: String?,
    val description: String?,
    val hashtags: List<String>,
    val ageConditions: List<AgeGroup>,
    val gender: Gender,
    val memberCount: Long,
)
