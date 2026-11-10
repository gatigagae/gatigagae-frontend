package com.gatigagae.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gatigagae.app.data.FakeData
import com.gatigagae.app.data.VoiceRequest
import com.gatigagae.app.data.VoiceRequestParser
import com.gatigagae.app.sdk.VoiceController
import com.gatigagae.app.ui.components.Chip
import com.gatigagae.app.ui.components.MicButton
import com.gatigagae.app.ui.components.Pill
import com.gatigagae.app.ui.components.PrimaryButton
import com.gatigagae.app.ui.components.SecondaryButton
import com.gatigagae.app.ui.theme.GatigagaeTheme
import com.gatigagae.app.ui.theme.GgBlue
import com.gatigagae.app.ui.theme.GgBlueLight
import com.gatigagae.app.ui.theme.GgRed
import com.gatigagae.app.ui.theme.GgScrim
import com.gatigagae.app.ui.theme.GgTextPrimary
import com.gatigagae.app.ui.theme.GgTextSecondary
import kotlinx.coroutines.delay

/** 음성 시트의 단계 */
private enum class VoiceStep {
    LISTENING,    // 듣는 중
    RECOGNIZED,   // 알아들음 (문장 + 칩 표시)
    ASK_CATEGORY, // 종류를 못 알아들어서 되묻는 중
    SEARCHING,    // 찾는 중 → 끝나면 S3로
    FAILED,       // 인식 실패 (Figma 200:1709: 빨간 마이크 + "인식하지 못했습니다" + 빨간 재탐색)
}

/**
 * S2-1 음성으로 물어보기 (바텀시트, Figma 18:175 / 실패 200:1709)
 * 주행 중 전체 화면 팝업 금지 규칙 때문에 아래에서 올라오는 시트 모양이에요 (S2-1-01).
 *
 * Figma 값(× 2/3): 위 모서리 72 → 48dp, 위 여백 43.2 → 29dp, 줄 간격 50.4 → 34dp,
 *                 후광 273.6 → 182dp / 마이크 216 → 144dp, 문장 72 → 48sp,
 *                 버튼 높이 140.4 → 94dp, 폭 417.6 : 817.2 (취소 : 진행), 버튼 사이 43.2 → 29dp
 *
 * presetText: 예시 칩을 눌러서 열었으면 그 문장, 마이크 버튼으로 열었으면 null
 * simulateFail: true면 듣기 후 "인식하지 못했습니다" 화면 (개발용 패널에서 확인용)
 *
 * voice: 있으면 진짜 음성 인식(STT), 없으면(미리보기) 2초 뒤 가짜 문장을 "들은 척"
 * onChoice: S3에서 열었을 때만. "첫 번째로 가줘" → onChoice(0)
 */
