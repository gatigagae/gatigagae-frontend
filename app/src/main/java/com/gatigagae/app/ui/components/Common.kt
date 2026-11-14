package com.gatigagae.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gatigagae.app.R
import com.gatigagae.app.data.PetStatus
import com.gatigagae.app.ui.theme.AlertRed
import com.gatigagae.app.ui.theme.AlertRedLight
import com.gatigagae.app.ui.theme.ButtonBasic
import com.gatigagae.app.ui.theme.ChipGray
import com.gatigagae.app.ui.theme.DashedBorder
import com.gatigagae.app.ui.theme.Gold100
import com.gatigagae.app.ui.theme.Gold500
import com.gatigagae.app.ui.theme.Gold600
import com.gatigagae.app.ui.theme.Gold800
import com.gatigagae.app.ui.theme.Surface
import com.gatigagae.app.ui.theme.SwitchOn
import com.gatigagae.app.ui.theme.TextPrimary
import com.gatigagae.app.ui.theme.TextSecondary
import com.gatigagae.app.ui.theme.TextTertiary

/*
 * 같이가개 공통 부품 (Figma 디자인 시안 › Components + Pleos ivi 컴포넌트 값)
 * 시안이 앱 실제 크기(1700×1184 / 840×1184)로 그려져 있어서 Figma 숫자 = dp 그대로예요.
 * 글꼴은 Pleos 시스템 글꼴(Asta Sans)을 그대로 써서 따로 지정하지 않아요.
 */

// ───────────────────────── 카드 ─────────────────────────

/** 흰 둥근 카드 (Figma: 흰 바탕, 모서리 32, 여백 위 32 / 아래 36 / 좌우 36) */
@Composable
fun WhiteCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(start = 36.dp, end = 36.dp, top = 32.dp, bottom = 36.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(32.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(Surface, shape)
            .padding(padding),
        content = content,
    )
}

/** 예전 이름 (S5·QR 화면이 씀) → 새 흰 카드 */
@Composable
fun GrayCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(36.dp),
    cornerRadius: Dp = 32.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    Column(
        modifier = modifier
            .clip(shape)
            .background(Surface, shape)
            .padding(padding),
        content = content,
    )
}

// ───────────────────────── 버튼 ─────────────────────────
// ivi_basic_button: Medium = 높이 80, 모서리 24, 글자 30 / Large = 높이 100, 모서리 32, 글자 32

/** 골드 버튼 (주 버튼) */
@Composable
fun GoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    large: Boolean = false,
    color: Color = Gold500,
) {
    BaseButton(text, onClick, modifier, enabled, large, background = color, textColor = Color.White)
}

/** 회색 버튼 (보조 버튼: 아니요, 취소, 전화하기) */
@Composable
fun BasicButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    large: Boolean = false,
) {
    BaseButton(text, onClick, modifier, enabled, large, background = ButtonBasic, textColor = TextPrimary)
}

