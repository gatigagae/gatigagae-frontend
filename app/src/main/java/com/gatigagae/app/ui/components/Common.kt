package com.gatigagae.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gatigagae.app.data.PetStatus
import com.gatigagae.app.ui.theme.GgBlue
import com.gatigagae.app.ui.theme.GgBlueLight
import com.gatigagae.app.ui.theme.GgBorder
import com.gatigagae.app.ui.theme.GgButtonBorder
import com.gatigagae.app.ui.theme.GgCard
import com.gatigagae.app.ui.theme.GgChipBorder
import com.gatigagae.app.ui.theme.GgChipText
import com.gatigagae.app.ui.theme.GgMicHalo
import com.gatigagae.app.ui.theme.GgOrange
import com.gatigagae.app.ui.theme.GgOrangeLight
import com.gatigagae.app.ui.theme.GgPhotoBorder
import com.gatigagae.app.ui.theme.GgRed
import com.gatigagae.app.ui.theme.GgRedLight
import com.gatigagae.app.ui.theme.GgTextCaption
import com.gatigagae.app.ui.theme.GgTextPrimary
import com.gatigagae.app.ui.theme.GgTextSecondary
import com.gatigagae.app.ui.theme.GgTrack

/*
 * 여러 화면에서 같이 쓰는 부품 모음 (Figma 컴포넌트와 1:1)
 *
 * ※ 크기 읽는 법
 *   Figma 가로 화면은 2560×1440 캔버스에 그려져 있는데, 실제 앱 영역은 1700×1184dp예요.
 *   그래서 Figma 숫자 × 2/3 = 코드 dp/sp 로 옮겼어요. (예: Figma 버튼 높이 140.4 → 94.dp)
 *   세로(분할) 화면 Figma(840×1184)는 실제 크기라 숫자를 그대로 썼어요.
 */

// ───────────────────────── 카드 ─────────────────────────

/** 회색 둥근 카드 + 연한 테두리 (Figma: 배경 #F3F4F6, 테두리 #E3E5E8, 모서리 36 → 24dp) */
@Composable
fun GrayCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(38.dp),
    cornerRadius: Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    Column(
        modifier = modifier
            .clip(shape)
            .background(GgCard, shape)
            .border(1.dp, GgBorder, shape)
            .padding(padding),
        content = content,
    )
}

// ───────────────────────── 버튼 ─────────────────────────
// Figma Button 컴포넌트: M = 높이 140.4 / 글자 39.6 → 94dp / 26sp
//                        S = 높이 111.6 / 글자 36   → 74dp / 24sp
// 모서리 14.4 → 10dp

/** 파란 채움 버튼 (Figma Button / Primary) */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 94.dp,
    fontSize: TextUnit = 26.sp,
    color: Color = GgBlue,
    icon: ImageVector? = null,
) {
    ButtonBase(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        height = height,
        fontSize = fontSize,
        background = color,
        borderColor = null,
        textColor = Color.White,
        fontWeight = FontWeight.Bold,
        icon = icon,
    )
}

/** 흰 바탕 + 회색 테두리 버튼 (Figma Button / Secondary) */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 94.dp,
    fontSize: TextUnit = 26.sp,
    icon: ImageVector? = null,
) {
    ButtonBase(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        height = height,
        fontSize = fontSize,
        background = Color.White,
        borderColor = GgButtonBorder,
        textColor = GgTextPrimary,
        fontWeight = FontWeight.Medium,
        icon = icon,
    )
}

@Composable
private fun ButtonBase(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    height: Dp,
    fontSize: TextUnit,
    background: Color,
    borderColor: Color?,
    textColor: Color,
    fontWeight: FontWeight,
    icon: ImageVector?,
) {
    val shape = RoundedCornerShape(10.dp)
    var m = modifier
        .height(height)
        .alpha(if (enabled) 1f else 0.4f)
        .clip(shape)
        .background(background, shape)
    if (borderColor != null) m = m.border(1.dp, borderColor, shape)
    m = m.clickable(enabled = enabled, onClick = onClick).padding(horizontal = 24.dp)

    Row(
        modifier = m,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(fontSize.value.dp))
        }
        Text(text, color = textColor, fontSize = fontSize, fontWeight = fontWeight, maxLines = 1)
    }
}

// ───────────────────────── 칩·뱃지 ─────────────────────────

/**
 * 흰 알약 칩 (Figma Chip: 테두리 #D6DAE0, 글자 32.4 → 22sp, 안쪽 여백 28.8/12.6 → 19/8dp)
 * 기어 칩, 예시 문구 칩, 음성 해석 칩에 써요.
 */
@Composable
fun Chip(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 22.sp,
    textColor: Color = GgChipText,
    onClick: (() -> Unit)? = null,
) {
    Pill(
        text = text,
        textColor = textColor,
        background = Color.White,
        borderColor = GgChipBorder,
        fontSize = fontSize,
        fontWeight = FontWeight.Normal,
        horizontalPadding = 19.dp,
        verticalPadding = 8.dp,
        modifier = modifier,
        onClick = onClick,
    )
}

