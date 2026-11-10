package com.gatigagae.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gatigagae.app.data.DogProfile
import com.gatigagae.app.data.FakeData
import com.gatigagae.app.data.Gear
import com.gatigagae.app.data.TextUtils
import com.gatigagae.app.data.VoiceRequestParser
import com.gatigagae.app.sdk.VoiceController
import com.gatigagae.app.ui.components.DogPhoto
import com.gatigagae.app.ui.components.GgIcons
import com.gatigagae.app.ui.components.GrayCard
import com.gatigagae.app.ui.components.PrimaryButton
import com.gatigagae.app.ui.components.SecondaryButton
import com.gatigagae.app.ui.theme.GatigagaeTheme
import com.gatigagae.app.ui.theme.GgBackground
import com.gatigagae.app.ui.theme.GgBlue
import com.gatigagae.app.ui.theme.GgTextPrimary
import com.gatigagae.app.ui.theme.GgTextSecondary
import kotlinx.coroutines.delay

/** S1-07: 이 시간 동안 대답이 없으면 팝업을 닫아요 (팀 결정 전 임시 15초) */
private const val NO_RESPONSE_TIMEOUT_MS = 15_000L

/**
 * S1 탑승 확인 팝업 (Figma 18:61)
 * 흰 화면 가운데 회색 카드 하나: 사진 → "콩이랑 같이 타나요?" → 마이크 안내 → 버튼 2개
 *
 * Figma 값(× 2/3): 카드 폭 1641.6 → 1094dp, 위 97 / 아래 101 → 65 / 67dp, 줄 간격 61.2 → 41dp,
 *                 사진 230 → 154dp, 질문 86.4 → 58sp, 안내 36 → 24sp, 버튼 187×695 → 125×463dp / 글자 54 → 36sp
 *
 * 음성: 팝업이 뜨면 질문을 소리로 읽고(S1-03) → 바로 듣기 시작해서 "응"/"아니"를 알아들어요(S1-06)
 *       못 알아들으면 한 번 더 들어요. 버튼을 눌러도 돼요.
 */
@Composable
fun BoardingScreen(
    profile: DogProfile?,
    gear: Gear,
    onYes: () -> Unit,
    onNo: () -> Unit,
    onTimeout: () -> Unit,
    voice: VoiceController? = null,
) {
    val currentOnTimeout by rememberUpdatedState(onTimeout)
    val currentOnYes by rememberUpdatedState(onYes)
    val currentOnNo by rememberUpdatedState(onNo)

    // S1-03 질문 읽기 → S1-06 대답 듣기
    DisposableEffect(voice) {
        var active = true // 화면을 떠나면 false → 늦게 온 음성 결과는 무시
        if (voice != null) {
            var tries = 0
            fun listenAnswer() {
                if (!active) return
                tries += 1
                voice.listen(
                    onResult = { text ->
                        if (active) when {
                            VoiceRequestParser.isDecline(text) -> currentOnNo()
                            VoiceRequestParser.isAccept(text) -> currentOnYes()
                            tries < 2 -> listenAnswer() // "응/아니"가 아니면 한 번 더
                        }
                    },
                    onFail = { if (active && tries < 2) listenAnswer() },
                )
            }
            val question = "${TextUtils.withRang(profile?.name ?: "")} 같이 타나요?"
            voice.speak(question) { listenAnswer() }
        }
        onDispose {
            active = false
            voice?.stopListening()
        }
    }

    // S1-07: 15초 동안 대답이 없으면 닫기
    LaunchedEffect(Unit) {
        delay(NO_RESPONSE_TIMEOUT_MS)
        currentOnTimeout()
    }

    // S1-08: 대답 전에 D단으로 바뀌면 닫기 (주행 중 전체 화면 팝업 금지)
    LaunchedEffect(gear) {
        if (gear == Gear.D) currentOnTimeout()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(GgBackground)
            .padding(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        // 세로(분할) 화면처럼 좁으면 버튼을 위아래로 쌓아요
        val narrow = maxWidth < 1000.dp
        val dogName = profile?.name ?: ""

        GrayCard(
            modifier = Modifier
                .widthIn(max = 1094.dp)
                .fillMaxWidth(),
            padding = PaddingValues(
                start = if (narrow) 40.dp else 68.dp,
                end = if (narrow) 40.dp else 68.dp,
                top = 65.dp,
                bottom = 67.dp,
            ),
            cornerRadius = 29.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(41.dp),
            ) {
                DogPhoto(
                    size = 154.dp,
                    label = if (dogName.isBlank()) "사진" else "$dogName 사진",
                    fontSize = 16.sp,
                )

                // S1-03: 이름이 없으면 "반려견이랑 같이 타나요?"
                Text(
                    text = "${TextUtils.withRang(dogName)} 같이 타나요?",
                    color = GgTextPrimary,
                    fontSize = 58.sp,
                    lineHeight = 72.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                // S1-06: 마이크 아이콘 + 안내
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(GgIcons.Mic, contentDescription = null, tint = GgBlue, modifier = Modifier.size(26.dp))
                    Text("\"응\" 또는 \"아니\"라고 말해 주세요", color = GgTextSecondary, fontSize = 24.sp)
                }

                if (narrow) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        YesButton(onYes, Modifier.fillMaxWidth())
                        NoButton(onNo, Modifier.fillMaxWidth())
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(31.dp),
                    ) {
                        YesButton(onYes, Modifier.weight(1f))
                        NoButton(onNo, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// S1-04
@Composable
private fun YesButton(onClick: () -> Unit, modifier: Modifier) {
    PrimaryButton("네, 함께 탑승합니다", onClick = onClick, height = 125.dp, fontSize = 36.sp, modifier = modifier)
}

// S1-05
@Composable
private fun NoButton(onClick: () -> Unit, modifier: Modifier) {
    SecondaryButton("아니요, 혼자 탑승합니다", onClick = onClick, height = 125.dp, fontSize = 36.sp, modifier = modifier)
}

@Preview(widthDp = 1700, heightDp = 1184)
@Composable
private fun BoardingScreenPreview() {
    GatigagaeTheme {
        BoardingScreen(profile = FakeData.profile, gear = Gear.P, onYes = {}, onNo = {}, onTimeout = {})
    }
}

@Preview(name = "S1 세로", widthDp = 840, heightDp = 1184)
@Composable
private fun BoardingScreenPortraitPreview() {
    GatigagaeTheme {
        BoardingScreen(profile = FakeData.profile, gear = Gear.P, onYes = {}, onNo = {}, onTimeout = {})
    }
}
