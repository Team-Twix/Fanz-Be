package com.example.fanzbe.domain.user.entity

import kotlin.test.Test
import kotlin.test.assertEquals

class UserTest {

    @Test
    fun `new user has default manner score and user role`() {
        val user = User(
            username ="fan@example.com",
            password = "encoded-password",
            nickname = "fan",
        )

        assertEquals(36.5, user.mannerScore)
        assertEquals(Role.USER, user.role)
    }
}
