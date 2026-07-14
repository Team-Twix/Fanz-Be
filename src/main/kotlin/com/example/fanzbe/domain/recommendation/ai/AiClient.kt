package com.example.fanzbe.domain.recommendation.ai

import com.anthropic.client.AnthropicClient
import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.models.messages.Message
import com.anthropic.models.messages.MessageCreateParams
import org.springframework.stereotype.Component

@Component
open class AiClient {

    private val apiKey: String? = System.getenv("ANTHROPIC_API_KEY")?.takeIf { it.isNotBlank() }

    private val client: AnthropicClient? by lazy {
        apiKey?.let { key ->
            AnthropicOkHttpClient.builder()
                .apiKey(key)
                .build()
        }
    }

    open fun rankCandidates(current: RankInput, candidates: List<RankInput>): List<Long>? {
        val activeClient = client ?: return null
        if (candidates.isEmpty()) {
            return emptyList()
        }

        return runCatching {
            val params = MessageCreateParams.builder()
                .model("claude-opus-4-8")
                .maxTokens(1024L)
                .addUserMessage(buildPrompt(current, candidates))
                .build()
            val message = activeClient.messages().create(params)

            parseRankedIds(extractText(message), candidates.map { it.userId }.toSet())
        }.getOrNull()
    }

    private fun extractText(message: Message): String =
        message.content()
            .mapNotNull { block -> block.text().orElse(null)?.text() }
            .joinToString("")

    private fun parseRankedIds(responseText: String, candidateIds: Set<Long>): List<Long> {
        val arrayText = JSON_ARRAY_REGEX.find(responseText)?.value
            ?: error("Claude response did not contain a JSON array.")
        val body = arrayText.removePrefix("[").removeSuffix("]").trim()
        if (body.isEmpty()) {
            return emptyList()
        }

        return body.split(",")
            .map { it.trim().toLong() }
            .filter { it in candidateIds }
            .distinct()
            .take(MAX_RECOMMENDATION_SIZE)
    }

    private fun buildPrompt(current: RankInput, candidates: List<RankInput>): String =
        """
        현재 유저와 후보 목록을 보고 덕질 메이트로 잘 맞는 순서로 최대 10명의 후보 id를 JSON 배열로만 반환하라.
        설명, 마크다운, 코드블록 없이 예시처럼 [12, 5, 33] 형식만 반환하라.

        현재 유저:
        ${current.toPromptLine()}

        후보 목록:
        ${candidates.joinToString(separator = "\n") { it.toPromptLine() }}
        """.trimIndent()

    private fun RankInput.toPromptLine(): String =
        "id=$userId, nickname=\"${nickname.promptEscaped()}\", " +
            "interests=${interests.toPromptArray()}, ageGroup=${ageGroup?.name ?: "UNKNOWN"}, " +
            "mannerScore=$mannerScore"

    private fun Set<String>.toPromptArray(): String =
        joinToString(prefix = "[", postfix = "]") { "\"${it.promptEscaped()}\"" }

    private fun String.promptEscaped(): String =
        replace("\\", "\\\\").replace("\"", "\\\"")

    companion object {
        private const val MAX_RECOMMENDATION_SIZE = 10
        private val JSON_ARRAY_REGEX = Regex("""\[[\s\d,]*\]""")
    }
}
