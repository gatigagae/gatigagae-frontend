package com.gatigagae.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 템플릿에 있던 "다이내믹 컬러(기기 배경화면 색 따라가기)"와 다크 모드는 껐어요.
 * 켜 두면 우리가 정한 파란색 대신 기기 색이 나와서 디자인이 깨져요.
 */
private val GgColorScheme = lightColorScheme(
    primary = GgBlue,
    onPrimary = Color.White,
    background = GgBackground,
    onBackground = GgTextPrimary,
    surface = GgBackground,
    onSurface = GgTextPrimary,
)

@Composable
fun GatigagaeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GgColorScheme,
        typography = Typography,
        content = content,
    )
}
