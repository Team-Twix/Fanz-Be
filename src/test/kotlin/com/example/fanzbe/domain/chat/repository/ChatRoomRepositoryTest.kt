package com.example.fanzbe.domain.chat.repository

import com.example.fanzbe.domain.chat.entity.AgeGroup
import com.example.fanzbe.domain.chat.entity.ChatRoom
import com.example.fanzbe.domain.chat.entity.Gender
import com.example.fanzbe.domain.user.entity.User
import com.example.fanzbe.domain.user.repository.UserRepository
import jakarta.persistence.EntityManager
import kotlin.test.Test
import kotlin.test.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

/**
 * 단체 채팅방 검색(해시태그) + 가입조건 저장을 실제 H2 로 검증한다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChatRoomRepositoryTest(
    @Autowired private val chatRoomRepository: ChatRoomRepository,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val entityManager: EntityManager,
) {

    @Test
    fun `해시태그로 검색하면 해당 태그를 가진 방만 반환한다`() {
        val host = createUser("host")
        val anime = chatRoomRepository.save(
            ChatRoom(name = "anime room", hashtags = mutableSetOf("anime", "kpop"), host = host),
        )
        chatRoomRepository.save(
            ChatRoom(name = "game room", hashtags = mutableSetOf("game"), host = host),
        )

        val result = chatRoomRepository.search(hashtag = "anime", keyword = null)

        assertEquals(listOf(anime.id), result.map { it.id })
    }

    @Test
    fun `가입조건(연령·성별·추가조건)이 저장되고 다시 로드된다`() {
        val host = createUser("host2")
        val saved = chatRoomRepository.save(
            ChatRoom(
                name = "conditioned",
                hashtags = mutableSetOf("anime"),
                ageConditions = mutableSetOf(AgeGroup.TEENS, AgeGroup.TWENTIES),
                genderCondition = Gender.FEMALE,
                extraConditions = mutableSetOf("매너 좋은 사람"),
                host = host,
            ),
        )
        entityManager.flush()
        entityManager.clear()

        val reloaded = chatRoomRepository.findById(saved.id!!).orElseThrow()

        assertEquals(setOf(AgeGroup.TEENS, AgeGroup.TWENTIES), reloaded.ageConditions)
        assertEquals(Gender.FEMALE, reloaded.genderCondition)
        assertEquals(setOf("매너 좋은 사람"), reloaded.extraConditions)
        assertEquals(setOf("anime"), reloaded.hashtags)
    }

    private fun createUser(nickname: String): User =
        userRepository.save(
            User(password = "encoded", nickname = nickname),
        )
}
