package com.springboard.core.voice

/**
 * STT가 돌려준 문장을 간단한 명령으로 바꾼다.
 *
 * Pleos SpeechToText SDK는 받아쓰기만 하고 의도 해석(NLU)은 하지 않는다. 그래서 짧은 대답
 * ("응", "아니", "취소")과 앱이 정한 몇 개의 키워드 명령은 앱이 직접 맞춰야 한다.
 * 자유 문장 이해가 필요하면 Gleo Interface(App Schema)를 검토한다 — docs/02-pleos/gleo-interface.md
 *
 * 판단 순서: 취소 → 예/아니오(첫 단어) → 키워드 명령(선언 순서) → 알 수 없음
 *
 * @param commands 명령 이름 → 키워드 목록. 선언 순서대로 먼저 맞는 것을 고른다.
 */
class VoiceCommandMatcher(
    private val commands: Map<String, List<String>> = emptyMap(),
) {
    sealed interface Match {
        data object Yes : Match
        data object No : Match
        data object Cancel : Match
        data class Command(val name: String, val keyword: String) : Match
        data object Unknown : Match
    }

    fun match(spoken: String): Match {
        val compact = compact(spoken)
        if (compact.isEmpty()) return Match.Unknown

        if (CANCEL_WORDS.any { compact.contains(it) }) return Match.Cancel

        val first = firstWord(spoken)
        if (first in NO_WORDS) return Match.No
        if (first in YES_WORDS) return Match.Yes

        for ((name, keywords) in commands) {
            val hit = keywords.firstOrNull { compact.contains(compact(it)) }
            if (hit != null) return Match.Command(name, hit)
        }
        return Match.Unknown
    }

    companion object {
        private val SEPARATORS = Regex("[\\s\\p{Punct}…~·、。]+")

        /** 존댓말 "요"를 떼고 비교한다 (좋아요 → 좋아, 아니요 → 아니). */
        private fun stripPolite(word: String): String =
            if (word.length > 1 && word.endsWith("요")) word.dropLast(1) else word

        private val YES_WORDS = setOf(
            "응", "어", "네", "예", "그래", "좋아", "맞아", "오케이", "ok", "okay", "yes", "콜",
        )
        private val NO_WORDS = setOf(
            "아니", "아뇨", "아니야", "싫어", "됐어", "안해", "안돼", "no", "nope",
        )
        private val CANCEL_WORDS = listOf("취소", "그만", "멈춰", "중지", "cancel", "stop")

        /** 공백·문장부호를 없애고 소문자로 바꾼다. */
        fun compact(text: String): String = text.lowercase().replace(SEPARATORS, "")

        /** 첫 단어 (문장부호 제거, 존댓말 "요" 제거). */
        fun firstWord(text: String): String {
            val word = text.lowercase().trim().split(SEPARATORS).firstOrNull { it.isNotEmpty() } ?: return ""
            return stripPolite(word)
        }
    }
}
