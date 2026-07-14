package com.example.fanzbe.domain.rating.controller

import com.example.fanzbe.domain.rating.dto.RateUserRequest
import com.example.fanzbe.domain.rating.dto.RatingSummaryResponse
import com.example.fanzbe.domain.rating.service.UserRatingService
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
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users/{userId}/ratings")
class UserRatingController(
    private val userRatingService: UserRatingService,
) {

    @PostMapping
    fun rate(
        @PathVariable("userId") userId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody request: RateUserRequest,
    ): ResponseEntity<ApiResponse<RatingSummaryResponse>> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(userRatingService.rate(userDetails.id, userId, request)))

    @GetMapping("/summary")
    fun getSummary(
        @PathVariable("userId") userId: Long,
    ): ApiResponse<RatingSummaryResponse> =
        ApiResponse.success(userRatingService.getSummary(userId))
}
