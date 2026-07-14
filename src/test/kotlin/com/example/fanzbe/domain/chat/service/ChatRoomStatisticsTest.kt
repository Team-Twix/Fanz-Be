package com.example.fanzbe.domain.chat.service

import kotlin.test.Test
import kotlin.test.assertEquals

class ChatRoomStatisticsTest {

    @Test
    fun `activity rate is zero when member count is zero`() {
        val activityRate = ChatRoomStatistics.calculateActivityRate(
            memberCount = 0,
            activeMemberCount = 3,
        )

        assertEquals(0.0, activityRate)
    }

    @Test
    fun `activity rate is active members divided by all members as percent`() {
        val activityRate = ChatRoomStatistics.calculateActivityRate(
            memberCount = 4,
            activeMemberCount = 2,
        )

        assertEquals(50.0, activityRate)
    }

    @Test
    fun `activity rate is rounded to one decimal place`() {
        val activityRate = ChatRoomStatistics.calculateActivityRate(
            memberCount = 3,
            activeMemberCount = 1,
        )

        assertEquals(33.3, activityRate)
    }

    @Test
    fun `monthly active member threshold is thirty messages`() {
        assertEquals(30, ChatRoomStatistics.MONTHLY_ACTIVE_MESSAGE_THRESHOLD)
    }
}
