package com.example.fanzbe.domain.profile.dto

import com.example.fanzbe.domain.user.entity.AgeGroup
import com.example.fanzbe.domain.user.entity.UserGender
import jakarta.validation.constraints.Size

data class UpdateProfileRequest(

    @field:Size(max = 50)
    val nickname: String? = null,

    @field:Size(max = 30)
    val handle: String? = null,

    @field:Size(max = 500)
    val bio: String? = null,

    @field:Size(max = 2048)
    val profileImageUrl: String? = null,

    @field:Size(max = 2048)
    val coverImageUrl: String? = null,

    val ageGroup: AgeGroup? = null,

    val gender: UserGender? = null,

    @field:Size(max = 20)
    val interests: List<String>? = null,
)
