package com.example.fanzbe.domain.chat.controller

import com.example.fanzbe.domain.chat.dto.ChatMessageResponse
import com.example.fanzbe.domain.chat.dto.ChatRoomResponse
import com.example.fanzbe.domain.chat.dto.CreateChatRoomRequest
import com.example.fanzbe.domain.chat.dto.PopularChatRoomResponse
import com.example.fanzbe.domain.chat.dto.SendMessageRequest
import com.example.fanzbe.domain.chat.service.ChatMessageService
import com.example.fanzbe.domain.chat.service.ChatRoomService
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class ChatRoomController(
    private val chatRoomService: ChatRoomService,
    private val chatMessageService: ChatMessageService,
) {

    @PostMapping("/api/chat-rooms")
    fun createRoom(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody request: CreateChatRoomRequest,
    ): ResponseEntity<ApiResponse<ChatRoomResponse>> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(chatRoomService.createRoom(userDetails.id, request)))

    @GetMapping("/api/chat-rooms")
    fun getRooms(
        @RequestParam(name = "hashtag", required = false) hashtag: String?,
        @RequestParam(name = "keyword", required = false) keyword: String?,
    ): ApiResponse<List<ChatRoomResponse>> =
        ApiResponse.success(chatRoomService.getRooms(hashtag, keyword))

    @GetMapping("/api/chat-rooms/popular")
    fun getPopularRooms(
        @RequestParam(name = "limit", defaultValue = "4") limit: Int,
    ): ApiResponse<List<PopularChatRoomResponse>> =
        ApiResponse.success(chatRoomService.getPopularRooms(limit))

    @PostMapping("/api/chat-rooms/{id}/join")
    fun joinRoom(
        @PathVariable("id") id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<ApiResponse<Nothing>> {
        chatRoomService.joinRoom(
            roomId = id,
            userId = userDetails.id,
        )

        return ResponseEntity.ok(ApiResponse.success())
    }

    @PostMapping("/api/chat-rooms/{id}/messages")
    fun sendMessage(
        @PathVariable("id") id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody request: SendMessageRequest,
    ): ResponseEntity<ApiResponse<ChatMessageResponse>> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                ApiResponse.success(
                    chatMessageService.sendMessage(
                        roomId = id,
                        senderUserId = userDetails.id,
                        content = request.content,
                    ),
                ),
            )
}
