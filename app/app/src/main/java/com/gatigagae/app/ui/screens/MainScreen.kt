package com.gatigagae.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gatigagae.app.data.DogProfile
import com.gatigagae.app.data.FakeData
import com.gatigagae.app.data.Gear
import com.gatigagae.app.data.TextUtils
import com.gatigagae.app.ui.components.DogPhoto
import com.gatigagae.app.ui.components.GrayCard
import com.gatigagae.app.ui.components.MicButton
import com.gatigagae.app.ui.components.Pill
import com.gatigagae.app.ui.components.PrimaryButton
import com.gatigagae.app.ui.components.ProgressBar
import com.gatigagae.app.ui.components.SecondaryButton
import com.gatigagae.app.ui.components.SubText
import com.gatigagae.app.ui.components.TitleText
import com.gatigagae.app.ui.components.TopBar
import com.gatigagae.app.ui.theme.GatigagaeTheme
import com.gatigagae.app.ui.theme.GgBackground
import com.gatigagae.app.ui.theme.GgBlue
import com.gatigagae.app.ui.theme.GgBlueLight
import com.gatigagae.app.ui.theme.GgBorder
import com.gatigagae.app.ui.theme.GgTextPrimary
import com.gatigagae.app.ui.theme.GgTextSecondary

/** 휴식 권고 기준 (2시간 = 120분) */
const val REST_MINUTES = 120

