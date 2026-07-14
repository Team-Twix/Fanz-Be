package com.example.fanzbe.domain.recommendation.service

import com.example.fanzbe.domain.profile.entity.Profile
import com.example.fanzbe.domain.profile.repository.ProfileRepository
import com.example.fanzbe.domain.recommendation.ai.AiClient
import com.example.fanzbe.domain.recommendation.ai.RankInput
import com.example.fanzbe.domain.user.entity.AgeGroup
import com.example.fanzbe.domain.user.entity.User
import com.example.fanzbe.domain.user.repository.UserRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(RecommendationServiceTest.NoAiClientConfig::class)
class RecommendationServiceTest(
    @Autowired private val recommendationService: RecommendationService,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val profileRepository: ProfileRepository,
) {

    @Test
    fun `AI가 없으면 공통 관심사 수 기준으로 추천하고 상위 10명만 반환한다`() {
        val current = createUser(
            username = "current",
            nickname = "current",
            interests = setOf("anime", "game", "vocaloid"),
        )
        val best = createUser(
            username = "best",
            nickname = "best",
            interests = setOf("anime", "game", "vocaloid"),
            mannerScore = 45.0,
        )
        val second = createUser(
            username = "second",
            nickname = "second",
            interests = setOf("anime", "game", "movie"),
            mannerScore = 50.0,
        )
        val outsider = createUser(
            username = "outsider",
            nickname = "outsider",
            interests = setOf("baseball"),
        )
        profileRepository.save(Profile(user = best, profileImageUrl = "https://example.com/best.png"))

        val fillerUsers = (1..10).map { index ->
            createUser(
                username = "candidate$index",
                nickname = "candidate$index",
                interests = setOf("anime"),
                mannerScore = 30.0 + index,
            )
        }

        val recommendations = recommendationService.getRecommendations(current.id!!)

        assertEquals(10, recommendations.size)
        assertEquals(listOf(best.id!!, second.id!!), recommendations.take(2).map { it.userId })
        assertFalse(recommendations.any { it.userId == current.id })
        assertFalse(recommendations.any { it.userId == outsider.id })
        assertEquals("https://example.com/best.png", recommendations.first().profileImageUrl)
        assertEquals(listOf("anime", "game", "vocaloid"), recommendations.first().sharedHashtags)
        assertTrue(fillerUsers.map { it.id!! }.intersect(recommendations.map { it.userId }.toSet()).isNotEmpty())
    }

    @Test
    fun `관심사가 없는 유저는 빈 추천을 받는다`() {
        val current = createUser(username = "empty", nickname = "empty", interests = emptySet())
        createUser(username = "candidate", nickname = "candidate", interests = setOf("anime"))

        val recommendations = recommendationService.getRecommendations(current.id!!)

        assertTrue(recommendations.isEmpty())
    }

    private fun createUser(
        username: String,
        nickname: String,
        interests: Set<String>,
        mannerScore: Double = 36.5,
    ): User =
        userRepository.save(
            User(
                username = username,
                password = "encoded",
                nickname = nickname,
                ageGroup = AgeGroup.TWENTIES,
                interests = interests.toMutableSet(),
                mannerScore = mannerScore,
            ),
        )

    @TestConfiguration
    class NoAiClientConfig {
        @Bean
        @Primary
        fun noAiClient(): AiClient =
            object : AiClient() {
                override fun rankCandidates(current: RankInput, candidates: List<RankInput>): List<Long>? =
                    null
            }
    }
}
