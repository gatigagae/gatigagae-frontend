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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gatigagae.app.data.DestinationCheck
import com.gatigagae.app.data.FakeData
import com.gatigagae.app.data.Gear
import com.gatigagae.app.data.PetStatus
import com.gatigagae.app.data.TextUtils
import com.gatigagae.app.ui.components.PrimaryButton
import com.gatigagae.app.ui.components.SecondaryButton
import com.gatigagae.app.ui.components.StatusBadge
import com.gatigagae.app.ui.components.SubText
import com.gatigagae.app.ui.components.TitleText
import com.gatigagae.app.ui.components.TopBar
import com.gatigagae.app.ui.theme.GatigagaeTheme
import com.gatigagae.app.ui.theme.GgBackground
import com.gatigagae.app.ui.theme.GgCard
import com.gatigagae.app.ui.theme.GgTextPrimary

/**
 * S5 동반 가능 여부 안내
 * 와이어프레임이 아직 없어서 S1 팝업과 같은 모양(가운데 카드)으로 만들었어요.
 *
 * TODO(기획 결정): 내비가 앞에 떠 있을 때 이 화면을 어디에 보여줄지
 * TODO(TTS): 안내 문구를 소리로도 읽어주기 (S5-04)
 */
@Composable
fun DestinationCheckScreen(
    dogName: String,
    check: DestinationCheck,
    gear: Gear,
    onShowAlternatives: () -> Unit,
    onKeep: () -> Unit,
) {
    val headline = when (check.status) {
        PetStatus.CONFIRMED -> "여기는 ${TextUtils.withRang(dogName)} 함께 갈 수 있어요"
        PetStatus.UNKNOWN -> "동반 여부가 확인되지 않아요"
        PetStatus.NOT_ALLOWED -> "여기는 ${TextUtils.withRang(dogName)} 함께 들어갈 수 없어요"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GgBackground)
    ) {
        TopBar(gear = gear, dogMode = true)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 800.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(GgCard)
                    .padding(horizontal = 48.dp, vertical = 44.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                StatusBadge(check.status, fontSize = 18.sp) // S5-03
                Spacer(Modifier.height(20.dp))
                TitleText(check.placeName, fontSize = 34.sp)  // S5-04
                Spacer(Modifier.height(12.dp))
                Text(headline, color = GgTextPrimary, fontSize = 24.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                SubText(check.detail, fontSize = 18.sp)
                Spacer(Modifier.height(36.dp))

                if (check.status == PetStatus.CONFIRMED) {
                    PrimaryButton("확인", onClick = onKeep, modifier = Modifier.fillMaxWidth())
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        SecondaryButton("목적지 유지", onClick = onKeep, modifier = Modifier.weight(1f))           // S5-06
                        PrimaryButton("근처 동반 가능 장소 보기", onClick = onShowAlternatives, modifier = Modifier.weight(1.4f)) // S5-05
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 1700, heightDp = 1184)
@Composable
private fun DestinationCheckPreview() {
    GatigagaeTheme {
        DestinationCheckScreen("콩이", FakeData.destinationChecks[1], Gear.D, {}, {})
    }
}
