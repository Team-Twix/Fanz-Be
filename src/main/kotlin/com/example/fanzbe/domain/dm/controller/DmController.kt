package com.example.fanzbe.domain.dm.controller

import com.example.fanzbe.domain.dm.dto.CreateDmRoomRequest
import com.example.fanzbe.domain.dm.dto.DmMessagePageResponse
import com.example.fanzbe.domain.dm.dto.DmMessageResponse
import com.example.fanzbe.domain.dm.dto.DmRoomResponse
import com.example.fanzbe.domain.dm.dto.DmRoomSummaryResponse
import com.example.fanzbe.domain.dm.dto.SendDmMessageRequest
import com.example.fanzbe.domain.dm.service.DmService
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
@RequestMapping("/api/dm/rooms")
class DmController(
    private val dmService: DmService,
) {

    @PostMapping
    fun getOrCreateRoom(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody request: CreateDmRoomRequest,
    ): ApiResponse<DmRoomResponse> =
        ApiResponse.success(dmService.getOrCreateRoom(userDetails.id, request.targetUserId))

    @GetMapping
    fun getMyRooms(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ApiResponse<List<DmRoomSummaryResponse>> =
        ApiResponse.success(dmService.getMyRooms(userDetails.id))

    @GetMapping("/{id}/messages")
    fun getMessages(
        @PathVariable("id") id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestParam(name = "page", defaultValue = "0") page: Int,
        @RequestParam(name = "size", defaultValue = "30") size: Int,
    ): ApiResponse<DmMessagePageResponse> =
        ApiResponse.success(dmService.getMessages(id, userDetails.id, page, size))

    @PostMapping("/{id}/messages")
    fun sendMessage(
        @PathVariable("id") id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody request: SendDmMessageRequest,
    ): ResponseEntity<ApiResponse<DmMessageResponse>> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(dmService.sendMessage(id, userDetails.id, request.content)))
}
