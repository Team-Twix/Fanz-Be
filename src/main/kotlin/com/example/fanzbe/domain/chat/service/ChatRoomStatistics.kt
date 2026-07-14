package com.example.fanzbe.domain.chat.service

import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.round

object ChatRoomStatistics {
    const val MONTHLY_ACTIVE_MESSAGE_THRESHOLD = 30

    fun currentMonthStart(): LocalDateTime =
        LocalDate.now()
            .withDayOfMonth(1)
            .atStartOfDay()

    fun calculateActivityRate(memberCount: Long, activeMemberCount: Long): Double {
        if (memberCount == 0L) {
            return 0.0
        }

        val rate = activeMemberCount.toDouble() / memberCount.toDouble() * 100.0
        return round(rate * 10.0) / 10.0
    }
}
