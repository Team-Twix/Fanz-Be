package com.example.fanzbe.domain.chat.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SendMessageRequest(

    @field:NotBlank
    @field:Size(max = 2000)
    val content: String,
)
