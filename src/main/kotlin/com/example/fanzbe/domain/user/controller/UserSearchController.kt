package com.example.fanzbe.domain.user.controller

import com.example.fanzbe.domain.user.dto.UserSearchResponse
import com.example.fanzbe.domain.user.service.UserSearchService
import com.example.fanzbe.global.common.ApiResponse
import com.example.fanzbe.global.security.CustomUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class UserSearchController(
    private val userSearchService: UserSearchService,
) {

    @GetMapping("/api/users/search")
    fun search(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestParam(name = "nickname", required = false) nickname: String?,
        @RequestParam(name = "hashtag", required = false) hashtag: String?,
    ): ApiResponse<List<UserSearchResponse>> =
        ApiResponse.success(userSearchService.search(userDetails.id, nickname, hashtag))
}
