package com.gatigagae.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gatigagae.app.data.Gear
import com.gatigagae.app.ui.theme.GgBlue
import com.gatigagae.app.ui.theme.GgTextPrimary
import com.gatigagae.app.ui.theme.GgTextSecondary

/**
 * 공통 상단 바 (S2 Figma "App Bar")
 * 왼쪽: "같이가개" + (반려견 모드면) 파란 뱃지 / 오른쪽: 기어 칩
 *
 * compact = true → 세로(분할) 화면용 크기 (Figma 세로 프레임 값 그대로)
 * compact = false → 가로 화면용 (Figma 가로 값 × 2/3)
 */
@Composable
fun TopBar(
    gear: Gear,
    dogMode: Boolean,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    contentPadding: PaddingValues =
        if (compact) PaddingValues(0.dp) else PaddingValues(start = 53.dp, end = 53.dp, top = 40.dp, bottom = 0.dp),
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 14.dp else 19.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "같이가개",
            color = GgTextPrimary,
            fontSize = if (compact) 28.sp else 26.sp,
            fontWeight = FontWeight.Bold,
        )
        if (dogMode) {
            Pill(
                text = "반려견 모드 켜짐",
                textColor = Color.White,
                background = GgBlue,
                fontSize = if (compact) 18.sp else 19.sp,
                fontWeight = if (compact) FontWeight.Medium else FontWeight.Bold,
                horizontalPadding = if (compact) 14.dp else 17.dp,
                verticalPadding = 6.dp,
            )
        }
        Spacer(Modifier.weight(1f))
        GearChip(gear, compact = compact)
    }
}

/** 기어 상태 칩. 예: "P · 정차 중" */
@Composable
fun GearChip(gear: Gear, compact: Boolean = false) {
    if (compact) {
        Chip(gear.label, fontSize = 20.sp, textColor = GgTextSecondary)
    } else {
        Chip(gear.label)
    }
}
