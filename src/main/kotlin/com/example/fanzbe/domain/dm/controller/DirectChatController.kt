package com.example.fanzbe.domain.dm.controller

import com.example.fanzbe.domain.chat.dto.SendMessageRequest
import com.example.fanzbe.domain.dm.dto.DirectChatRoomResponse
import com.example.fanzbe.domain.dm.dto.DirectMessagePageResponse
import com.example.fanzbe.domain.dm.dto.DirectMessageResponse
import com.example.fanzbe.domain.dm.service.DirectChatService
import com.example.fanzbe.global.common.ApiResponse
import com.example.fanzbe.global.security.CustomUserDetails
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/dm-rooms")
class DirectChatController(
    private val directChatService: DirectChatService,
) {

    @PostMapping(value = ["/{userId}", "/users/{userId}"])
    fun openRoom(
        @PathVariable("userId") userId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ApiResponse<DirectChatRoomResponse> =
        ApiResponse.success(directChatService.openRoom(userDetails.id, userId))

    @GetMapping
    fun getRooms(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ApiResponse<List<DirectChatRoomResponse>> =
        ApiResponse.success(directChatService.getRooms(userDetails.id))

    @GetMapping("/{id}")
    fun getRoom(
        @PathVariable("id") id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ApiResponse<DirectChatRoomResponse> =
        ApiResponse.success(directChatService.getRoom(id, userDetails.id))

    @GetMapping("/{id}/messages")
    fun getMessages(
        @PathVariable("id") id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestParam(name = "page", defaultValue = "0") page: Int,
        @RequestParam(name = "size", defaultValue = "30") size: Int,
    ): ApiResponse<DirectMessagePageResponse> =
        ApiResponse.success(directChatService.getMessages(id, userDetails.id, page, size))

    @PostMapping("/{id}/messages")
    fun sendMessage(
        @PathVariable("id") id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody request: SendMessageRequest,
    ): ResponseEntity<ApiResponse<DirectMessageResponse>> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(directChatService.sendMessage(id, userDetails.id, request)))
}
