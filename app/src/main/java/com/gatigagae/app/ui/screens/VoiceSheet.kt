package com.gatigagae.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gatigagae.app.data.FakeData
import com.gatigagae.app.data.VoiceRequest
import com.gatigagae.app.data.VoiceRequestParser
import com.gatigagae.app.sdk.VoiceController
import com.gatigagae.app.ui.components.BasicButton
import com.gatigagae.app.ui.components.GoldButton
import com.gatigagae.app.ui.components.MicButton
import com.gatigagae.app.ui.components.popupShadow
import com.gatigagae.app.ui.theme.AlertRed
import com.gatigagae.app.ui.theme.ChipGray
import com.gatigagae.app.ui.theme.GatigagaeTheme
import com.gatigagae.app.ui.theme.Gold100
import com.gatigagae.app.ui.theme.Gold500
import com.gatigagae.app.ui.theme.Gold800
import com.gatigagae.app.ui.theme.Gold900
import com.gatigagae.app.ui.theme.Scrim
import com.gatigagae.app.ui.theme.Surface
import com.gatigagae.app.ui.theme.TextPrimary
import com.gatigagae.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

/** 음성 시트의 단계 */
private enum class VoiceStep {
    LISTENING,    // 듣는 중 (Figma "Listening")
    RECOGNIZED,   // 알아들음 → 잠시 뒤 자동으로 찾기
    ASK_CATEGORY, // 종류를 못 알아들어서 되묻는 중
    SEARCHING,    // 찾는 중 (Figma "Searching") → 끝나면 S3로
    FAILED,       // 인식 실패 (Figma "Failed")
}

