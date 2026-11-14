package com.gatigagae.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gatigagae.app.R
import com.gatigagae.app.data.DogProfile
import com.gatigagae.app.data.FakeData
import com.gatigagae.app.data.Gear
import com.gatigagae.app.ui.components.CardLabel
import com.gatigagae.app.ui.components.DashedBox
import com.gatigagae.app.ui.components.DogAvatar
import com.gatigagae.app.ui.components.GgSwitch
import com.gatigagae.app.ui.components.MicButton
import com.gatigagae.app.ui.components.Pill
import com.gatigagae.app.ui.components.ProgressBar
import com.gatigagae.app.ui.components.RoundIconButton
import com.gatigagae.app.ui.components.TopBar
import com.gatigagae.app.ui.components.WhiteCard
import com.gatigagae.app.ui.theme.AlertRed
import com.gatigagae.app.ui.theme.BgPrimary
import com.gatigagae.app.ui.theme.GatigagaeTheme
import com.gatigagae.app.ui.theme.Gold100
import com.gatigagae.app.ui.theme.Gold500
import com.gatigagae.app.ui.theme.Gold700
import com.gatigagae.app.ui.theme.Gold800
import com.gatigagae.app.ui.theme.Gold900
import com.gatigagae.app.ui.theme.SurfaceGray
import com.gatigagae.app.ui.theme.TextSecondary

/** 휴식 권고 기준 (2시간 = 120분) */
const val REST_MINUTES = 120

/** 이 폭보다 좁으면 세로(내비 옆 분할) 배치 */
private val PORTRAIT_MAX_WIDTH = 1100.dp

/** 예시 문장 (Figma "음성 제안 문구") */
// 시안의 "가는 길에 휴게소 알려줘"는 휴게소가 빠지면서 식당으로 바꿨어요
private val SUGGESTIONS = listOf("근처 카페 찾아줘", "근처 식당 알려줘", "근처 공원 가자")

/**
 * S2 메인 (Figma "S2 - L" 344:13798 / "S2 - P" 344:14127, 2시간 경과 344:13726)
 *
 * 가로 1700×1184                              세로 840×1184
 * ┌ 상단 바 ─────────────────────────────┐    ┌ 상단 바 ───────────┐
 * │ ┌ 음성 영역 ──────────┐ ┌ 탑승 반려견 ┐│    │ 음성 영역           │
 * │ │   (마이크 240)      │ ├ 실내 온도   ┤│    │ 탑승 중인 반려견    │
 * │ │   예시 문장 3줄      │ ├ 연속 주행   ┤│    │ 실내 온도│연속 주행 │
 * └─┴─────────────────────┴─┴────────────┘┘    └───────────────────┘
 */
@Composable
fun MainScreen(
    profile: DogProfile?,
    dogMode: Boolean,
    gear: Gear,
    indoorTemp: Double,
    drivingMinutes: Int,
    onEditProfile: () -> Unit,
    onMicClick: () -> Unit,
    onExampleClick: (String) -> Unit,
    onRestBannerClick: () -> Unit,
    hvacOn: Boolean = true,
    onHvacChange: (Boolean) -> Unit = {},
    customTemp: Int = profile?.preferredTemp ?: 22,
    onCustomTempChange: (Int) -> Unit = {},
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        val portrait = maxWidth < PORTRAIT_MAX_WIDTH
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 48.dp, end = 48.dp, top = 24.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            TopBar(gear = gear, dogMode = dogMode, compact = portrait)

            if (portrait) {
                VoiceArea(onMicClick, onExampleClick, compact = true, modifier = Modifier.fillMaxWidth().weight(1f))
                DogsCard(profile, gear, onEditProfile, compact = true, modifier = Modifier.fillMaxWidth())
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 320.dp).height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                ) {
                    TempCard(indoorTemp, hvacOn, onHvacChange, customTemp, onCustomTempChange, gear, compact = true,
                        modifier = Modifier.weight(1f).fillMaxHeight())
                    DrivingCard(drivingMinutes, onRestBannerClick, compact = true,
                        modifier = Modifier.weight(1f).fillMaxHeight())
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                ) {
                    VoiceArea(onMicClick, onExampleClick, compact = false, modifier = Modifier.weight(1f).fillMaxHeight())
                    Column(
                        modifier = Modifier.width(560.dp).fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(28.dp),
                    ) {
                        DogsCard(profile, gear, onEditProfile, compact = false, modifier = Modifier.fillMaxWidth())
                        TempCard(indoorTemp, hvacOn, onHvacChange, customTemp, onCustomTempChange, gear, compact = false,
                            modifier = Modifier.fillMaxWidth().weight(1f))
                        DrivingCard(drivingMinutes, onRestBannerClick, compact = false,
                            modifier = Modifier.fillMaxWidth().weight(1f))
                    }
                }
            }
        }
    }
}

