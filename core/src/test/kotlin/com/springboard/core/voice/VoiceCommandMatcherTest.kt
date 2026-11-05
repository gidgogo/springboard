package com.springboard.core.voice

import com.springboard.core.voice.VoiceCommandMatcher.Match
import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceCommandMatcherTest {

    private val matcher = VoiceCommandMatcher(
        linkedMapOf(
            "REPEAT" to listOf("다시", "한 번 더"),
            "NEXT" to listOf("다음"),
        ),
    )

    /** 짧은 긍정 대답 */
    @Test
    fun shortYesAnswers() {
        listOf("응", "네", "네 좋아요", "그래요", "좋아", "오케이", "OK!").forEach {
            assertEquals(it, Match.Yes, matcher.match(it))
        }
    }

    /** 짧은 부정 대답 */
    @Test
    fun shortNoAnswers() {
        listOf("아니", "아니요", "아니요 괜찮아요", "싫어요", "됐어", "No").forEach {
            assertEquals(it, Match.No, matcher.match(it))
        }
    }

    /** 취소는 어디에 있든 가장 먼저 처리한다 */
    @Test
    fun cancelWinsAnywhereInSentence() {
        listOf("취소", "취소해 줘", "이제 그만", "안내 중지").forEach {
            assertEquals(it, Match.Cancel, matcher.match(it))
        }
    }

    /** "네비"처럼 "네"로 시작하는 단어를 긍정으로 오해하지 않는다 */
    @Test
    fun wordStartingWithNeIsNotYes() {
        assertEquals(Match.Unknown, matcher.match("네비 켜 줘"))
    }

    /** 키워드 명령은 띄어쓰기와 상관없이 찾는다 */
    @Test
    fun keywordCommandsIgnoreSpacing() {
        assertEquals(Match.Command("REPEAT", "한 번 더"), matcher.match("한번 더 말해 줘"))
        assertEquals(Match.Command("NEXT", "다음"), matcher.match("다음 거"))
    }

    /** 빈 문장과 모르는 말 */
    @Test
    fun emptyAndUnknownSentences() {
        assertEquals(Match.Unknown, matcher.match(""))
        assertEquals(Match.Unknown, matcher.match("  ...  "))
        assertEquals(Match.Unknown, matcher.match("오늘 날씨 어때"))
    }

    /** 정규화 함수 */
    @Test
    fun normalization() {
        assertEquals("한번더", VoiceCommandMatcher.compact("한 번, 더!"))
        assertEquals("좋아", VoiceCommandMatcher.firstWord("좋아요. 그렇게 해"))
    }
}