@Composable
fun VoiceSheet(
    dogName: String,
    presetText: String?,
    onCancel: () -> Unit,
    onSearch: (VoiceRequest) -> Unit,
    simulateFail: Boolean = false,
    voice: VoiceController? = null,
    onChoice: ((Int) -> Unit)? = null,
) {
    var step by remember {
        mutableStateOf(if (presetText != null) VoiceStep.RECOGNIZED else VoiceStep.LISTENING)
    }
    var request by remember {
        mutableStateOf(presetText?.let { VoiceRequestParser.parse(it) })
    }
    var failOnce by remember { mutableStateOf(simulateFail) }
    var heard by remember { mutableStateOf("") } // 말하는 중간 글자
    val currentOnSearch by rememberUpdatedState(onSearch)
    val currentOnChoice by rememberUpdatedState(onChoice)

    // 시트가 닫히면 마이크 끄기
    DisposableEffect(voice) {
        onDispose { voice?.stopListening() }
    }

    // 단계가 바뀔 때마다 다음 단계로 넘어가는 흐름
    LaunchedEffect(step) {
        when (step) {
            VoiceStep.LISTENING -> {
                heard = ""
                if (failOnce) {
                    // 개발용: 인식 실패 화면 보기
                    delay(2000)
                    failOnce = false // 재탐색하면 그다음엔 진짜로 들어요
                    step = VoiceStep.FAILED
                } else if (voice != null) {
                    // 진짜 음성 인식
                    voice.listen(
                        onPartial = { text -> heard = text },
                        onResult = { text ->
                            request = VoiceRequestParser.parse(text)
                            step = VoiceStep.RECOGNIZED
                        },
                        onFail = { step = VoiceStep.FAILED },
                    )
                } else {
                    // 미리보기용 가짜
                    delay(2000)
                    request = VoiceRequestParser.parse(FakeData.spokenText(dogName))
                    step = VoiceStep.RECOGNIZED
                }
            }
            VoiceStep.RECOGNIZED -> {
                delay(1200)
                val current = request
                val choice = current?.let { VoiceRequestParser.parseChoice(it.text) }
                val chooser = currentOnChoice
                if (choice != null && chooser != null) {
                    chooser(choice) // S3: "첫 번째로 가줘"
                } else {
                    step = if (current?.category == null) VoiceStep.ASK_CATEGORY else VoiceStep.SEARCHING
                }
            }
            VoiceStep.SEARCHING -> {
                delay(1000)
                val current = request
                if (current != null) currentOnSearch(current)
            }
            VoiceStep.ASK_CATEGORY, VoiceStep.FAILED -> Unit // 사용자가 고를 때까지 기다림
        }
    }

    val failed = step == VoiceStep.FAILED

    // 어두운 배경 + 아래쪽 흰 시트
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GgScrim),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp))
                .background(Color.White)
                .padding(start = 40.dp, end = 40.dp, top = 29.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(34.dp),
        ) {
            // S2-1-02: 마이크 + 상태 글자 (Figma "Listening" 묶음, 사이 18 → 12dp)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MicButton(
                    size = 144.dp,
                    iconSize = 58.dp,
                    halo = true,
                    color = if (failed) GgRed else GgBlue,
                    onClick = { if (failed) step = VoiceStep.LISTENING },
                )
                Text(
                    text = when (step) {
                        VoiceStep.LISTENING -> "듣고 있어요"
                        VoiceStep.ASK_CATEGORY -> "어디로 갈지 알려 주세요"
                        VoiceStep.FAILED -> "다시 말해 주세요"
                        else -> "알아들었어요"
                    },
                    color = GgTextSecondary,
                    fontSize = 22.sp,
                )
            }

            // S2-1-03: 인식된 문장 / 실패 문구
            val current = request
            Text(
                text = when {
                    failed -> "인식하지 못했습니다"
                    step == VoiceStep.LISTENING && heard.isNotBlank() -> "\"$heard\""
                    current == null || step == VoiceStep.LISTENING -> "…"
                    else -> "\"${current.text}\""
                },
                color = GgTextPrimary,
                fontSize = 48.sp,
                lineHeight = 60.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            // S2-1-04: 해석 결과 칩 (Figma Parsed Chips, 사이 25.2 → 17dp)
            if (current != null && !failed && step != VoiceStep.LISTENING) {
                Row(horizontalArrangement = Arrangement.spacedBy(17.dp)) {
                    Chip("종류  ${current.category ?: "?"}")
                    Chip("범위  ${current.range ?: "-"}")
                    Chip("의도  ${current.intent ?: "-"}")
                }
            }

            // 종류를 못 알아들었을 때 되묻기 (기획서: "카페, 식당, 공원 중에 어디로 갈까요?")
            if (step == VoiceStep.ASK_CATEGORY && current != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(17.dp),
                ) {
                    Text("카페, 식당, 공원 중에 어디로 갈까요?", color = GgTextSecondary, fontSize = 24.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(17.dp)) {
                        listOf("카페", "식당", "공원").forEach { choice ->
                            Pill(
                                text = choice,
                                textColor = GgBlue,
                                background = GgBlueLight,
                                borderColor = GgBlue,
                                fontSize = 26.sp,
                                horizontalPadding = 28.dp,
                                verticalPadding = 12.dp,
                                onClick = {
                                    request = current.copy(category = choice)
                                    step = VoiceStep.SEARCHING
                                },
                            )
                        }
                    }
                }
            }

            // 하단 버튼 (S2-1-06, S2-1-07). Figma Sheet Buttons: 위 여백 50.4 → 34dp 추가
            Spacer(Modifier.height(0.dp))
            Row(
                modifier = Modifier
                    .widthIn(max = 852.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(29.dp),
            ) {
                SecondaryButton("취소", onClick = onCancel, modifier = Modifier.weight(278f))
                if (failed) {
                    // Figma: 빨간 "재탐색" → 다시 듣기
                    PrimaryButton(
                        text = "재탐색",
                        onClick = { step = VoiceStep.LISTENING },
                        color = GgRed,
                        modifier = Modifier.weight(545f),
                    )
                } else {
                    PrimaryButton(
                        text = when (step) {
                            VoiceStep.LISTENING -> "듣는 중…"
                            VoiceStep.ASK_CATEGORY -> "종류를 골라 주세요"
                            else -> "찾는 중… (결과로 이동)"
                        },
                        onClick = {}, // 버튼 모양이지만 진행 상태 표시 역할 (S2-1-07)
                        modifier = Modifier.weight(545f),
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 1700, heightDp = 1184)
@Composable
private fun VoiceSheetPreview() {
    GatigagaeTheme {
        VoiceSheet(dogName = "콩이", presetText = "근처에 콩이랑 갈 수 있는 카페 찾아줘", onCancel = {}, onSearch = {})
    }
}
