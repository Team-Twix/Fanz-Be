package com.example.fanzbe.domain.rating.service

import com.example.fanzbe.domain.chat.repository.ChatRoomMemberRepository
import com.example.fanzbe.domain.chat.repository.ChatRoomRepository
import com.example.fanzbe.domain.rating.dto.RateUserRequest
import com.example.fanzbe.domain.rating.dto.RatingSummaryResponse
import com.example.fanzbe.domain.rating.entity.UserRating
import com.example.fanzbe.domain.rating.repository.UserRatingRepository
import com.example.fanzbe.domain.user.entity.User
import com.example.fanzbe.domain.user.repository.UserRepository
import com.example.fanzbe.global.exception.BusinessException
import com.example.fanzbe.global.exception.ErrorCode
import java.math.RoundingMode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserRatingService(
    private val userRatingRepository: UserRatingRepository,
    private val userRepository: UserRepository,
    private val chatRoomRepository: ChatRoomRepository,
    private val chatRoomMemberRepository: ChatRoomMemberRepository,
) {

    @Transactional
    fun rate(raterId: Long, targetId: Long, request: RateUserRequest): RatingSummaryResponse {
        if (raterId == targetId) {
            throw BusinessException(ErrorCode.CANNOT_RATE_SELF)
        }

        val rater = getUser(raterId)
        val target = getUser(targetId)
        val room = chatRoomRepository.findById(request.chatRoomId)
            .orElseThrow { BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND) }
        if (!chatRoomMemberRepository.existsByChatRoomAndUser(room, rater) ||
            !chatRoomMemberRepository.existsByChatRoomAndUser(room, target)
        ) {
            throw BusinessException(ErrorCode.RATING_NOT_ELIGIBLE)
        }

        val rating = userRatingRepository.findByRaterIdAndTargetIdAndChatRoomId(
            raterId = raterId,
            targetId = targetId,
            chatRoomId = request.chatRoomId,
        ) ?: UserRating(rater = rater, target = target, chatRoom = room, score = request.score)
        rating.score = request.score
        userRatingRepository.saveAndFlush(rating)

        val average = requireNotNull(userRatingRepository.findAverageByTargetId(targetId))
        target.mannerScore = calculateMannerScore(average)
        return target.toSummary(average)
    }

    @Transactional(readOnly = true)
    fun getSummary(targetId: Long): RatingSummaryResponse {
        val target = getUser(targetId)
        return target.toSummary(userRatingRepository.findAverageByTargetId(targetId))
    }

    private fun User.toSummary(average: Double?): RatingSummaryResponse {
        val userId = requireNotNull(id) { "Rated user id must not be null." }
        return RatingSummaryResponse(
            userId = userId,
            averageRating = average?.roundToOneDecimal(),
            ratingCount = userRatingRepository.countByTargetId(userId),
            mannerScore = mannerScore,
        )
    }

    private fun calculateMannerScore(average: Double): Double =
        (User.DEFAULT_MANNER_SCORE + (average - NEUTRAL_RATING) * MANNER_POINT_PER_STAR)
            .coerceIn(MIN_MANNER_SCORE, MAX_MANNER_SCORE)
            .roundToOneDecimal()

    private fun Double.roundToOneDecimal(): Double =
        toBigDecimal().setScale(1, RoundingMode.HALF_UP).toDouble()

    private fun getUser(userId: Long): User =
        userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

    companion object {
        private const val NEUTRAL_RATING = 3.0
        private const val MANNER_POINT_PER_STAR = 5.0
        private const val MIN_MANNER_SCORE = 0.0
        private const val MAX_MANNER_SCORE = 99.9
    }
}
