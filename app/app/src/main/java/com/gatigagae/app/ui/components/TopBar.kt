package com.gatigagae.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.gatigagae.app.ui.theme.GgBorder
import com.gatigagae.app.ui.theme.GgTextPrimary

/**
 * 공통 상단 바 (S1-10, S1-11, S2-18, S2-19)
 * 왼쪽: "같이가개" + (반려견 모드면) 파란 뱃지 / 오른쪽: 기어 칩
 */
@Composable
fun TopBar(gear: Gear, dogMode: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("같이가개", color = GgTextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        if (dogMode) {
            Spacer(Modifier.width(12.dp))
            Pill("반려견 모드 켜짐", textColor = Color.White, background = GgBlue, fontSize = 15.sp)
        }
        Spacer(Modifier.weight(1f))
        GearChip(gear)
    }
}

/** 기어 상태 칩. 예: "P · 정차 중" */
@Composable
fun GearChip(gear: Gear) {
    Pill(
        text = gear.label,
        textColor = GgTextPrimary,
        background = Color.White,
        borderColor = GgBorder,
        fontSize = 15.sp,
    )
}
