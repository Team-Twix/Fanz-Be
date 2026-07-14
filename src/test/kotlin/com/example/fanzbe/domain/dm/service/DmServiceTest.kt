package com.example.fanzbe.domain.dm.service

import com.example.fanzbe.domain.user.entity.User
import com.example.fanzbe.domain.user.repository.UserRepository
import com.example.fanzbe.global.exception.BusinessException
import com.example.fanzbe.global.exception.ErrorCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DmServiceTest(
    @Autowired private val dmService: DmService,
    @Autowired private val userRepository: UserRepository,
) {

    @Test
    fun `대화방은 유저 쌍당 하나로 get-or-create 된다`() {
        val a = createUser("a")
        val b = createUser("b")

        val room1 = dmService.getOrCreateRoom(a.id!!, b.id!!).roomId
        val room2 = dmService.getOrCreateRoom(b.id!!, a.id!!).roomId // 순서 바꿔도 동일

        assertEquals(room1, room2)
    }

    @Test
    fun `자기 자신과는 DM 불가`() {
        val a = createUser("a")
        val ex = assertFailsWith<BusinessException> {
            dmService.getOrCreateRoom(a.id!!, a.id!!)
        }
        assertEquals(ErrorCode.CANNOT_DM_SELF, ex.errorCode)
    }

    @Test
    fun `메시지 전송·조회는 참여자만 가능하다`() {
        val a = createUser("a")
        val b = createUser("b")
        val c = createUser("c")
        val roomId = dmService.getOrCreateRoom(a.id!!, b.id!!).roomId

        dmService.sendMessage(roomId, a.id!!, "hi")
        val page = dmService.getMessages(roomId, b.id!!, page = 0, size = 30)
        assertEquals(1, page.content.size)
        assertEquals("hi", page.content.first().content)

        val ex = assertFailsWith<BusinessException> {
            dmService.getMessages(roomId, c.id!!, page = 0, size = 30)
        }
        assertEquals(ErrorCode.NOT_DM_PARTICIPANT, ex.errorCode)
    }

    @Test
    fun `내 대화 목록에 상대와 마지막 메시지가 담긴다`() {
        val a = createUser("a")
        val b = createUser("b")
        val roomId = dmService.getOrCreateRoom(a.id!!, b.id!!).roomId
        dmService.sendMessage(roomId, b.id!!, "마지막 메시지")

        val rooms = dmService.getMyRooms(a.id!!)

        assertEquals(1, rooms.size)
        assertEquals(b.id, rooms.first().otherUser.userId)
        assertEquals("마지막 메시지", rooms.first().lastMessageContent)
    }

    private fun createUser(username: String): User =
        userRepository.save(User(username = username, password = "encoded", nickname = username))
}
