package com.example.fanzbe.domain.recommendation.controller

import com.example.fanzbe.domain.recommendation.dto.RecommendedMateResponse
import com.example.fanzbe.domain.recommendation.service.RecommendationService
import com.example.fanzbe.global.common.ApiResponse
import com.example.fanzbe.global.security.CustomUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class RecommendationController(
    private val recommendationService: RecommendationService,
) {

    @GetMapping("/api/recommendations/mates")
    fun getRecommendedMates(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ApiResponse<List<RecommendedMateResponse>> =
        ApiResponse.success(recommendationService.getRecommendations(userDetails.id))
}
