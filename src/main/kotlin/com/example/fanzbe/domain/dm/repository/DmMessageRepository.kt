package com.example.fanzbe.domain.dm.repository

import com.example.fanzbe.domain.dm.entity.DmMessage
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface DmMessageRepository : JpaRepository<DmMessage, Long> {
    fun findByDmRoomId(dmRoomId: Long, pageable: Pageable): Page<DmMessage>

    // 대화 목록의 마지막 메시지 미리보기
    fun findFirstByDmRoomIdOrderByIdDesc(dmRoomId: Long): DmMessage?
}
