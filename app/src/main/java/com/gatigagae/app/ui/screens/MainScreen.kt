package com.gatigagae.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gatigagae.app.data.DogProfile
import com.gatigagae.app.data.FakeData
import com.gatigagae.app.data.Gear
import com.gatigagae.app.data.TextUtils
import com.gatigagae.app.ui.components.Chip
import com.gatigagae.app.ui.components.DogPhoto
import com.gatigagae.app.ui.components.GgIcons
import com.gatigagae.app.ui.components.GrayCard
import com.gatigagae.app.ui.components.IconLabel
import com.gatigagae.app.ui.components.MicButton
import com.gatigagae.app.ui.components.PrimaryButton
import com.gatigagae.app.ui.components.ProgressBar
import com.gatigagae.app.ui.components.SecondaryButton
import com.gatigagae.app.ui.components.SubText
import com.gatigagae.app.ui.components.TitleText
import com.gatigagae.app.ui.components.TopBar
import com.gatigagae.app.ui.theme.GatigagaeTheme
import com.gatigagae.app.ui.theme.GgBackground
import com.gatigagae.app.ui.theme.GgBlue
import com.gatigagae.app.ui.theme.GgRed
import com.gatigagae.app.ui.theme.GgTextCaption
import com.gatigagae.app.ui.theme.GgTextPrimary
import com.gatigagae.app.ui.theme.GgTextSecondary

/** 휴식 권고 기준 (2시간 = 120분) */
const val REST_MINUTES = 120

/** 이 폭보다 좁으면 세로(내비 옆 분할) 화면 배치로 바꿔요. 분할 화면 앱 폭 = 840dp */
private val PORTRAIT_MAX_WIDTH = 1100.dp

