package com.example.fanzbe.domain.dm.repository

import com.example.fanzbe.domain.dm.entity.DmRoom
import org.springframework.data.jpa.repository.JpaRepository

interface DmRoomRepository : JpaRepository<DmRoom, Long> {
    fun findByUserLowIdAndUserHighId(userLowId: Long, userHighId: Long): DmRoom?

    // 유저가 참여한 모든 대화방 (양쪽 컬럼 중 하나가 userId)
    fun findByUserLowIdOrUserHighId(userLowId: Long, userHighId: Long): List<DmRoom>
}
