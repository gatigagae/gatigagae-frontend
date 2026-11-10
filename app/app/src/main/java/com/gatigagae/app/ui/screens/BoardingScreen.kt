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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gatigagae.app.data.DogProfile
import com.gatigagae.app.data.FakeData
import com.gatigagae.app.data.Gear
import com.gatigagae.app.data.TextUtils
import com.gatigagae.app.ui.components.DogPhoto
import com.gatigagae.app.ui.components.PrimaryButton
import com.gatigagae.app.ui.components.SecondaryButton
import com.gatigagae.app.ui.components.SubText
import com.gatigagae.app.ui.components.TitleText
import com.gatigagae.app.ui.components.TopBar
import com.gatigagae.app.ui.theme.GatigagaeTheme
import com.gatigagae.app.ui.theme.GgBackground
import com.gatigagae.app.ui.theme.GgCard
import kotlinx.coroutines.delay

/** S1-07: 이 시간 동안 대답이 없으면 팝업을 닫아요 (팀 결정 전 임시 15초) */
private const val NO_RESPONSE_TIMEOUT_MS = 15_000L

/**
 * S1 탑승 확인 팝업
 * "콩이랑 같이 타나요?" → 응 / 아니
 *
 * TODO(STT): 팝업이 뜨면 바로 듣기 시작해서 "응"/"아니"를 알아듣기 (S1-06)
 * TODO(TTS): 질문 문구를 소리로 읽어주기 (S1-03)
 */
@Composable
fun BoardingScreen(
    profile: DogProfile?,
    gear: Gear,
    onYes: () -> Unit,
    onNo: () -> Unit,
    onTimeout: () -> Unit,
) {
    // LaunchedEffect 안에서 항상 최신 onTimeout을 부르기 위한 장치
    val currentOnTimeout by rememberUpdatedState(onTimeout)

    // S1-07: 15초 동안 대답이 없으면 닫기
    LaunchedEffect(Unit) {
        delay(NO_RESPONSE_TIMEOUT_MS)
        currentOnTimeout()
    }

    // S1-08: 대답 전에 D단으로 바뀌면 닫기 (주행 중 전체 화면 팝업 금지)
    LaunchedEffect(gear) {
        if (gear == Gear.D) currentOnTimeout()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GgBackground)
    ) {
        TopBar(gear = gear, dogMode = false)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            // 가운데 회색 카드
            Column(
                modifier = Modifier
                    .widthIn(max = 800.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(GgCard)
                    .padding(horizontal = 48.dp, vertical = 44.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DogPhoto(size = 120.dp)
                Spacer(Modifier.height(24.dp))

                // S1-03: 이름이 없으면 "반려견이랑 같이 타나요?"
                TitleText("${TextUtils.withRang(profile?.name ?: "")} 같이 타나요?", fontSize = 36.sp)
                Spacer(Modifier.height(14.dp))

                // S1-06
                SubText("🎤 듣고 있어요 · \"응\" 또는 \"아니\"라고 말해 주세요", fontSize = 18.sp)
                Spacer(Modifier.height(36.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    PrimaryButton("네, 함께 탑승합니다", onClick = onYes, modifier = Modifier.weight(1f))     // S1-04
                    SecondaryButton("아니요, 혼자 탑승합니다", onClick = onNo, modifier = Modifier.weight(1f)) // S1-05
                }
            }
        }
    }
}

@Preview(widthDp = 1700, heightDp = 1184)
@Composable
private fun BoardingScreenPreview() {
    GatigagaeTheme {
        BoardingScreen(profile = FakeData.profile, gear = Gear.P, onYes = {}, onNo = {}, onTimeout = {})
    }
}
