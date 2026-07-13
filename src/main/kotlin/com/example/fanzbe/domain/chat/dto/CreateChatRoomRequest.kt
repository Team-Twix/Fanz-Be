package com.example.fanzbe.domain.chat.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateChatRoomRequest(

    @field:NotBlank
    @field:Size(max = 100)
    val name: String,

    @field:Size(max = 500)
    val description: String? = null,

    @field:Size(max = 2048)
    val imageUrl: String? = null,

    @field:NotBlank
    @field:Size(max = 50)
    val category: String,
)
