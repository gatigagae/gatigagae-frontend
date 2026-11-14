package com.gatigagae.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gatigagae.app.R
import com.gatigagae.app.data.Gear
import com.gatigagae.app.ui.theme.ChipGray
import com.gatigagae.app.ui.theme.Gold100
import com.gatigagae.app.ui.theme.Gold500
import com.gatigagae.app.ui.theme.Gold800
import com.gatigagae.app.ui.theme.Gold900
import com.gatigagae.app.ui.theme.TextPrimary

/**
 * 상단 바 (Figma "상단 바": ivi_title + 반려견 모드 뱃지 + 기어 상태 칩, 높이 80)
 *
 * onBack이 없으면: [발바닥 로고] 같이가개          (메인)
 * onBack이 있으면: [<] 추천 장소                  (깊이 화면: S3, 장소 정보)
 */
@Composable
fun TopBar(
    gear: Gear,
    dogMode: Boolean,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    title: String = "같이가개",
    onBack: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding)
            .height(80.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            Icon(
                painterResource(R.drawable.ic_arrow_back),
                contentDescription = "뒤로",
                tint = TextPrimary,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBack),
            )
            Spacer(Modifier.size(8.dp))
        } else {
            Icon(
                painterResource(R.drawable.logo_symbol),
                contentDescription = null,
                tint = Gold500,
                modifier = Modifier.size(80.dp),
            )
        }
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 40.sp,
            lineHeight = 52.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (dogMode) ModeBadge()
        GearChip(gear, compact)
    }
}

/** "● 반려견 모드" 뱃지 */
@Composable
fun ModeBadge() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Gold100)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(12.dp).clip(CircleShape).background(Gold500))
        Text("반려견 모드", color = Gold800, fontSize = 26.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold)
    }
}

/** 기어 상태 칩 "[P] 정차 중" / "[D] 주행 중" */
@Composable
fun GearChip(gear: Gear, compact: Boolean = false) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(ChipGray)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = gear.name,
            color = Color.White,
            fontSize = 26.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Gold900)
                .padding(horizontal = 10.dp),
        )
        Text(
            text = if (gear == Gear.P) "정차 중" else "주행 중",
            color = Gold900,
            fontSize = 26.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
