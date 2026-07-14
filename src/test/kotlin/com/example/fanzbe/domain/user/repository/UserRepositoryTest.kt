package com.example.fanzbe.domain.user.repository

import com.example.fanzbe.domain.user.entity.User
import kotlin.test.Test
import kotlin.test.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserRepositoryTest(
    @Autowired private val userRepository: UserRepository,
) {

    @Test
    fun `닉네임 일부로 사용자를 대소문자 구분 없이 검색한다`() {
        val animeFan = saveUser("AnimeFan", "anime", "game")
        val animeMate = saveUser("animeMate", "music")
        saveUser("BookFan", "book")

        val result = userRepository.findByNicknameContainingIgnoreCase("ANIME")

        assertEquals(setOf(animeFan.id, animeMate.id), result.map { it.id }.toSet())
    }

    @Test
    fun `해시태그로 사용자를 대소문자 구분 없이 검색한다`() {
        val first = saveUser("first", "anime", "game")
        val second = saveUser("second", "ANIME")
        saveUser("third", "music")

        val result = userRepository.findByHashtagIgnoreCase("anime")

        assertEquals(setOf(first.id, second.id), result.map { it.id }.toSet())
    }

    private fun saveUser(nickname: String, vararg hashtags: String): User =
        userRepository.save(
            User(
                password = "encoded",
                nickname = nickname,
                hashtags = hashtags.toMutableSet(),
            ),
        )
}
