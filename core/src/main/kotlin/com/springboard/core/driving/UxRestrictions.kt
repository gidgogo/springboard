package com.springboard.core.driving

/**
 * AAOS `CarUxRestrictions`의 활성 제한 플래그.
 *
 * 값은 `android.car.drivingstate.CarUxRestrictions.UX_RESTRICTIONS_*` 상수와 같다.
 * Pleos "지역별 주행 규제 정책" 표의 괄호 숫자(1, 2, 4, 8, 16, 32, 64)와도 일치한다.
 */
object UxRestrictionFlags {
    const val BASELINE = 0
    const val NO_DIALPAD = 1
    const val NO_FILTERING = 1 shl 1
    const val LIMIT_STRING_LENGTH = 1 shl 2
    const val NO_KEYBOARD = 1 shl 3
    const val NO_VIDEO = 1 shl 4
    const val LIMIT_CONTENT = 1 shl 5
    const val NO_SETUP = 1 shl 6
    const val NO_TEXT_MESSAGE = 1 shl 7
    const val NO_VOICE_TRANSCRIPTION = 1 shl 8
    const val FULLY_RESTRICTED = (1 shl 9) - 1
}

/**
 * 플랫폼이 내려준 주행 규제 상태를 안드로이드 의존성 없이 담는 값.
 *
 * Google 가이드: 화면을 막을지 판단할 때는 [requiresDistractionOptimization]만 본다.
 * 기어나 속도로 규제 상태를 추정하지 않는다 (디스플레이마다 다를 수 있음).
 * 나머지 플래그는 DO(Distraction Optimized)를 선언한 화면이 "무엇을 줄일지" 정할 때 쓴다.
 *
 * @property maxStringLength `CarUxRestrictions.getMaxRestrictedStringLength()`
 * @property maxContentItems `CarUxRestrictions.getMaxCumulativeContentItems()`
 * @property maxContentDepth `CarUxRestrictions.getMaxContentDepth()`
 */
data class UxRestrictions(
    val requiresDistractionOptimization: Boolean,
    val activeFlags: Int = UxRestrictionFlags.BASELINE,
    val maxStringLength: Int = DEFAULT_MAX_STRING_LENGTH,
    val maxContentItems: Int = DEFAULT_MAX_CONTENT_ITEMS,
    val maxContentDepth: Int = DEFAULT_MAX_CONTENT_DEPTH,
) {
    /** DO가 필요할 때만 플래그가 의미가 있다. */
    fun has(flag: Int): Boolean = requiresDistractionOptimization && (activeFlags and flag) != 0

    val noDialpad: Boolean get() = has(UxRestrictionFlags.NO_DIALPAD)
    val noFiltering: Boolean get() = has(UxRestrictionFlags.NO_FILTERING)
    val limitStringLength: Boolean get() = has(UxRestrictionFlags.LIMIT_STRING_LENGTH)
    val noKeyboard: Boolean get() = has(UxRestrictionFlags.NO_KEYBOARD)
    val noVideo: Boolean get() = has(UxRestrictionFlags.NO_VIDEO)
    val limitContent: Boolean get() = has(UxRestrictionFlags.LIMIT_CONTENT)
    val noSetup: Boolean get() = has(UxRestrictionFlags.NO_SETUP)
    val noTextMessage: Boolean get() = has(UxRestrictionFlags.NO_TEXT_MESSAGE)
    val noVoiceTranscription: Boolean get() = has(UxRestrictionFlags.NO_VOICE_TRANSCRIPTION)

    /** 로그·디버그 화면용: 켜진 플래그 이름 목록. */
    fun activeFlagNames(): List<String> {
        if (!requiresDistractionOptimization) return emptyList()
        return FLAG_NAMES.filter { (flag, _) -> activeFlags and flag != 0 }.map { it.second }
    }

    companion object {
        /** AOSP 기본 설정값. 실제 값은 차량(플랫폼)이 내려준 값을 쓴다. */
        const val DEFAULT_MAX_STRING_LENGTH = 120
        const val DEFAULT_MAX_CONTENT_ITEMS = 21
        const val DEFAULT_MAX_CONTENT_DEPTH = 3

        /** 제한 없음 (주차 중). */
        val NONE = UxRestrictions(requiresDistractionOptimization = false)

        /** 모든 제한 (데모 시나리오의 주행 상태 등). */
        val FULLY_RESTRICTED = UxRestrictions(
            requiresDistractionOptimization = true,
            activeFlags = UxRestrictionFlags.FULLY_RESTRICTED,
        )

        private val FLAG_NAMES = listOf(
            UxRestrictionFlags.NO_DIALPAD to "NO_DIALPAD",
            UxRestrictionFlags.NO_FILTERING to "NO_FILTERING",
            UxRestrictionFlags.LIMIT_STRING_LENGTH to "LIMIT_STRING_LENGTH",
            UxRestrictionFlags.NO_KEYBOARD to "NO_KEYBOARD",
            UxRestrictionFlags.NO_VIDEO to "NO_VIDEO",
            UxRestrictionFlags.LIMIT_CONTENT to "LIMIT_CONTENT",
            UxRestrictionFlags.NO_SETUP to "NO_SETUP",
            UxRestrictionFlags.NO_TEXT_MESSAGE to "NO_TEXT_MESSAGE",
            UxRestrictionFlags.NO_VOICE_TRANSCRIPTION to "NO_VOICE_TRANSCRIPTION",
        )
    }
}
