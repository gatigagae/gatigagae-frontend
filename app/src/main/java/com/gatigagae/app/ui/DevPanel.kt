package com.gatigagae.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gatigagae.app.data.Gear
import com.gatigagae.app.ui.components.Pill

/**
 * 개발용 패널 (화면 위쪽 가운데 "🛠 개발용" 버튼)
 * 차량 신호·내비 이벤트가 아직 연결 안 됐으니, 버튼으로 흉내 내서 화면을 확인해요.
 * 시연 영상 찍을 때는 GatigagaeApp.kt의 SHOW_DEV_PANEL을 false로 바꿔서 숨기기!
 */
@Composable
fun DevPanel(
    gear: Gear,
    onToggleGear: () -> Unit,
    onAddDriving: () -> Unit,
    onShowBoarding: () -> Unit,
    onClearProfile: () -> Unit,
    onSimulateDestination: (Int) -> Unit,
    onOpenNaviTest: () -> Unit,
    onOpenVoiceTest: () -> Unit,
    onShowVoiceFail: () -> Unit,
    onShowEmptyPlaces: () -> Unit,
    onUseFakeProfile: () -> Unit,
    onShowGetOff: () -> Unit,
    onArrive: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    var hidden by remember { mutableStateOf(false) } // 시연 녹화용: 패널이 안 보이게

    if (hidden) {
        // 같은 자리에 안 보이는 버튼만 남겨요. 그 자리(왼쪽 가운데)를 누르면 다시 나와요.
        Box(
            modifier = modifier
                .padding(12.dp)
                .size(96.dp)
                .clickable { hidden = false },
        )
        return
    }

    Column(
        modifier = modifier.padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Pill(
            text = if (open) "개발용 닫기" else "🛠 개발용",
            textColor = Color.White,
            background = Color(0xFF222222),
            fontSize = 16.sp,
            onClick = { open = !open },
        )
        if (open) {
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .width(320.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xEE222222))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DevButton("🎬 녹화용으로 숨기기 (같은 자리를 누르면 다시 나와요)", { open = false; hidden = true })
                DevButton("기어 바꾸기 (지금 ${gear.name} → ${if (gear == Gear.P) "D" else "P"})", onToggleGear)
                DevButton("연속 주행 시간 +30분", onAddDriving)
                DevButton("S1 탑승 팝업 다시 띄우기", onShowBoarding)
                DevButton("🔄 처음부터 다시 (프로필 지우기 + 탑승 팝업)", onClearProfile)
                DevButton("가짜 프로필(콩이) 넣기", onUseFakeProfile)
                DevButton("S5: 목적지 = 동반 가능", { onSimulateDestination(0) })
                DevButton("S5: 목적지 = 정보 없음", { onSimulateDestination(1) })
                DevButton("S5: 목적지 = 동반 불가", { onSimulateDestination(2) })
                DevButton("음성 시트: 인식 실패 화면 보기", onShowVoiceFail)
                DevButton("🏁 목적지 도착 신호 (→ 기어 P로 바꾸면 하차 확인)", onArrive)
                DevButton("S7: 하차 확인 팝업 바로 보기", onShowGetOff)
                DevButton("S3: 검색 결과 없음 화면 보기", onShowEmptyPlaces)
                DevButton("🧭 poiId 테스트 화면 열기", onOpenNaviTest)
                DevButton("🎙 음성 테스트 화면 열기", onOpenVoiceTest)
            }
        }
    }
}

@Composable
private fun DevButton(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 15.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF444444))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    )
}