/**
 * S2 메인 화면 (와이어프레임 v1 기준)
 * ┌───────────┬──────────────┬──────────────┐
 * │           │  실내 온도    │ 연속 주행 시간 │
 * │ 프로필 카드 ├──────────────┴──────────────┤
 * │           │        마이크 카드            │
 * └───────────┴─────────────────────────────┘
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GgBackground)
    ) {
        TopBar(gear = gear, dogMode = dogMode)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(start = 32.dp, end = 32.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // 왼쪽: 프로필 카드
            ProfileCard(
                profile = profile,
                gear = gear,
                onEditProfile = onEditProfile,
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight(),
            )

            // 오른쪽
            Column(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.45f),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    TemperatureCard(
                        dogName = dogName,
                        indoorTemp = indoorTemp,
                        preferredTemp = profile?.preferredTemp ?: 23,
                        dogMode = dogMode,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                    DrivingCard(
                        drivingMinutes = drivingMinutes,
                        onRestBannerClick = onRestBannerClick,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
                MicCard(
                    dogName = dogName,
                    onMicClick = onMicClick,
                    onExampleClick = onExampleClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.55f),
                )
            }
        }
    }
}

// ───────────────────────── 프로필 카드 (S2-02, S2-03, S2-13) ─────────────────────────
@Composable
private fun ProfileCard(
    profile: DogProfile?,
    gear: Gear,
    onEditProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canEdit = gear == Gear.P // 정차 중에만 수정 가능

    GrayCard(modifier = modifier) {
        if (profile == null) {
            // S2-03: 프로필이 없을 때
            Spacer(Modifier.weight(1f))
            TitleText("프로필이 없어요", fontSize = 24.sp)
            Spacer(Modifier.height(8.dp))
            SubText("반려견 이름을 등록하면 안내에 이름을 불러 드려요")
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                text = if (canEdit) "프로필 등록" else "정차 후 등록할 수 있어요",
                onClick = onEditProfile,
                enabled = canEdit,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.weight(1f))
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DogPhoto(size = 96.dp)
                Spacer(Modifier.width(20.dp))
                TitleText(profile.name, fontSize = 34.sp)
            }
            Spacer(Modifier.height(28.dp))
            LabelValue("견종", profile.breed)
            LabelValue("나이", profile.age)
            LabelValue("특이사항", profile.note)

            Spacer(Modifier.weight(1f))

            // S2-13: 정차 중에만 누를 수 있음
            SecondaryButton(
                text = if (canEdit) "프로필 수정" else "프로필 수정 (정차 중에만)",
                onClick = onEditProfile,
                enabled = canEdit,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun LabelValue(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(label, color = GgTextSecondary, fontSize = 18.sp, modifier = Modifier.width(96.dp))
        Text(value.ifBlank { "-" }, color = GgTextPrimary, fontSize = 18.sp)
    }
}

// ───────────────────────── 실내 온도 카드 (S2-04, S2-05, S2-06) ─────────────────────────
@Composable
private fun TemperatureCard(
    dogName: String,
    indoorTemp: Double,
    preferredTemp: Int,
    dogMode: Boolean,
    modifier: Modifier = Modifier,
) {
    GrayCard(modifier = modifier) {
        SubText("🌡 실내 온도", fontSize = 18.sp) // 아이콘은 디자인 나오면 교체
        Spacer(Modifier.height(8.dp))
        // TODO(SDK): getHvacTemperatureCurrent 값으로 교체
        Text(TextUtils.temperature(indoorTemp), color = GgTextPrimary, fontSize = 56.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.weight(1f))

        // 흰 박스: "콩이 설정 온도 23℃"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${dogName.ifBlank { "반려견" }} 설정 온도",
                color = GgTextPrimary,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f),
            )
            Text("${preferredTemp}℃", color = GgBlue, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
        SubText(
            text = if (dogMode) "공조 켜짐 · 설정 온도로 맞추는 중" else "반려견 모드가 꺼져 있어요",
            fontSize = 15.sp,
        )
    }
}

// ───────────────────────── 연속 주행 시간 카드 (S2-07, S2-08, S2-14, S2-15) ─────────────────────────
@Composable
private fun DrivingCard(
    drivingMinutes: Int,
    onRestBannerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GrayCard(modifier = modifier) {
        SubText("🕐 연속 주행 시간", fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        Text(TextUtils.drivingTime(drivingMinutes), color = GgTextPrimary, fontSize = 56.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.weight(1f))

        ProgressBar(fraction = drivingMinutes / REST_MINUTES.toFloat())
        Spacer(Modifier.height(12.dp))

        if (drivingMinutes >= REST_MINUTES) {
            // S2-08: 2시간 넘으면 휴식 권고 배너
            Pill(
                text = "2시간 넘게 달렸어요 · 쉬어 갈 곳 보기 ›",
                textColor = GgBlue,
                background = GgBlueLight,
                borderColor = GgBlue,
                onClick = onRestBannerClick,
            )
        } else {
            SubText("2시간이 되면 쉬어 갈 곳을 알려 드려요", fontSize = 15.sp)
        }
    }
}

// ───────────────────────── 마이크 카드 (S2-09, S2-16, S2-17) ─────────────────────────
@Composable
private fun MicCard(
    dogName: String,
    onMicClick: () -> Unit,
    onExampleClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val examples = listOf(
        "근처 카페 찾아줘",
        "가는 길에 휴게소",
        "공원 가자",
        "${TextUtils.withIga(dogName)} 아파",
    )

    GrayCard(modifier = modifier) {
        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            MicButton(size = 130.dp, onClick = onMicClick)
            Spacer(Modifier.width(36.dp))
            Column {
                TitleText("${TextUtils.withRang(dogName)} 갈 곳 물어보기", fontSize = 30.sp)
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExampleChip(examples[0], onExampleClick)
                    ExampleChip(examples[1], onExampleClick)
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExampleChip(examples[2], onExampleClick)
                    ExampleChip(examples[3], onExampleClick)
                }
            }
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun ExampleChip(text: String, onClick: (String) -> Unit) {
    Pill(
        text = "\"$text\"",
        textColor = GgTextPrimary,
        background = Color.White,
        borderColor = GgBorder,
        fontSize = 17.sp,
        onClick = { onClick(text) }, // S2-17: 탭하면 그 문장으로 바로 요청
    )
}

@Preview(widthDp = 1700, heightDp = 1184)
@Composable
private fun MainScreenPreview() {
    GatigagaeTheme {
        MainScreen(
            profile = FakeData.profile,
            dogMode = true,
            gear = Gear.P,
            indoorTemp = FakeData.INDOOR_TEMP,
            drivingMinutes = 84,
            onEditProfile = {},
            onMicClick = {},
            onExampleClick = {},
            onRestBannerClick = {},
        )
    }
}