// ───────────────────────── 음성 영역 (S2-09, S2-16, S2-17) ─────────────────────────
// 가로: 마이크 240(아이콘 120) → 48 → 안내 32 → 48 → 예시 3줄(36 굵게, 진하기 100/60/30%)
// 세로: 마이크 120, 예시 1줄
@Composable
private fun VoiceArea(
    onMicClick: () -> Unit,
    onExampleClick: (String) -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    WhiteCard(modifier = modifier, padding = PaddingValues(if (compact) 32.dp else 48.dp)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (compact) 24.dp else 48.dp, Alignment.CenterVertically),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (compact) 20.dp else 48.dp),
            ) {
                MicButton(size = if (compact) 120.dp else 240.dp, onClick = onMicClick)
                Text(
                    "반려견과 갈 곳을 물어보세요",
                    color = TextSecondary,
                    fontSize = if (compact) 28.sp else 32.sp,
                    lineHeight = if (compact) 36.sp else 44.sp,
                    textAlign = TextAlign.Center,
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val shown = if (compact) SUGGESTIONS.take(1) else SUGGESTIONS
                shown.forEachIndexed { index, text ->
                    Text(
                        text = "“$text”",
                        color = Gold900,
                        fontSize = 36.sp,
                        lineHeight = 44.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .alpha(listOf(1f, 0.6f, 0.3f)[index])
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onExampleClick(text) } // S2-17: 누르면 그 문장으로 바로 찾기
                            .padding(horizontal = 12.dp),
                    )
                }
            }
        }
    }
}