/**
 * S2 메인 화면
 *
 * 가로 (Figma 18:75, 앱만 떠 있을 때 1700×1184)
 * ┌───────────┬──────────────┬──────────────┐
 * │           │  실내 온도    │ 연속 주행 시간 │
 * │ 프로필 카드 ├──────────────┴──────────────┤
 * │           │        마이크 카드            │
 * └───────────┴─────────────────────────────┘
 *
 * 세로 (Figma 114:263 / 152:909, 내비 옆 분할 840×1184)
 * ┌─────────────────────┐
 * │      프로필 카드      │
 * ├──────────┬──────────┤
 * │ 실내 온도 │ 주행 시간 │
 * ├──────────┴──────────┤
 * │      마이크 카드      │
 * └─────────────────────┘
 *
 * 연속 주행 2시간이 넘으면 주행 시간 카드가 빨간색으로 바뀌어요 (Figma 152:909 "2시간 경과")
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
) {
    val dogName = profile?.name ?: ""

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(GgBackground)
    ) {
        if (maxWidth < PORTRAIT_MAX_WIDTH) {
            // ───── 세로 (분할 화면) ─────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                TopBar(gear = gear, dogMode = dogMode, compact = true)
                ProfileCard(profile, gear, onEditProfile, compact = true, modifier = Modifier.fillMaxWidth())
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 269.dp)
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    TemperatureCard(
                        dogName, indoorTemp, profile?.preferredTemp ?: 23, dogMode, compact = true,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                    DrivingCard(
                        drivingMinutes, onRestBannerClick, compact = true,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
                MicCard(
                    dogName, onMicClick, onExampleClick, compact = true,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
            }
        } else {
            // ───── 가로 (앱 전체 화면) ─────
            // Figma 값 × 2/3: 좌우 여백 79.2 → 53dp, 카드 사이 50.4 → 34dp
            Column(modifier = Modifier.fillMaxSize()) {
                TopBar(gear = gear, dogMode = dogMode)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(start = 53.dp, end = 53.dp, top = 44.dp, bottom = 34.dp),
                    horizontalArrangement = Arrangement.spacedBy(34.dp),
                ) {
                    // 왼쪽 프로필 카드 (Figma 폭 716.4 → 478) : 오른쪽 (1630.8 → 1087)
                    ProfileCard(
                        profile, gear, onEditProfile, compact = false,
                        modifier = Modifier.weight(478f).fillMaxHeight(),
                    )
                    Column(
                        modifier = Modifier.weight(1087f).fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(34.dp),
                    ) {
                        // 위 카드 2장 (Figma 높이 478.8 → 319) : 마이크 카드 (673.2 → 449)
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(319f),
                            horizontalArrangement = Arrangement.spacedBy(34.dp),
                        ) {
                            TemperatureCard(
                                dogName, indoorTemp, profile?.preferredTemp ?: 23, dogMode, compact = false,
                                modifier = Modifier.weight(528f).fillMaxHeight(),
                            )
                            DrivingCard(
                                drivingMinutes, onRestBannerClick, compact = false,
                                modifier = Modifier.weight(525f).fillMaxHeight(),
                            )
                        }
                        MicCard(
                            dogName, onMicClick, onExampleClick, compact = false,
                            modifier = Modifier.fillMaxWidth().weight(449f),
                        )
                    }
                }
            }
        }
    }
}

// ───────────────────────── 프로필 카드 (S2-02, S2-03, S2-13) ─────────────────────────
// 가로: 여백 57.6 → 38, 사진 194.4 → 130, 이름 72 → 48sp, 라벨 34.2 → 23sp(폭 198 → 132), 값 36 → 24sp
// 세로: 여백 32/28, 사진 128, 이름 48sp, 라벨·값 22sp(라벨 폭 104)
@Composable
private fun ProfileCard(
    profile: DogProfile?,
    gear: Gear,
    onEditProfile: () -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    val canEdit = gear == Gear.P // 정차 중에만 수정 가능
    val editText = if (canEdit) "프로필 수정" else "프로필 수정 (정차 중에만)"

    GrayCard(
        modifier = modifier,
        padding = if (compact) PaddingValues(horizontal = 32.dp, vertical = 28.dp) else PaddingValues(38.dp),
    ) {
        if (profile == null) {
            // S2-03: 프로필이 없을 때
            if (!compact) Spacer(Modifier.weight(1f))
            TitleText("프로필이 없어요", fontSize = 32.sp)
            Spacer(Modifier.height(10.dp))
            SubText("반려견 이름을 등록하면 안내에 이름을 불러 드려요", fontSize = 22.sp)
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                text = if (canEdit) "프로필 등록" else "정차 후 등록할 수 있어요",
                onClick = onEditProfile,
                enabled = canEdit,
                height = if (compact) 66.dp else 74.dp,
                fontSize = if (compact) 22.sp else 26.sp,
                modifier = Modifier.fillMaxWidth(),
            )
            if (!compact) Spacer(Modifier.weight(1f))
            return@GrayCard
        }

        if (compact) {
            // 세로: [사진] [이름 + 견종·나이·특이사항] 한 줄, 아래 수정 버튼
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                DogPhoto(size = 128.dp, fontSize = 18.sp)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    TitleText(profile.name, fontSize = 48.sp)
                    LabelValue("견종", profile.breed, labelWidth = 104, fontSize = 22.sp, valueSize = 22.sp)
                    LabelValue("나이", profile.age, labelWidth = 104, fontSize = 22.sp, valueSize = 22.sp)
                    LabelValue("특이사항", profile.note, labelWidth = 104, fontSize = 22.sp, valueSize = 22.sp)
                }
            }
            Spacer(Modifier.height(20.dp))
            SecondaryButton(
                text = editText, onClick = onEditProfile, enabled = canEdit,
                height = 66.dp, fontSize = 22.sp, modifier = Modifier.fillMaxWidth(),
            )
        } else {
            // 가로: 사진+이름 / 정보 3줄 / (빈 공간) / 수정 버튼
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(31.dp),
            ) {
                DogPhoto(size = 130.dp, fontSize = 16.sp)
                TitleText(profile.name, fontSize = 48.sp)
            }
            Spacer(Modifier.height(43.dp))
            Column(verticalArrangement = Arrangement.spacedBy(26.dp)) {
                LabelValue("견종", profile.breed, labelWidth = 132, fontSize = 23.sp, valueSize = 24.sp)
                LabelValue("나이", profile.age, labelWidth = 132, fontSize = 23.sp, valueSize = 24.sp)
                LabelValue("특이사항", profile.note, labelWidth = 132, fontSize = 23.sp, valueSize = 24.sp)
            }
            Spacer(Modifier.weight(1f))
            // S2-13: 정차 중에만 누를 수 있음 (Figma 높이 111.6 → 74, 글자 39.6 → 26sp)
            SecondaryButton(
                text = editText, onClick = onEditProfile, enabled = canEdit,
                height = 74.dp, fontSize = 26.sp, modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun LabelValue(label: String, value: String, labelWidth: Int, fontSize: TextUnit, valueSize: TextUnit) {
    Row {
        Text(label, color = GgTextSecondary, fontSize = fontSize, modifier = Modifier.width(labelWidth.dp))
        Text(value.ifBlank { "-" }, color = GgTextPrimary, fontSize = valueSize, modifier = Modifier.weight(1f))
    }
}

// ───────────────────────── 실내 온도 카드 (S2-04, S2-05, S2-06) ─────────────────────────
// 가로: 여백 위 54 / 아래 50.4 / 좌우 57.6 → 36 / 34 / 38, 값 108 → 72sp, 흰 박스 높이 90 → 60
// 세로: 여백 32/28, 값 52sp, 흰 박스 여백 16/12
@Composable
private fun TemperatureCard(
    dogName: String,
    indoorTemp: Double,
    preferredTemp: Int,
    dogMode: Boolean,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    GrayCard(modifier = modifier, padding = cardPadding(compact)) {
        IconLabel(
            GgIcons.Thermometer, "실내 온도",
            iconSize = if (compact) 26.dp else 24.dp,
            fontSize = 22.sp,
        )
        Spacer(Modifier.height(if (compact) 16.dp else 12.dp))
        // TODO(SDK): getHvacTemperatureCurrent 값으로 교체
        BigValue(TextUtils.temperature(indoorTemp), compact, GgTextPrimary)

        Spacer(Modifier.weight(1f).heightIn(min = if (compact) 16.dp else 12.dp))

        // 흰 박스: "콩이 설정 온도 23℃"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (compact) 53.dp else 60.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .padding(horizontal = if (compact) 16.dp else 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${dogName.ifBlank { "반려견" }} 설정 온도",
                color = GgTextSecondary,
                fontSize = 20.sp,
                modifier = Modifier.weight(1f),
            )
            Text("${preferredTemp}℃", color = GgBlue, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(if (compact) 16.dp else 12.dp))
        Text(
            text = if (dogMode) "공조 켜짐 · 설정 온도로 맞추는 중" else "반려견 모드가 꺼져 있어요",
            color = if (compact) GgTextSecondary else GgTextCaption,
            fontSize = if (compact) 18.sp else 19.sp,
        )
    }
}

// ───────────────────────── 연속 주행 시간 카드 (S2-07, S2-08, S2-14, S2-15) ─────────────────────────
// 2시간 전: 검정 숫자 + 파란 막대 + "2시간이 되면 쉬어 갈 곳을 알려 드려요"
// 2시간 후: 빨간 숫자 + 빨간 막대(꽉 참) + "주행 2 시간이 지났습니다. 휴식을 취해주세요." (Figma 152:909)
@Composable
private fun DrivingCard(
    drivingMinutes: Int,
    onRestBannerClick: () -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    val overdue = drivingMinutes >= REST_MINUTES
    // 2시간이 넘으면 카드를 눌러 쉬어 갈 곳 보기 (S2-08)
    val cardModifier = if (overdue) modifier.clip(RoundedCornerShape(24.dp)).clickable(onClick = onRestBannerClick) else modifier

    GrayCard(modifier = cardModifier, padding = cardPadding(compact)) {
        IconLabel(
            GgIcons.Clock, "연속 주행 시간",
            iconSize = if (compact) 26.dp else 24.dp,
            fontSize = 22.sp,
        )
        Spacer(Modifier.height(if (compact) 16.dp else 12.dp))
        BigValue(TextUtils.drivingTime(drivingMinutes), compact, if (overdue) GgRed else GgTextPrimary)

        Spacer(Modifier.weight(1f).heightIn(min = if (compact) 16.dp else 12.dp))

        ProgressBar(
            fraction = drivingMinutes / REST_MINUTES.toFloat(),
            color = if (overdue) GgRed else GgBlue,
            height = if (compact) 16.dp else 12.dp,
        )
        Spacer(Modifier.height(if (compact) 16.dp else 24.dp))

        if (overdue) {
            Text(
                text = "주행 2 시간이 지났습니다.\n휴식을 취해주세요.",
                color = GgTextPrimary,
                fontSize = 20.sp,
                lineHeight = 26.sp,
            )
        } else {
            Text(
                text = "2시간이 되면 쉬어 갈 곳을 알려 드려요",
                color = if (compact) GgTextSecondary else GgTextCaption,
                fontSize = if (compact) 18.sp else 19.sp,
            )
        }
    }
}

@Composable
private fun BigValue(text: String, compact: Boolean, color: Color) {
    Text(
        text = text,
        color = color,
        fontSize = if (compact) 52.sp else 72.sp,
        lineHeight = if (compact) 62.sp else 83.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
    )
}

private fun cardPadding(compact: Boolean): PaddingValues =
    if (compact) PaddingValues(horizontal = 32.dp, vertical = 28.dp)
    else PaddingValues(start = 38.dp, end = 38.dp, top = 36.dp, bottom = 34.dp)

// ───────────────────────── 마이크 카드 (S2-09, S2-16, S2-17) ─────────────────────────
// 가로: [마이크 158dp] [제목 34sp / 예시 칩] 왼쪽 정렬, 세로 가운데
// 세로: 마이크 136dp(아이콘 64) / 제목 34sp / 칩 20sp 가운데 정렬
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MicCard(
    dogName: String,
    onMicClick: () -> Unit,
    onExampleClick: (String) -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    // S2-17: 탭하면 그 문장으로 바로 요청
    val examples = if (compact) {
        listOf("근처 카페 찾아줘", "가는 길에 휴게소", "공원 가자", "${TextUtils.withIga(dogName)} 아파")
    } else {
        listOf("근처 카페 찾아줘", "근처에 휴게소 있어?", "근처 공원 알려줘", "${TextUtils.withIga(dogName)} 아파")
    }

    if (compact) {
        GrayCard(modifier = modifier, padding = PaddingValues(32.dp)) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(22.dp, Alignment.CenterVertically),
            ) {
                MicButton(size = 136.dp, iconSize = 64.dp, onClick = onMicClick)
                TitleText("${TextUtils.withRang(dogName)} 갈 곳 물어보기", fontSize = 34.sp)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    examples.forEach { text ->
                        Chip("\"$text\"", fontSize = 20.sp, textColor = GgTextPrimary, onClick = { onExampleClick(text) })
                    }
                }
            }
        }
    } else {
        GrayCard(modifier = modifier, padding = PaddingValues(start = 38.dp, end = 38.dp)) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(38.dp),
            ) {
                MicButton(size = 158.dp, onClick = onMicClick)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    TitleText("${TextUtils.withWa(dogName)} 방문할 장소 찾기", fontSize = 34.sp)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(19.dp),
                        verticalArrangement = Arrangement.spacedBy(17.dp),
                    ) {
                        examples.forEach { text ->
                            Chip("\"$text\"", onClick = { onExampleClick(text) })
                        }
                    }
                }
            }
        }
    }
}

// ───────────────────────── 미리보기 ─────────────────────────

@Preview(name = "S2 가로", widthDp = 1700, heightDp = 1184)
@Composable
private fun MainScreenPreview() {
    GatigagaeTheme {
        MainScreen(FakeData.profile, true, Gear.P, FakeData.INDOOR_TEMP, 84, {}, {}, {}, {})
    }
}

@Preview(name = "S2 세로 (분할)", widthDp = 840, heightDp = 1184)
@Composable
private fun MainScreenPortraitPreview() {
    GatigagaeTheme {
        MainScreen(FakeData.profile, true, Gear.D, FakeData.INDOOR_TEMP, 84, {}, {}, {}, {})
    }
}

@Preview(name = "S2 세로 2시간 경과", widthDp = 840, heightDp = 1184)
@Composable
private fun MainScreenOverduePreview() {
    GatigagaeTheme {
        MainScreen(FakeData.profile, true, Gear.D, FakeData.INDOOR_TEMP, 120, {}, {}, {}, {})
    }
}
