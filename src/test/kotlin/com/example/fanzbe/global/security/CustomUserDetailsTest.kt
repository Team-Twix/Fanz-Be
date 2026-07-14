package com.example.fanzbe.global.security

import com.example.fanzbe.domain.user.entity.Role
import com.example.fanzbe.domain.user.entity.User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CustomUserDetailsTest {

    @Test
    fun `custom user details exposes id credentials and role authority`() {
        val user = User(
            id = 7L,
            username = "admin-account",
            password = "encoded-password",
            nickname = "admin",
            role = Role.ADMIN,
        )

        val userDetails = CustomUserDetails(user)

        assertEquals(7L, userDetails.id)
        assertEquals("admin-account", userDetails.username)
        assertEquals("encoded-password", userDetails.password)
        assertTrue(userDetails.authorities.any { it.authority == "ROLE_ADMIN" })
    }
}
