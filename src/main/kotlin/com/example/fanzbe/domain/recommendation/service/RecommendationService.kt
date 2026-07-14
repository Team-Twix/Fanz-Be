package com.example.fanzbe.domain.recommendation.service

import com.example.fanzbe.domain.profile.repository.ProfileRepository
import com.example.fanzbe.domain.recommendation.ai.AiClient
import com.example.fanzbe.domain.recommendation.ai.RankInput
import com.example.fanzbe.domain.recommendation.dto.RecommendedMateResponse
import com.example.fanzbe.domain.user.entity.User
import com.example.fanzbe.domain.user.repository.UserRepository
import com.example.fanzbe.global.exception.BusinessException
import com.example.fanzbe.global.exception.ErrorCode
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecommendationService(
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val aiClient: AiClient,
) {

    private val cache = ConcurrentHashMap<Long, CachedRecommendations>()

    @Transactional(readOnly = true)
    fun getRecommendations(currentUserId: Long): List<RecommendedMateResponse> {
        getCached(currentUserId)?.let { return it }

        val current = userRepository.findById(currentUserId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        val currentInterests = current.interests.toSet()
        if (currentInterests.isEmpty()) {
            return emptyList()
        }

        val candidatePool = userRepository.findCandidatesByInterests(currentUserId, currentInterests)
            .asSequence()
            .filter { it.id != null && it.id != currentUserId }
            .map { candidate -> CandidateScore(candidate, sharedHashtags(currentInterests, candidate)) }
            .filter { it.sharedHashtags.isNotEmpty() }
            .sortedWith(fallbackComparator)
            .take(CANDIDATE_LIMIT)
            .toList()

        val aiRankedIds = aiClient.rankCandidates(
            current = current.toRankInput(),
            candidates = candidatePool.map { it.user.toRankInput() },
        )

        val selectedCandidates = if (aiRankedIds == null) {
            candidatePool.take(RECOMMENDATION_LIMIT)
        } else {
            val candidatesById = candidatePool.associateBy { requireNotNull(it.user.id) }
            aiRankedIds.mapNotNull(candidatesById::get).take(RECOMMENDATION_LIMIT)
        }

        val recommendations = selectedCandidates.map { it.toResponse() }
        cache[currentUserId] = CachedRecommendations(cachedAt = Instant.now(), recommendations = recommendations)

        return recommendations
    }

    private fun getCached(userId: Long): List<RecommendedMateResponse>? {
        val cached = cache[userId] ?: return null
        if (Duration.between(cached.cachedAt, Instant.now()) >= CACHE_TTL) {
            cache.remove(userId, cached)
            return null
        }
        return cached.recommendations
    }

    private fun CandidateScore.toResponse(): RecommendedMateResponse {
        val userId = requireNotNull(user.id) { "Recommended user id must not be null." }
        return RecommendedMateResponse(
            userId = userId,
            nickname = user.nickname,
            profileImageUrl = profileRepository.findByUserId(userId)?.profileImageUrl,
            sharedHashtags = sharedHashtags,
            mannerScore = user.mannerScore,
        )
    }

    private fun User.toRankInput(): RankInput =
        RankInput(
            userId = requireNotNull(id) { "Rank input user id must not be null." },
            nickname = nickname,
            interests = interests.toSet(),
            ageGroup = ageGroup,
            mannerScore = mannerScore,
        )

    private fun sharedHashtags(currentInterests: Set<String>, candidate: User): List<String> =
        currentInterests.intersect(candidate.interests.toSet()).sorted()

    private data class CandidateScore(
        val user: User,
        val sharedHashtags: List<String>,
    )

    private data class CachedRecommendations(
        val cachedAt: Instant,
        val recommendations: List<RecommendedMateResponse>,
    )

    companion object {
        private const val CANDIDATE_LIMIT = 30
        private const val RECOMMENDATION_LIMIT = 10
        private val CACHE_TTL: Duration = Duration.ofMinutes(30)
        private val fallbackComparator = compareByDescending<CandidateScore> { it.sharedHashtags.size }
            .thenByDescending { it.user.mannerScore }
            .thenBy { requireNotNull(it.user.id) }
    }
}
