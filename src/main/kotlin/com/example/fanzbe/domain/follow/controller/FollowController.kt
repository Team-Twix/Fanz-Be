package com.example.fanzbe.domain.follow.controller

import com.example.fanzbe.domain.follow.dto.FollowUserResponse
import com.example.fanzbe.domain.follow.service.FollowService
import com.example.fanzbe.global.common.ApiResponse
import com.example.fanzbe.global.security.CustomUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users/{userId}")
class FollowController(
    private val followService: FollowService,
) {

    @PostMapping("/follow")
    fun follow(
        @PathVariable("userId") userId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ApiResponse<Nothing> {
        followService.follow(followerId = userDetails.id, followeeId = userId)
        return ApiResponse.success()
    }

    @DeleteMapping("/follow")
    fun unfollow(
        @PathVariable("userId") userId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ApiResponse<Nothing> {
        followService.unfollow(followerId = userDetails.id, followeeId = userId)
        return ApiResponse.success()
    }

    @GetMapping("/followers")
    fun followers(
        @PathVariable("userId") userId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ApiResponse<List<FollowUserResponse>> =
        ApiResponse.success(followService.getFollowers(targetUserId = userId, currentUserId = userDetails.id))

    @GetMapping("/followings")
    fun followings(
        @PathVariable("userId") userId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ApiResponse<List<FollowUserResponse>> =
        ApiResponse.success(followService.getFollowings(targetUserId = userId, currentUserId = userDetails.id))
}