/**
 * S2-1 음성으로 물어보기 (아래에서 올라오는 시트)
 * Figma: Listening 344:13406 / Failed 344:13499 / Searching 344:16361
 * 흰 바탕 · 위 모서리 48 · 손잡이 · 마이크(후광 2겹) · 상태 글자 28 · 문장 48 굵게 · 칩 · [골드][회색] 버튼
 *
 * voice: 있으면 진짜 음성 인식(STT), 없으면(미리보기) 2초 뒤 가짜 문장
 * onChoice: S3에서 열었을 때만. "첫 번째로 가줘" → onChoice(0)
 * simulateFail: 개발용 패널에서 실패 화면 확인용
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
    var step by remember { mutableStateOf(if (presetText != null) VoiceStep.RECOGNIZED else VoiceStep.LISTENING) }
    var request by remember { mutableStateOf(presetText?.let { VoiceRequestParser.parse(it) }) }
    var failOnce by remember { mutableStateOf(simulateFail) }
    var heard by remember { mutableStateOf("") } // 말하는 중간 글자
    val currentOnSearch by rememberUpdatedState(onSearch)
    val currentOnChoice by rememberUpdatedState(onChoice)

    // 시트가 닫히면 마이크 끄기
    DisposableEffect(voice) {
        onDispose { voice?.stopListening() }
    }

    LaunchedEffect(step) {
        when (step) {
            VoiceStep.LISTENING -> {
                heard = ""
                if (failOnce) {
                    delay(2000)
                    failOnce = false
                    step = VoiceStep.FAILED
                } else if (voice != null) {
                    voice.listen(
                        onPartial = { text -> heard = text },
                        onResult = { text ->
                            request = VoiceRequestParser.parse(text)
                            step = VoiceStep.RECOGNIZED
                        },
                        onFail = { step = VoiceStep.FAILED },
                    )
                } else {
                    delay(2000)
                    request = VoiceRequestParser.parse(FakeData.spokenText(dogName))
                    step = VoiceStep.RECOGNIZED
                }
            }
            VoiceStep.RECOGNIZED -> {
                delay(1500) // 해석 칩을 잠깐 보여주고 자동으로 찾기 ([이대로 찾기]로 바로 넘어가도 돼요)
                val current = request
                val choice = current?.let { VoiceRequestParser.parseChoice(it.text) }
                val chooser = currentOnChoice
                if (choice != null && chooser != null) {
                    chooser(choice)
                } else {
                    step = if (current?.category == null) VoiceStep.ASK_CATEGORY else VoiceStep.SEARCHING
                }
            }
            VoiceStep.SEARCHING -> {
                delay(800)
                request?.let { currentOnSearch(it) }
            }
            VoiceStep.ASK_CATEGORY, VoiceStep.FAILED -> Unit
        }
    }

    val current = request
    val failed = step == VoiceStep.FAILED

    // 어두운 배경 + 아래 시트
    Box(
        modifier = Modifier.fillMaxSize().background(Scrim),
        contentAlignment = Alignment.BottomCenter,
    ) {
        val shape = RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .popupShadow(48.dp)
                .clip(shape)
                .background(Surface, shape)
                .padding(start = 48.dp, end = 48.dp, top = 24.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 손잡이
            Box(Modifier.size(width = 80.dp, height = 8.dp).clip(RoundedCornerShape(4.dp)).background(ChipGray))
            Spacer(Modifier.height(40.dp))

            // 마이크 + 상태 글자
            MicButton(
                size = 160.dp,
                iconSize = 80.dp,
                halo = true,
                color = if (failed) AlertRed else Gold500,
                onClick = { if (failed) step = VoiceStep.LISTENING },
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = when (step) {
                    VoiceStep.LISTENING -> "듣고 있어요"
                    VoiceStep.RECOGNIZED -> "알아들었어요"
                    VoiceStep.ASK_CATEGORY -> "어디로 갈지 알려 주세요"
                    VoiceStep.SEARCHING -> "찾고 있어요"
                    VoiceStep.FAILED -> "인식하지 못했어요"
                },
                color = if (failed) AlertRed else TextSecondary,
                fontSize = 28.sp,
                lineHeight = 36.sp,
                fontWeight = if (failed) FontWeight.Bold else FontWeight.Normal,
            )
            Spacer(Modifier.height(28.dp))

            // 큰 문장
            Text(
                text = when {
                    failed -> "“근처 카페 찾아줘”처럼 다시 말해 주세요"
                    step == VoiceStep.SEARCHING -> "반려견과 갈 수 있는 ${current?.category ?: "곳"} 찾는 중이에요"
                    step == VoiceStep.LISTENING && heard.isNotBlank() -> "“$heard”"
                    step == VoiceStep.LISTENING || current == null -> "…"
                    else -> "“${current.text}”"
                },
                color = if (failed) TextPrimary else Gold900,
                fontSize = if (failed || step == VoiceStep.SEARCHING) 36.sp else 48.sp,
                lineHeight = if (failed || step == VoiceStep.SEARCHING) 44.sp else 58.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            // 해석 칩 (종류 · 범위)
            if (current != null && (step == VoiceStep.RECOGNIZED || step == VoiceStep.ASK_CATEGORY)) {
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LabeledChip("종류", current.category ?: "?")
                    LabeledChip("범위", current.range ?: "근처")
                }
            }

            // 종류 되묻기
            if (step == VoiceStep.ASK_CATEGORY && current != null) {
                Spacer(Modifier.height(24.dp))
                Text("카페, 식당, 공원 중에 어디로 갈까요?", color = TextSecondary, fontSize = 28.sp)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    listOf("카페", "식당", "공원").forEach { choice ->
                        GoldButton(choice, onClick = {
                            request = current.copy(category = choice)
                            step = VoiceStep.SEARCHING
                        })
                    }
                }
            }

            // 버튼
            Spacer(Modifier.height(48.dp))
            Row(
                modifier = Modifier.widthIn(max = 816.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when {
                    failed -> GoldButton("다시 말하기", onClick = { step = VoiceStep.LISTENING }, modifier = Modifier.weight(1f), large = true)
                    step == VoiceStep.RECOGNIZED && current?.category != null ->
                        GoldButton("이대로 찾기", onClick = { step = VoiceStep.SEARCHING }, modifier = Modifier.weight(1f), large = true)
                    else -> GoldButton(
                        if (step == VoiceStep.SEARCHING) "찾는 중…" else "이대로 찾기",
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.weight(1f),
                        large = true,
                    )
                }
                BasicButton("취소", onClick = onCancel, modifier = Modifier.weight(1f), large = true)
            }
        }
    }
}

/** "종류 카페" 칩: 앞은 회색 글자, 뒤는 골드 굵은 글자 (Figma 해석 칩) */
@Composable
private fun LabeledChip(label: String, value: String) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(50)).background(Gold100).padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(label, color = TextSecondary, fontSize = 26.sp, lineHeight = 34.sp)
        Text(value, color = Gold800, fontSize = 26.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview(widthDp = 1700, heightDp = 1184)
@Composable
private fun VoiceSheetPreview() {
    GatigagaeTheme {
        VoiceSheet(dogName = "콩이", presetText = "근처에 콩이랑 갈 수 있는 카페 찾아줘", onCancel = {}, onSearch = {})
    }
}
