package com.springboard.core.driving

/**
 * 현재 주행 규제에서 화면이 "해도 되는 것"을 한곳에서 계산한다.
 * 화면 코드는 플래그를 직접 해석하지 말고 이 정책만 보고 그린다.
 *
 * 근거
 * - Pleos 지역별 주행 규제 정책: 키보드·검색·설정·동영상 등 기능별 허용 범위
 * - Pleos UI 디자인 가이드: 단일 메시지 120자(영문 기준) 이하, 문단 3줄 이하, 1~2초 안에 파악
 * - AAOS CarUxRestrictions: 문자열 길이·목록 항목 수 상한은 플랫폼이 내려준다
 */
data class DrivingUiPolicy(
    val drivingMode: Boolean,
    val allowKeyboard: Boolean,
    val allowVideo: Boolean,
    val allowSetup: Boolean,
    val allowSearch: Boolean,
    /** null 이면 제한 없음 */
    val maxListItems: Int?,
    /** null 이면 제한 없음 */
    val maxMessageChars: Int?,
    val maxLines: Int?,
    /** 주행 중에는 화면 조작보다 음성(TTS/STT, Gleo)을 우선한다. */
    val preferVoice: Boolean,
) {
    /** 주행 중 글자 수 제한에 맞게 자른다. */
    fun clip(text: String): String {
        val limit = maxMessageChars ?: return text
        if (text.length <= limit) return text
        if (limit <= 1) return text.take(limit)
        return text.take(limit - 1).trimEnd() + "…"
    }

    /** 주행 중 목록 길이 제한에 맞게 자른다. */
    fun <T> limit(items: List<T>): List<T> {
        val max = maxListItems ?: return items
        return items.take(max)
    }

    companion object {
        /** Pleos UI 가이드: 한 메시지는 120자(영문 기준) 이하 */
        const val GUIDE_MAX_MESSAGE_CHARS = 120

        /** Pleos UI 가이드: 문장·문단은 3줄 이하 */
        const val GUIDE_MAX_LINES = 3

        val PARKED = DrivingUiPolicy(
            drivingMode = false,
            allowKeyboard = true,
            allowVideo = true,
            allowSetup = true,
            allowSearch = true,
            maxListItems = null,
            maxMessageChars = null,
            maxLines = null,
            preferVoice = false,
        )

        fun from(state: DrivingState): DrivingUiPolicy = from(state.restrictions)

        fun from(restrictions: UxRestrictions): DrivingUiPolicy {
            if (!restrictions.requiresDistractionOptimization) return PARKED
            val messageLimit = if (restrictions.limitStringLength) {
                minOf(restrictions.maxStringLength, GUIDE_MAX_MESSAGE_CHARS)
            } else {
                GUIDE_MAX_MESSAGE_CHARS
            }
            return DrivingUiPolicy(
                drivingMode = true,
                allowKeyboard = !restrictions.noKeyboard,
                allowVideo = !restrictions.noVideo,
                allowSetup = !restrictions.noSetup,
                allowSearch = !restrictions.noFiltering && !restrictions.noKeyboard,
                maxListItems = if (restrictions.limitContent) restrictions.maxContentItems else null,
                // 플랫폼이 길이 제한을 안 걸어도 주행 중에는 디자인 가이드 상한을 지킨다.
                maxMessageChars = messageLimit,
                maxLines = GUIDE_MAX_LINES,
                preferVoice = true,
            )
        }
    }
}
