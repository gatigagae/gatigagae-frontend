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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.gatigagae.app.ui.components.MicButton
import com.gatigagae.app.ui.components.Pill
import com.gatigagae.app.ui.components.PrimaryButton
import com.gatigagae.app.ui.components.SecondaryButton
import com.gatigagae.app.ui.components.SubText
import com.gatigagae.app.ui.theme.GatigagaeTheme
import com.gatigagae.app.ui.theme.GgBlue
import com.gatigagae.app.ui.theme.GgBlueLight
import com.gatigagae.app.ui.theme.GgScrim
import com.gatigagae.app.ui.theme.GgTextPrimary
import kotlinx.coroutines.delay

/** 음성 시트의 단계 */
private enum class VoiceStep {
    LISTENING,    // 듣는 중
    RECOGNIZED,   // 알아들음 (문장 + 칩 표시)
    ASK_CATEGORY, // 종류를 못 알아들어서 되묻는 중
    SEARCHING,    // 찾는 중 → 끝나면 S3로
}

/**
 * S2-1 음성으로 물어보기 (바텀시트)
 * 주행 중 전체 화면 팝업 금지 규칙 때문에 아래에서 올라오는 시트 모양이에요 (S2-1-01).
 *
 * presetText: 예시 칩을 눌러서 열었으면 그 문장, 마이크 버튼으로 열었으면 null
 *
 * TODO(STT): 지금은 2초 뒤 가짜 문장을 "들은 척" 해요. STT SDK가 붙으면 실제 인식 결과로 교체
 */
@Composable
fun VoiceSheet(
    dogName: String,
    presetText: String?,
    onCancel: () -> Unit,
    onSearch: (VoiceRequest) -> Unit,
) {
    var step by remember {
        mutableStateOf(if (presetText != null) VoiceStep.RECOGNIZED else VoiceStep.LISTENING)
    }
    var request by remember {
        mutableStateOf(presetText?.let { VoiceRequestParser.parse(it) })
    }
    val currentOnSearch by rememberUpdatedState(onSearch)

    // 단계가 바뀔 때마다 다음 단계로 넘어가는 흐름
    LaunchedEffect(step) {
        when (step) {
            VoiceStep.LISTENING -> {
                delay(2000)
                request = VoiceRequestParser.parse(FakeData.spokenText(dogName)) // ← STT 결과로 교체할 곳
                step = VoiceStep.RECOGNIZED
            }
            VoiceStep.RECOGNIZED -> {
                delay(1200)
                val current = request
                step = if (current?.category == null) VoiceStep.ASK_CATEGORY else VoiceStep.SEARCHING
            }
            VoiceStep.SEARCHING -> {
                delay(1000)
                val current = request
                if (current != null) currentOnSearch(current)
            }
            VoiceStep.ASK_CATEGORY -> Unit // 사용자가 칩을 고를 때까지 기다림
        }
    }

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
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.White)
                .padding(horizontal = 48.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // S2-1-02: 듣는 중 표시
            MicButton(size = 96.dp, onClick = {}, halo = step == VoiceStep.LISTENING)
            Spacer(Modifier.height(10.dp))
            SubText(
                text = when (step) {
                    VoiceStep.LISTENING -> "듣고 있어요"
                    VoiceStep.ASK_CATEGORY -> "어디로 갈지 알려 주세요"
                    else -> "알아들었어요"
                },
            )
            Spacer(Modifier.height(16.dp))

            // S2-1-03: 인식된 문장
            val current = request
            Text(
                text = if (current == null) "…" else "\"${current.text}\"",
                color = GgTextPrimary,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(18.dp))

            // S2-1-04: 해석 결과 칩
            if (current != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ResultChip("종류", current.category ?: "?")
                    ResultChip("범위", current.range ?: "-")
                    ResultChip("의도", current.intent ?: "-")
                }
                Spacer(Modifier.height(18.dp))
            }

            // 종류를 못 알아들었을 때 되묻기 (기획서: "카페, 식당, 공원 중에 어디로 갈까요?")
            if (step == VoiceStep.ASK_CATEGORY && current != null) {
                SubText("카페, 식당, 공원 중에 어디로 갈까요?", fontSize = 18.sp)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("카페", "식당", "공원").forEach { choice ->
                        Pill(
                            text = choice,
                            textColor = GgBlue,
                            background = GgBlueLight,
                            borderColor = GgBlue,
                            fontSize = 20.sp,
                            onClick = {
                                request = current.copy(category = choice)
                                step = VoiceStep.SEARCHING
                            },
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
            }

            // 하단 버튼 (S2-1-06, S2-1-07)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SecondaryButton("취소", onClick = onCancel, modifier = Modifier.weight(1f))
                PrimaryButton(
                    text = when (step) {
                        VoiceStep.LISTENING -> "듣는 중…"
                        VoiceStep.ASK_CATEGORY -> "종류를 골라 주세요"
                        else -> "찾는 중… (결과로 이동)"
                    },
                    onClick = {}, // 버튼 모양이지만 진행 상태 표시 역할 (S2-1-07)
                    modifier = Modifier.weight(2f),
                )
            }
        }
    }
}

@Composable
private fun ResultChip(label: String, value: String) {
    Pill(
        text = "$label  $value",
        textColor = GgTextPrimary,
        background = Color(0xFFF1F3F5),
        fontSize = 16.sp,
    )
}

@Preview(widthDp = 1700, heightDp = 1184)
@Composable
private fun VoiceSheetPreview() {
    GatigagaeTheme {
        VoiceSheet(dogName = "콩이", presetText = "근처에 콩이랑 갈 수 있는 카페 찾아줘", onCancel = {}, onSearch = {})
    }
}