@Composable
private fun BaseButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    large: Boolean,
    background: Color,
    textColor: Color,
) {
    val shape = RoundedCornerShape(if (large) 32.dp else 24.dp)
    Box(
        modifier = modifier
            .heightIn(min = if (large) 100.dp else 80.dp)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(shape)
            .background(background, shape)
            .clickable(enabled = enabled, onClick = onClick)
            // 글자 잘림 방지(11/15): 시안 버튼 198 폭에 "안내 시작"(32)이 들어가도록 좌우 여백 24
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = if (large) 32.sp else 30.sp,
            lineHeight = if (large) 40.sp else 38.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

/** 예전 이름 → 골드 버튼 (높이·글자 크기는 받은 값 그대로) */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 80.dp,
    fontSize: TextUnit = 30.sp,
    color: Color = Gold500,
    icon: ImageVector? = null,
) {
    LegacyButton(text, onClick, modifier, enabled, height, fontSize, color, Color.White, icon)
}

/** 예전 이름 → 회색 버튼 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 80.dp,
    fontSize: TextUnit = 30.sp,
    icon: ImageVector? = null,
) {
    LegacyButton(text, onClick, modifier, enabled, height, fontSize, ButtonBasic, TextPrimary, icon)
}

@Composable
private fun LegacyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    height: Dp,
    fontSize: TextUnit,
    background: Color,
    textColor: Color,
    icon: ImageVector?,
) {
    val shape = RoundedCornerShape(24.dp)
    Row(
        modifier = modifier
            .height(height)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(shape)
            .background(background, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(fontSize.value.dp))
        }
        Text(text, color = textColor, fontSize = fontSize, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

/** 동그란 아이콘 버튼 (ivi_icon_button · Circle Small: 64, 아이콘 44, 회색 바탕) */
@Composable
fun RoundIconButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(CircleShape)
            .background(ButtonBasic)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(iconRes), contentDescription = contentDescription, tint = TextPrimary, modifier = Modifier.size(44.dp))
    }
}

/** 공조 스위치 (ivi_switch · Medium: 폭 80, 여백 8, 손잡이 32, 켜짐 #02C265) */
@Composable
fun GgSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (checked) SwitchOn else ChipGray)
            .clickable { onCheckedChange(!checked) }
            .padding(8.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .size(32.dp)
                .shadow(4.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
        )
    }
}

// ───────────────────────── 칩·뱃지 ─────────────────────────

/** 알약 모양 라벨 (모든 칩·뱃지의 바탕) */
@Composable
fun Pill(
    text: String,
    textColor: Color,
    background: Color,
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    fontSize: TextUnit = 26.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    horizontalPadding: Dp = 20.dp,
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

/** 연한 골드 칩 (예: 음성 해석 "종류 카페") — 앞 글자는 회색, 뒤 글자는 골드 */
@Composable
fun Chip(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 26.sp,
    textColor: Color = Gold800,
    onClick: (() -> Unit)? = null,
) {
    Pill(text = text, textColor = textColor, background = Gold100, fontSize = fontSize, modifier = modifier, onClick = onClick)
}

/** "🐾 반려견 동반 가능" 뱃지 (Figma 동반 가능 뱃지) */
@Composable
fun PawBadge(text: String = "반려견 동반 가능", modifier: Modifier = Modifier, warn: Boolean = false) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (warn) AlertRedLight else Gold100)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(R.drawable.ic_paw),
            contentDescription = null,
            tint = if (warn) AlertRed else Gold800,
            modifier = Modifier.size(24.dp),
        )
        Text(text, color = if (warn) AlertRed else Gold800, fontSize = 26.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold)
    }
}

/** 동반 상태 뱃지 (S5 화면에서 씀) */
@Composable
fun StatusBadge(status: PetStatus, fontSize: TextUnit = 26.sp) {
    when (status) {
        PetStatus.CONFIRMED -> PawBadge("반려견 동반 가능")
        PetStatus.UNKNOWN -> Pill("동반 정보 없음", textColor = TextSecondary, background = ChipGray, fontSize = fontSize)
        PetStatus.NOT_ALLOWED -> PawBadge("반려견 동반 불가", warn = true)
    }
}

// ───────────────────────── 반려견 그림 ─────────────────────────

/**
 * 견종 일러스트 (Figma Breed Icons · 일러스트형)
 * 지금은 믹스(기본)·말티즈 두 장만 넣었어요. 나머지 견종은 믹스 그림으로 보여요.
 */
@Composable
fun DogAvatar(breed: String, size: Dp, modifier: Modifier = Modifier) {
    val res = if (breed.contains("말티즈") || breed.equals("MALTESE", ignoreCase = true)) R.drawable.breed_maltese else R.drawable.breed_mix
    Image(painterResource(res), contentDescription = null, modifier = modifier.size(size))
}