/** 알약 모양 라벨 (모든 칩·뱃지의 바탕 모양) */
@Composable
fun Pill(
    text: String,
    textColor: Color,
    background: Color,
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Medium,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 8.dp,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(50)
    var m = modifier.clip(shape).background(background, shape)
    if (borderColor != null) m = m.border(1.dp, borderColor, shape)
    if (onClick != null) m = m.clickable(onClick = onClick)
    Text(
        text = text,
        modifier = m.padding(horizontal = horizontalPadding, vertical = verticalPadding),
        color = textColor,
        fontSize = fontSize,
        fontWeight = fontWeight,
        maxLines = 1,
    )
}

/**
 * 동반 상태 뱃지 (Figma Status Badge: 글자 27 → 18sp, 여백 25.2/9 → 17/6dp)
 * 동반 가능 = 파랑 / 알수없음 = 주황 / 동반 불가 = 빨강
 */
@Composable
fun StatusBadge(status: PetStatus, fontSize: TextUnit = 18.sp) {
    val (label, color, bg) = when (status) {
        PetStatus.CONFIRMED -> Triple("동반 가능", GgBlue, GgBlueLight)
        PetStatus.UNKNOWN -> Triple("알수없음", GgOrange, GgOrangeLight)
        PetStatus.NOT_ALLOWED -> Triple("동반 불가", GgRed, GgRedLight)
    }
    Pill(
        text = label,
        textColor = color,
        background = bg,
        borderColor = color,
        fontSize = fontSize,
        horizontalPadding = 17.dp,
        verticalPadding = 6.dp,
    )
}

// ───────────────────────── 사진·마이크·진행 바 ─────────────────────────

/** 반려견 사진 자리 (흰 원 + 점선 #B5BAC2). 사진 기능 전까지 기본 모양 */
@Composable
fun DogPhoto(
    size: Dp,
    modifier: Modifier = Modifier,
    label: String = "사진",
    fontSize: TextUnit = 16.sp,
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = Color.White)
            drawCircle(
                color = GgPhotoBorder,
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()), 0f),
                ),
            )
        }
        Text(label, color = GgTextCaption, fontSize = fontSize)
    }
}

/**
 * 원형 마이크 버튼 (Figma Mic Button: 237.6 → 158dp, 아이콘 86.4 → 58dp)
 * halo = true면 바깥에 연파랑 후광 (음성 시트, Figma Mic Halo 273.6 : 버튼 216)
 * color: 평소 파랑, 인식 실패 때 빨강
 */
@Composable
fun MicButton(
    size: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    halo: Boolean = false,
    color: Color = GgBlue,
    iconSize: Dp = size * 0.364f,
) {
    val button = @Composable {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(color)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(GgIcons.Mic, contentDescription = "음성으로 물어보기", tint = Color.White, modifier = Modifier.size(iconSize))
        }
    }
    if (halo) {
        Box(
            modifier = modifier
                .size(size * 1.267f)
                .clip(CircleShape)
                .background(GgMicHalo),
            contentAlignment = Alignment.Center,
        ) { button() }
    } else {
        Box(modifier = modifier) { button() }
    }
}

/** 진행 막대 (Figma: 높이 18 → 12dp, 바탕 #E1E4E8). fraction: 0.0 ~ 1.0 */
@Composable
fun ProgressBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    color: Color = GgBlue,
    height: Dp = 12.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(GgTrack)
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}

/** 아이콘 + 회색 라벨 한 줄 (Figma 온도·주행 카드 라벨: 아이콘 36 / 글자 32.4 → 24dp / 22sp) */
@Composable
fun IconLabel(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    fontSize: TextUnit = 22.sp,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = GgTextSecondary, modifier = Modifier.size(iconSize))
        Text(text, color = GgTextSecondary, fontSize = fontSize)
    }
}

// ───────────────────────── 글자 ─────────────────────────

/** 회색 설명 글자 (#5B616B) */
@Composable
fun SubText(text: String, modifier: Modifier = Modifier, fontSize: TextUnit = 20.sp, color: Color = GgTextSecondary) {
    Text(text, modifier = modifier, color = color, fontSize = fontSize)
}

/** 굵은 제목 (#16181D) */
@Composable
fun TitleText(text: String, modifier: Modifier = Modifier, fontSize: TextUnit = 36.sp, color: Color = GgTextPrimary) {
    Text(text, modifier = modifier, color = color, fontSize = fontSize, fontWeight = FontWeight.Bold)
}

/** 화면 바깥 여백 등 자주 쓰는 숫자 */
object GgSpace {
    val ScreenH = 53.dp      // 가로 화면 좌우 여백 (Figma 79.2)
    val CardGap = 34.dp      // 카드 사이 간격 (Figma 50.4)
    val Portrait = 40.dp     // 세로 화면 바깥 여백 (Figma 세로 프레임 그대로)
}