// ───────────────────────── 탑승 중인 반려견 (S2-02, S2-03, S2-13) ─────────────────────────
// 머리: [발바닥] 탑승 중인 반려견 (수) ........ 프로필 수정
// 카드: 회색 #F4F5F7 모서리 24, 일러스트 72, 이름 30 굵게, "견종 · 나이" 20 / 추가: 점선 + (+) 버튼
@Composable
private fun DogsCard(
    profile: DogProfile?,
    gear: Gear,
    onEditProfile: () -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    val canEdit = gear == Gear.P // S2-13: 정차 중에만 수정
    WhiteCard(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            CardLabel(R.drawable.ic_paw_line, "탑승 중인 반려견")
            Spacer(Modifier.weight(1f))
            Text(
                text = if (profile == null) "프로필 등록" else "프로필 수정",
                color = if (canEdit) Gold700 else TextSecondary,
                fontSize = 24.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .alpha(if (canEdit) 1f else 0.5f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = canEdit, onClick = onEditProfile)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
        Spacer(Modifier.height(if (compact) 24.dp else 32.dp))
        // 기획 결정(11/14): 반려견은 한 마리 기준 → 카드 하나만 (추가 칸 없음)
        if (profile != null) {
            DogCard(profile, compact, Modifier.fillMaxWidth())
        } else {
            AddDogCard(canEdit, onEditProfile, compact, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun DogCard(profile: DogProfile, compact: Boolean, modifier: Modifier) {
    val sub = listOf(profile.breed, profile.age).filter { it.isNotBlank() }.joinToString(" · ")
    val shape = RoundedCornerShape(24.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(SurfaceGray)
            // Figma 11/14 한 마리 시안: 가로 435:51198 (높이 112, 여백 24/20, 그림 72, 간격 20)
            //                         세로 435:51261 (높이 108, 여백 12/18, 그림 56, 간격 12)
            .padding(horizontal = if (compact) 12.dp else 24.dp, vertical = if (compact) 18.dp else 20.dp),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DogAvatar(profile.breed, if (compact) 56.dp else 72.dp)
        Column {
            Text(profile.name, color = Gold900, fontSize = 30.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(sub, color = TextSecondary, fontSize = 20.sp, lineHeight = 34.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun AddDogCard(enabled: Boolean, onClick: () -> Unit, compact: Boolean, modifier: Modifier) {
    DashedBox(
        modifier = modifier
            .heightIn(min = if (compact) 96.dp else 160.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(RoundedCornerShape(24.dp))
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        val content = @Composable {
            RoundIconButton(R.drawable.ic_plus, "반려견 프로필 등록", onClick = onClick, enabled = enabled)
            Text("프로필 등록", color = TextSecondary, fontSize = 30.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold)
        }
        if (compact) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) { content() }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
        }
    }
}

// ───────────────────────── 실내 온도 (S2-04~06) ─────────────────────────
// 머리: [온도계] 실내 온도 ...... 공조 [스위치] / 값 60 + °C 32 / 맞춤 설정 줄(연한 골드, 정차 중엔 −/+ 버튼)
@Composable
private fun TempCard(
    indoorTemp: Double,
    hvacOn: Boolean,
    onHvacChange: (Boolean) -> Unit,
    customTemp: Int,
    onCustomTempChange: (Int) -> Unit,
    gear: Gear,
    compact: Boolean,
    modifier: Modifier,
) {
    WhiteCard(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            CardLabel(R.drawable.ic_temp, "실내 온도")
            Spacer(Modifier.weight(1f))
            if (!compact) {
                Text("공조", color = TextSecondary, fontSize = 28.sp)
                Spacer(Modifier.width(20.dp))
            }
            // TODO(Vehicle SDK 2차): setHvacPower 연결. 지금은 화면에서만 켜고 꺼져요
            GgSwitch(checked = hvacOn, onCheckedChange = onHvacChange)
        }
        Spacer(Modifier.height(12.dp))
        BigNumber(listOf(String.format("%.1f", indoorTemp) to "°C"), Gold900)
        Spacer(Modifier.weight(1f).heightIn(min = 12.dp))

        val canAdjust = gear == Gear.P
        if (compact && canAdjust) {
            // 세로 시안(435:51261): [−]  맞춤 설정 / 23°C  [+]  (높이 76, 연한 골드)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 76.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Gold100)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RoundIconButton(R.drawable.ic_minus, "온도 내림", onClick = { onCustomTempChange(customTemp - 1) })
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("맞춤 설정", color = Gold800, fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold)
                    Text("${customTemp}°C", color = Gold800, fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold)
                }
                RoundIconButton(R.drawable.ic_plus, "온도 올림", onClick = { onCustomTempChange(customTemp + 1) })
            }
            return@WhiteCard
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Gold100)
                .padding(start = 20.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("맞춤 설정", color = Gold800, fontSize = 26.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            if (canAdjust && !compact) {
                RoundIconButton(R.drawable.ic_minus, "온도 내림", onClick = { onCustomTempChange(customTemp - 1) })
                Spacer(Modifier.width(8.dp))
            }
            Text(
                "${customTemp}°C",
                color = Gold800,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = if (canAdjust && !compact) 0.dp else 16.dp),
            )
            if (canAdjust && !compact) {
                Spacer(Modifier.width(8.dp))
                RoundIconButton(R.drawable.ic_plus, "온도 올림", onClick = { onCustomTempChange(customTemp + 1) })
            }
        }
    }
}

// ───────────────────────── 연속 주행 (S2-07, S2-08, S2-14, S2-15) ─────────────────────────
// 2시간 전: 골드 막대 + "2시간이 되면 알려 드려요"
// 2시간 후: 빨간 테두리·숫자·막대(꽉 참) + "잠시 쉬어 가세요" (Figma 344:13726)
@Composable
private fun DrivingCard(
    drivingMinutes: Int,
    onRestBannerClick: () -> Unit,
    compact: Boolean,
    modifier: Modifier,
) {
    val overdue = drivingMinutes >= REST_MINUTES
    val accent = if (overdue) AlertRed else Gold900
    val shape = RoundedCornerShape(32.dp)
    val cardModifier = if (overdue) {
        modifier.clip(shape).border(3.dp, AlertRed, shape).clickable(onClick = onRestBannerClick)
    } else modifier

    WhiteCard(modifier = cardModifier) {
        CardLabel(R.drawable.ic_clock, "연속 주행", color = if (overdue) AlertRed else TextSecondary)
        Spacer(Modifier.height(16.dp))
        val hours = drivingMinutes / 60
        val minutes = drivingMinutes % 60
        val parts = buildList {
            if (hours > 0) add("$hours" to "시간")
            add("$minutes" to "분")
        }
        BigNumber(parts, accent)
        Spacer(Modifier.weight(1f).heightIn(min = 16.dp))
        ProgressBar(fraction = drivingMinutes / REST_MINUTES.toFloat(), color = if (overdue) AlertRed else Gold500)
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (overdue) "잠시 쉬어 가세요" else "2시간이 되면 알려 드려요",
            color = if (overdue) AlertRed else TextSecondary,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            fontWeight = if (overdue) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

/** 큰 숫자 + 작은 단위 ("26.5 °C", "1 시간 24 분") — 숫자 60, 단위 32 */
@Composable
private fun BigNumber(parts: List<Pair<String, String>>, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Bottom) {
        parts.forEach { (number, unit) ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                NumberText(number, 60.sp, color)
                NumberText(unit, 32.sp, color, Modifier.padding(bottom = 10.dp))
            }
        }
    }
}

@Composable
private fun NumberText(text: String, size: TextUnit, color: Color, modifier: Modifier = Modifier) {
    Text(text, color = color, fontSize = size, lineHeight = size, fontWeight = FontWeight.Bold, modifier = modifier, maxLines = 1)
}

// ───────────────────────── 미리보기 ─────────────────────────

@Preview(name = "S2 가로", widthDp = 1700, heightDp = 1184)
@Composable
private fun MainScreenPreview() {
    GatigagaeTheme { MainScreen(FakeData.profile, true, Gear.P, FakeData.INDOOR_TEMP, 84, {}, {}, {}, {}) }
}

@Preview(name = "S2 세로", widthDp = 840, heightDp = 1184)
@Composable
private fun MainScreenPortraitPreview() {
    GatigagaeTheme { MainScreen(FakeData.profile, true, Gear.D, FakeData.INDOOR_TEMP, 125, {}, {}, {}, {}) }
}
