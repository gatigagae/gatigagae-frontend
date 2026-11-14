package com.gatigagae.app.ui.screens

import com.gatigagae.app.R
import com.gatigagae.app.ui.components.GgDialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gatigagae.app.data.DogProfile
import com.gatigagae.app.data.FakeData
import com.gatigagae.app.data.Gear
import com.gatigagae.app.data.TextUtils
import com.gatigagae.app.data.VoiceRequestParser
import com.gatigagae.app.sdk.VoiceController
import com.gatigagae.app.ui.components.AppIcon
import com.gatigagae.app.ui.components.BasicButton
import com.gatigagae.app.ui.components.GoldButton
import com.gatigagae.app.ui.components.popupShadow
import com.gatigagae.app.ui.theme.BgPrimary
import com.gatigagae.app.ui.theme.Divider10
import com.gatigagae.app.ui.theme.GatigagaeTheme
import com.gatigagae.app.ui.theme.Surface
import com.gatigagae.app.ui.theme.TextPrimary
import com.gatigagae.app.ui.theme.TextTertiary
import kotlinx.coroutines.delay

/** S1-07: 이 시간 동안 대답이 없으면 알림을 닫아요 (팀 결정 전 임시 15초) */
private const val NO_RESPONSE_TIMEOUT_MS = 15_000L

/**
 * S1 탑승 확인 (Figma 344:14113 "S1 - L": 화면 위쪽 시스템 알림 카드 ivi_notification_system)
 * 폭 700 · 모서리 48 · [앱 아이콘 100] 제목 36 / 설명 26 · [네, 함께 타요][아니요]
 *
 * 메인 화면 위에 겹쳐서 띄워요 (GatigagaeApp에서 MainScreen 위에 그림).
 * 음성: 질문을 소리로 읽고(S1-03) → 바로 듣기 시작해서 "응"/"아니"를 알아들어요(S1-06). 버튼을 눌러도 돼요.
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
    // 기획 결정(11/14): 프로필 없으면 "반려견과 함께 탑승하나요?", 있으면 "콩이와 함께 탑승하나요?" (받침 있으면 '과')
    // 화면 제목과 음성이 같은 문장이에요.
    val title = profile?.name?.takeIf { it.isNotBlank() }?.let { "${TextUtils.withWa(it)} 함께 탑승하나요?" } ?: "반려견과 함께 탑승하나요?"

    // S1-03 질문 읽기 → S1-06 대답 듣기
    DisposableEffect(voice) {
        var active = true // 알림이 닫히면 false → 늦게 온 음성 결과는 무시
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
            voice.speak(title) { listenAnswer() }
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

    // S1-08: 대답 전에 D단으로 바뀌면 닫기
    LaunchedEffect(gear) {
        if (gear == Gear.D) currentOnTimeout()
    }

    // Figma S1 - L 344:13943: 앱 화면 위 가운데 시스템 팝업 (발바닥 아이콘 · 제목 · [네, 함께 타요][아니요])
    // (다른 앱을 보고 있을 때의 위쪽 알림 카드 344:14113은 앱이 켜진 상태에선 쓰지 않아요)
    GgDialog(
        title = title,
        iconRes = R.drawable.ic_paw,
        confirmText = "네, 함께 타요",   // S1-04
        onConfirm = onYes,
        dismissText = "아니요",          // S1-05
        onDismiss = onNo,
    )
}

@Preview(widthDp = 1700, heightDp = 1184)
@Composable
private fun BoardingScreenPreview() {
    GatigagaeTheme {
        Box(Modifier.fillMaxSize().background(BgPrimary)) {
            BoardingScreen(profile = FakeData.profile, gear = Gear.P, onYes = {}, onNo = {}, onTimeout = {})
        }
    }
}
