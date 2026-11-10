package com.gatigagae.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gatigagae.app.data.PetStatus
import com.gatigagae.app.ui.theme.GgBlue
import com.gatigagae.app.ui.theme.GgBlueLight
import com.gatigagae.app.ui.theme.GgBorder
import com.gatigagae.app.ui.theme.GgCard
import com.gatigagae.app.ui.theme.GgOrange
import com.gatigagae.app.ui.theme.GgOrangeLight
import com.gatigagae.app.ui.theme.GgRed
import com.gatigagae.app.ui.theme.GgRedLight
import com.gatigagae.app.ui.theme.GgTextPrimary
import com.gatigagae.app.ui.theme.GgTextSecondary

/**
 * 여러 화면에서 같이 쓰는 부품 모음.
 * 디자인이 나오면 여기만 고쳐도 모든 화면이 같이 바뀌어요.
 */

/** 회색 둥근 카드 (S2 프로필·온도·주행·마이크 카드, S1 팝업 등) */
@Composable
fun GrayCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(GgCard)
            .padding(28.dp),
        content = content,
    )
}

/** 파란 채움 버튼 (주 버튼) */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = GgBlue, contentColor = Color.White),
    ) {
        Text(text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

/** 흰 바탕 + 회색 테두리 버튼 (보조 버튼) */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, GgBorder),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = GgTextPrimary),
    ) {
        Text(text, fontSize = 20.sp, fontWeight = FontWeight.Medium)
    }
}

/** 알약 모양 작은 라벨 (기어 칩, 반려견 모드 뱃지, 예시 발화 칩, 해석 결과 칩) */
@Composable
fun Pill(
    text: String,
    textColor: Color,
    background: Color,
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    fontSize: TextUnit = 16.sp,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(50)
    var m = modifier.clip(shape).background(background, shape)
    if (borderColor != null) m = m.border(1.dp, borderColor, shape)
    if (onClick != null) m = m.clickable(onClick = onClick)
    Text(
        text = text,
        modifier = m.padding(horizontal = 16.dp, vertical = 8.dp),
        color = textColor,
        fontSize = fontSize,
        fontWeight = FontWeight.Medium,
    )
}

/** 동반 상태 뱃지 (S3-05, S5-03) */
@Composable
fun StatusBadge(status: PetStatus, fontSize: TextUnit = 15.sp) {
    when (status) {
        PetStatus.CONFIRMED -> Pill("동반 가능 확인됨", GgBlue, GgBlueLight, borderColor = GgBlue, fontSize = fontSize)
        PetStatus.UNKNOWN -> Pill("방문 전 확인 필요", GgOrange, GgOrangeLight, borderColor = GgOrange, fontSize = fontSize)
        PetStatus.NOT_ALLOWED -> Pill("동반 불가", GgRed, GgRedLight, borderColor = GgRed, fontSize = fontSize)
    }
}

/** 반려견 사진 자리 (점선 원). 사진 기능 전까지 기본 모양 (S1-02, S2-02) */
@Composable
fun DogPhoto(size: Dp, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = Color.White)
            drawCircle(
                color = GgBorder,
                style = Stroke(
                    width = 3.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f),
                ),
            )
        }
        Text("사진", color = GgTextSecondary, fontSize = 16.sp)
    }
}

/** 파란 원형 마이크 버튼. halo = true면 "듣는 중" 후광 표시 (S2-09, S2-1-02) */
@Composable
fun MicButton(
    size: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    halo: Boolean = false,
) {
    Box(modifier = modifier.size(size * 1.3f), contentAlignment = Alignment.Center) {
        if (halo) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(GgBlueLight)
            )
        }
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(GgBlue)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            // 아이콘은 디자인 나오면 이미지로 교체
            Text("🎤", fontSize = (size.value * 0.38f).sp)
        }
    }
}

/** 진행 막대 (S2-14). fraction: 0.0 ~ 1.0 */
@Composable
fun ProgressBar(fraction: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(50))
            .background(GgBorder)
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(GgBlue)
        )
    }
}

/** 화면 제목 아래 작은 회색 설명 */
@Composable
fun SubText(text: String, modifier: Modifier = Modifier, fontSize: TextUnit = 16.sp) {
    Text(text, modifier = modifier, color = GgTextSecondary, fontSize = fontSize)
}

/** 큰 제목 */
@Composable
fun TitleText(text: String, modifier: Modifier = Modifier, fontSize: TextUnit = 28.sp) {
    Text(text, modifier = modifier, color = GgTextPrimary, fontSize = fontSize, fontWeight = FontWeight.Bold)
}
