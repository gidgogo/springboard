package com.springboard.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 차량용 기본 테마.
 *
 * 수치 근거
 * - 대비 4.5:1 이상 (Pleos UI 디자인 가이드 "색상 및 대비")
 * - 터치 영역 최소 76×76dp, 간격 23dp 권장 (Google Design for Driving / AAOS)
 * - 글자: 최소 12sp, 주요 정보는 크게 (Pleos: 주요 정보 높이 5mm 이상, 보조 3mm 이상)
 * - Pleos 시스템 UI 폰트는 Asta Sans (가이드 Typography). 여기서는 기본 폰트를 쓴다.
 *
 * 브랜드 색이 정해지면 [CarColors] 만 바꾼다. 바꾼 뒤 대비를 꼭 다시 확인한다.
 */
private val CarColors = darkColorScheme(
    primary = Color(0xFF7CC4FF),
    onPrimary = Color(0xFF00213A),
    primaryContainer = Color(0xFF12395C),
    onPrimaryContainer = Color(0xFFD3E9FF),
    secondary = Color(0xFFFFC66D),
    onSecondary = Color(0xFF2B1B00),
    secondaryContainer = Color(0xFF3A3020),
    onSecondaryContainer = Color(0xFFFFE2B5),
    background = Color(0xFF0E1013),
    onBackground = Color(0xFFF2F4F7),
    surface = Color(0xFF171A1F),
    onSurface = Color(0xFFF2F4F7),
    surfaceVariant = Color(0xFF232830),
    onSurfaceVariant = Color(0xFFC3CAD4),
    error = Color(0xFFFF8A80),
    onError = Color(0xFF3A0000),
    outline = Color(0xFF4A515C),
)

private val CarTypography = Typography(
    displayLarge = TextStyle(fontSize = 72.sp, lineHeight = 80.sp, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontSize = 40.sp, lineHeight = 48.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 24.sp, lineHeight = 32.sp),
    bodyMedium = TextStyle(fontSize = 20.sp, lineHeight = 28.sp),
    bodySmall = TextStyle(fontSize = 18.sp, lineHeight = 24.sp),
    labelLarge = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),
)

/** 차량 화면 치수. 화면 코드에서 숫자를 직접 쓰지 말고 이것을 쓴다. */
object CarDimens {
    /** AAOS 최소 터치 영역 76×76dp */
    val MinTouchTarget = 76.dp

    /** 터치 영역 사이 권장 간격 */
    val TouchSpacing = 23.dp

    val ScreenPadding = 32.dp
    val CardPadding = 24.dp
    val CardSpacing = 24.dp
}

@Composable
fun SpringboardTheme(content: @Composable () -> Unit) {
    // 차량 화면은 주·야간 모두 어두운 테마를 기본으로 한다. 주간 모드가 필요하면 여기서 분기한다.
    MaterialTheme(colorScheme = CarColors, typography = CarTypography, content = content)
}
