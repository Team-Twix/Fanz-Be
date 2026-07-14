package com.example.fanzbe.domain.chat.controller

import com.example.fanzbe.domain.chat.dto.ChatMessageResponse
import com.example.fanzbe.domain.chat.dto.ChatRoomResponse
import com.example.fanzbe.domain.chat.dto.CreateChatRoomRequest
import com.example.fanzbe.domain.chat.dto.MessagePageResponse
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
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chat-rooms")
class ChatRoomController(
    private val chatRoomService: ChatRoomService,
    private val chatMessageService: ChatMessageService,
) {

    @PostMapping
    fun createRoom(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody request: CreateChatRoomRequest,
    ): ResponseEntity<ApiResponse<ChatRoomResponse>> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(chatRoomService.createRoom(userDetails.id, request)))

    @GetMapping
    fun getRooms(
        @RequestParam(name = "hashtag", required = false) hashtag: String?,
        @RequestParam(name = "keyword", required = false) keyword: String?,
    ): ApiResponse<List<ChatRoomResponse>> =
        ApiResponse.success(chatRoomService.getRooms(hashtag, keyword))

    @GetMapping("/popular")
    fun getPopularRooms(
        @RequestParam(name = "limit", defaultValue = "4") limit: Int,
    ): ApiResponse<List<PopularChatRoomResponse>> =
        ApiResponse.success(chatRoomService.getPopularRooms(limit))

    @PostMapping("/{id}/join")
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

    // 채팅방 나가기 (멤버 본인). "/members/me" 는 "/members/{userId}" 보다 우선 매칭됨.
    @DeleteMapping("/{id}/members/me")
    fun leaveRoom(
        @PathVariable("id") id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<ApiResponse<Nothing>> {
        chatRoomService.leaveRoom(roomId = id, userId = userDetails.id)
        return ResponseEntity.noContent().build()
    }

    // 방장의 멤버 추방
    @DeleteMapping("/{id}/members/{userId}")
    fun kickMember(
        @PathVariable("id") id: Long,
        @PathVariable("userId") userId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<ApiResponse<Nothing>> {
        chatRoomService.kickMember(roomId = id, hostUserId = userDetails.id, targetUserId = userId)
        return ResponseEntity.noContent().build()
    }

    // 방장의 채팅방 삭제
    @DeleteMapping("/{id}")
    fun deleteRoom(
        @PathVariable("id") id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<ApiResponse<Nothing>> {
        chatRoomService.deleteRoom(roomId = id, hostUserId = userDetails.id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/messages")
    fun getMessages(
        @PathVariable("id") id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestParam(name = "page", defaultValue = "0") page: Int,
        @RequestParam(name = "size", defaultValue = "30") size: Int,
    ): ApiResponse<MessagePageResponse> =
        ApiResponse.success(
            chatMessageService.getMessages(
                roomId = id,
                userId = userDetails.id,
                page = page,
                size = size,
            ),
        )

    @PostMapping("/{id}/messages")
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
