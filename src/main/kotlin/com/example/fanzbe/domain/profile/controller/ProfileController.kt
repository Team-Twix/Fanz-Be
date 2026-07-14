package com.example.fanzbe.domain.profile.controller

import com.example.fanzbe.domain.profile.dto.MyProfileResponse
import com.example.fanzbe.domain.profile.dto.UpdateProfileRequest
import com.example.fanzbe.domain.profile.dto.UserProfileResponse
import com.example.fanzbe.domain.profile.service.ProfileService
import com.example.fanzbe.global.common.ApiResponse
import com.example.fanzbe.global.security.CustomUserDetails
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/profile")
class ProfileController(
    private val profileService: ProfileService,
) {

    @GetMapping("/me")
    fun getMyProfile(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ApiResponse<MyProfileResponse> =
        ApiResponse.success(profileService.getMyProfile(userDetails.id))

    @PutMapping("/me")
    fun updateMyProfile(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody request: UpdateProfileRequest,
    ): ApiResponse<MyProfileResponse> =
        ApiResponse.success(profileService.updateMyProfile(userDetails.id, request))

    @GetMapping("/{userId}")
    fun getUserProfile(
        @PathVariable("userId") userId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ApiResponse<UserProfileResponse> =
        ApiResponse.success(profileService.getUserProfile(targetUserId = userId, currentUserId = userDetails.id))
}
