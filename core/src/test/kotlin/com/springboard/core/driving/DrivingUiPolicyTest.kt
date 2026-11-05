package com.springboard.core.driving

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DrivingUiPolicyTest {

    /** 주차 중에는 제한이 없다 */
    @Test
    fun parkedHasNoLimits() {
        val policy = DrivingUiPolicy.from(DrivingState(restrictions = UxRestrictions.NONE, gear = Gear.PARK))

        assertFalse(policy.drivingMode)
        assertTrue(policy.allowKeyboard)
        assertTrue(policy.allowVideo)
        assertNull(policy.maxListItems)
        assertEquals("아주 긴 문장도 그대로", policy.clip("아주 긴 문장도 그대로"))
    }

    /** DO가 필요 없으면 플래그가 켜져 있어도 무시한다 */
    @Test
    fun flagsAreIgnoredWithoutDistractionOptimization() {
        val restrictions = UxRestrictions(
            requiresDistractionOptimization = false,
            activeFlags = UxRestrictionFlags.NO_KEYBOARD or UxRestrictionFlags.NO_VIDEO,
        )

        assertFalse(restrictions.noKeyboard)
        assertEquals(DrivingUiPolicy.PARKED, DrivingUiPolicy.from(restrictions))
    }

    /** 한국(그외 지역) 주행 규제 - 키보드는 허용, 설정·동영상은 제한 */
    @Test
    fun koreaDrivingAllowsKeyboardButBlocksSetupAndVideo() {
        // Pleos 지역별 주행 규제 정책 "그외 지역" Driving 행: 설정(NO_SETUP)·동영상(NO_VIDEO)·대용량 텍스트 제한
        val restrictions = UxRestrictions(
            requiresDistractionOptimization = true,
            activeFlags = UxRestrictionFlags.NO_SETUP or UxRestrictionFlags.NO_VIDEO or
                UxRestrictionFlags.LIMIT_CONTENT or UxRestrictionFlags.LIMIT_STRING_LENGTH,
            maxStringLength = 80,
            maxContentItems = 10,
        )

        val policy = DrivingUiPolicy.from(restrictions)

        assertTrue(policy.drivingMode)
        assertTrue(policy.allowKeyboard)
        assertTrue(policy.allowSearch)
        assertFalse(policy.allowSetup)
        assertFalse(policy.allowVideo)
        assertEquals(10, policy.maxListItems)
        assertEquals(80, policy.maxMessageChars)
        assertTrue(policy.preferVoice)
    }

    /** 완전 제한이면 키보드·검색 모두 막힌다 */
    @Test
    fun fullyRestrictedBlocksKeyboardAndSearch() {
        val policy = DrivingUiPolicy.from(UxRestrictions.FULLY_RESTRICTED)

        assertFalse(policy.allowKeyboard)
        assertFalse(policy.allowSearch)
        assertFalse(policy.allowSetup)
        assertEquals(UxRestrictions.DEFAULT_MAX_CONTENT_ITEMS, policy.maxListItems)
        assertEquals(DrivingUiPolicy.GUIDE_MAX_LINES, policy.maxLines)
    }

    /** 플랫폼 길이 제한이 없어도 주행 중에는 가이드 상한 120자를 지킨다 */
    @Test
    fun drivingAlwaysClipsToGuideMessageLength() {
        val policy = DrivingUiPolicy.from(UxRestrictions(requiresDistractionOptimization = true))
        val long = "가".repeat(200)

        val clipped = policy.clip(long)

        assertEquals(DrivingUiPolicy.GUIDE_MAX_MESSAGE_CHARS, clipped.length)
        assertTrue(clipped.endsWith("…"))
    }

    /** 목록은 상한까지만 보여준다 */
    @Test
    fun listIsLimitedToMaxContentItems() {
        val policy = DrivingUiPolicy.from(
            UxRestrictions(
                requiresDistractionOptimization = true,
                activeFlags = UxRestrictionFlags.LIMIT_CONTENT,
                maxContentItems = 3,
            ),
        )

        assertEquals(listOf(1, 2, 3), policy.limit(listOf(1, 2, 3, 4, 5)))
    }

    /** 활성 플래그 이름을 보여준다 */
    @Test
    fun activeFlagNamesAreListed() {
        val restrictions = UxRestrictions(
            requiresDistractionOptimization = true,
            activeFlags = UxRestrictionFlags.NO_VIDEO or UxRestrictionFlags.NO_SETUP,
        )

        assertEquals(listOf("NO_VIDEO", "NO_SETUP"), restrictions.activeFlagNames())
        assertTrue(UxRestrictions.NONE.activeFlagNames().isEmpty())
    }

    /** 플래그 값은 AAOS CarUxRestrictions 상수와 같다 */
    @Test
    fun flagValuesMatchAaosConstants() {
        assertEquals(1, UxRestrictionFlags.NO_DIALPAD)
        assertEquals(2, UxRestrictionFlags.NO_FILTERING)
        assertEquals(4, UxRestrictionFlags.LIMIT_STRING_LENGTH)
        assertEquals(8, UxRestrictionFlags.NO_KEYBOARD)
        assertEquals(16, UxRestrictionFlags.NO_VIDEO)
        assertEquals(32, UxRestrictionFlags.LIMIT_CONTENT)
        assertEquals(64, UxRestrictionFlags.NO_SETUP)
        assertEquals(511, UxRestrictionFlags.FULLY_RESTRICTED)
    }

    /** 속도 단위 변환과 주행 판단 */
    @Test
    fun speedConversionAndMovement() {
        assertEquals(36f, DrivingState.metersPerSecondToKph(10f), 0.001f)
        assertTrue(DrivingState(speedKph = 20f).isMoving)
        assertFalse(DrivingState(speedKph = null).isMoving)
        assertTrue(DrivingState(gear = Gear.REVERSE).isReversing)
    }
}
