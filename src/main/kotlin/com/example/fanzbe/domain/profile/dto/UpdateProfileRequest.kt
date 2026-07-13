package com.example.fanzbe.domain.profile.dto

import jakarta.validation.constraints.Size

data class UpdateProfileRequest(

    @field:Size(max = 30)
    val handle: String? = null,

    @field:Size(max = 500)
    val bio: String? = null,

    @field:Size(max = 2048)
    val profileImageUrl: String? = null,

    @field:Size(max = 2048)
    val coverImageUrl: String? = null,
)