/** 예전 이름 (QR 화면 등) → 기본 반려견 일러스트 */
@Composable
fun DogPhoto(size: Dp, modifier: Modifier = Modifier, label: String = "사진", fontSize: TextUnit = 16.sp) {
    DogAvatar(breed = "", size = size, modifier = modifier)
}

/** 같이가개 앱 아이콘 (골드 그라데이션 + 흰 발바닥 핀) */
@Composable
fun AppIcon(size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.16f))
            .background(Brush.verticalGradient(listOf(Gold500, Gold600))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(R.drawable.logo_symbol), contentDescription = null, tint = Color.White, modifier = Modifier.size(size))
    }
}

// ───────────────────────── 마이크·진행 바 ─────────────────────────

/**
 * 원형 마이크 버튼 (Figma 마이크 버튼: 골드, 그림자 0/16/40 rgba(204,132,0,.35), 아이콘 = 지름의 1/2)
 * halo = true면 바깥에 골드 후광 2겹 (음성 시트 "듣고 있어요")
 */
@Composable
fun MicButton(
    size: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    halo: Boolean = false,
    color: Color = Gold500,
    iconSize: Dp = size * 0.5f,
) {
    val button = @Composable {
        Box(
            modifier = Modifier
                .size(size)
                .shadow(16.dp, CircleShape, ambientColor = color, spotColor = color)
                .clip(CircleShape)
                .background(color)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.ic_mic), contentDescription = "음성으로 물어보기", tint = Color.White, modifier = Modifier.size(iconSize))
        }
    }
    if (halo) {
        Box(
            modifier = modifier
                .size(size * 1.5f)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(size * 1.25f)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center,
            ) { button() }
        }
    } else {
        Box(modifier = modifier) { button() }
    }
}

/** 진행 막대 (Figma: 높이 20, 모서리 10, 바탕 #ECEDEF) */
@Composable
fun ProgressBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    color: Color = Gold500,
    height: Dp = 20.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(ChipGray)
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

/** 아이콘 + 회색 라벨 (Figma 카드 머리: 아이콘 32, 글자 28 #6B6F76) */
@Composable
fun CardLabel(iconRes: Int, text: String, modifier: Modifier = Modifier, color: Color = TextSecondary) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(iconRes), contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
        Text(text, color = color, fontSize = 28.sp, lineHeight = 36.sp)
    }
}

/** 예전 이름 (Compose 그림 아이콘용) */
@Composable
fun IconLabel(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 32.dp,
    fontSize: TextUnit = 28.sp,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(iconSize))
        Text(text, color = TextSecondary, fontSize = fontSize)
    }
}

/** 점선 테두리 상자 ("추가" 카드) */
@Composable
fun DashedBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawRoundRect(
                color = DashedBorder,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx()),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()), 0f),
                ),
            )
        }
        content()
    }
}

// ───────────────────────── 글자 ─────────────────────────

@Composable
fun SubText(text: String, modifier: Modifier = Modifier, fontSize: TextUnit = 26.sp, color: Color = TextTertiary) {
    Text(text, modifier = modifier, color = color, fontSize = fontSize)
}

@Composable
fun TitleText(text: String, modifier: Modifier = Modifier, fontSize: TextUnit = 36.sp, color: Color = TextPrimary) {
    Text(text, modifier = modifier, color = color, fontSize = fontSize, fontWeight = FontWeight.Bold)
}

/** 화면 바깥 여백 (Figma: 위 24, 좌우 48, 아래 48, 덩어리 사이 28) */
object GgSpace {
    val ScreenH = 48.dp
    val ScreenTop = 24.dp
    val ScreenBottom = 48.dp
    val Gap = 28.dp
}

/** 팝업·시트 공통 그림자 (Figma Shadow/Level3) */
fun Modifier.popupShadow(radius: Dp): Modifier = this.shadow(24.dp, RoundedCornerShape(radius))
