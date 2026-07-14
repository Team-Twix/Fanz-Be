package com.example.fanzbe.domain.chat.dto

import com.example.fanzbe.domain.chat.entity.AgeGroup
import com.example.fanzbe.domain.chat.entity.Gender

data class ChatRoomResponse(
    val id: Long,
    val name: String,
    val summary: String?,
    val description: String?,
    val imageUrl: String?,
    val hashtags: List<String>,
    val ageConditions: List<AgeGroup>,
    val gender: Gender,
    val extraConditions: List<String>,
    val hostId: Long,
    val memberCount: Long,
)
