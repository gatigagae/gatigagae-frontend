package com.gatigagae.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.gatigagae.app.data.Gear
import com.gatigagae.app.data.Place
import com.gatigagae.app.data.TextUtils
import com.gatigagae.app.ui.components.GearChip
import com.gatigagae.app.ui.components.PrimaryButton
import com.gatigagae.app.ui.components.SecondaryButton
import com.gatigagae.app.ui.components.StatusBadge
import com.gatigagae.app.ui.components.SubText
import com.gatigagae.app.ui.components.TitleText
import com.gatigagae.app.ui.theme.GatigagaeTheme
import com.gatigagae.app.ui.theme.GgBackground
import com.gatigagae.app.ui.theme.GgBlack
import com.gatigagae.app.ui.theme.GgBorder
import com.gatigagae.app.ui.theme.GgCard
import com.gatigagae.app.ui.theme.GgTextPrimary
import com.gatigagae.app.ui.theme.GgTextSecondary

/**
 * S3 추천 장소 리스트
 *
 * 화면이 넓으면(앱 전체 화면) 왼쪽에 지도 자리 + 오른쪽 목록,
 * 화면이 좁으면(내비 옆 분할 화면) 목록만 보여줘요.
 * → S3-01 "앱 안 지도 vs 내비 분할" 팀 결정 전이라 둘 다 되게 해 뒀어요.
 */
@Composable
fun PlacesScreen(
    dogName: String,
    category: String,
    places: List<Place>,
    gear: Gear,
    onBack: () -> Unit,
    onNavigate: (Place) -> Unit,
    onMore: () -> Unit,
    onSpeakAgain: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(GgBackground)
    ) {
        val wide = maxWidth > 1100.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (wide) {
                // 지도 자리 (S3-02). 지도를 어떻게 할지 정해지면 교체
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(24.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(GgCard),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "지도 자리\n(내비 분할 화면이면 여기에 차량 내비가 떠요)",
                        color = GgTextSecondary,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            PlaceList(
                dogName = dogName,
                category = category,
                places = places,
                gear = gear,
                onBack = onBack,
                onNavigate = onNavigate,
                onMore = onMore,
                onSpeakAgain = onSpeakAgain,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun PlaceList(
    dogName: String,
    category: String,
    places: List<Place>,
    gear: Gear,
    onBack: () -> Unit,
    onNavigate: (Place) -> Unit,
    onMore: () -> Unit,
    onSpeakAgain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(24.dp)) {
        // ── 상단 바 (S3-12 뒤로, S3-13 제목, S3-14 안내, S3-15 기어) ──
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, GgBorder, RoundedCornerShape(14.dp))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Text("‹", fontSize = 32.sp, color = GgTextPrimary)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                TitleText("${TextUtils.withRang(dogName)} 갈 수 있는 $category · 근처 ${places.size}곳", fontSize = 26.sp)
                SubText("\"첫 번째로 가줘\"처럼 말하거나 길안내를 누르세요", fontSize = 16.sp)
            }
            Spacer(Modifier.width(12.dp))
            GearChip(gear)
        }

        Spacer(Modifier.height(20.dp))

        // ── 장소 카드 3장 (S3-03) / 결과 없음 (S3-08) ──
        if (places.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "반경 안에 결과가 없어요.\n반경을 넓혀서 다시 찾아볼까요?",
                    color = GgTextSecondary,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            // 주행 중 긴 스크롤 금지 → 스크롤 없이 3장만 (S3-06)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                places.forEachIndexed { index, place ->
                    PlaceCard(number = index + 1, place = place, onNavigate = onNavigate)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── 하단 버튼 (S3-11 다른 곳 보기, S3-07 다시 말하기) ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SecondaryButton("다른 곳 보기", onClick = onMore, modifier = Modifier.weight(1f))
            SecondaryButton("🎤 다시 말하기", onClick = onSpeakAgain, modifier = Modifier.weight(1f))
        }
    }
}

/** 장소 카드 1장 */
@Composable
private fun PlaceCard(number: Int, place: Place, onNavigate: (Place) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GgCard)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // S3-04: 번호 원
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(GgBlack),
            contentAlignment = Alignment.Center,
        ) {
            Text("$number", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(20.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(place.name, color = GgTextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(10.dp))
                Text(TextUtils.distance(place.distanceM), color = GgTextSecondary, fontSize = 16.sp)
            }
            Spacer(Modifier.height(6.dp))
            Text(place.petSummary, color = GgTextPrimary, fontSize = 17.sp)
            Spacer(Modifier.height(10.dp))
            StatusBadge(place.petStatus) // S3-05
        }
        Spacer(Modifier.width(16.dp))

        // S3-10: 길안내 버튼
        PrimaryButton("길안내", onClick = { onNavigate(place) }, modifier = Modifier.width(128.dp))
    }
}

@Preview(name = "S3 넓은 화면", widthDp = 1700, heightDp = 1184)
@Composable
private fun PlacesScreenWidePreview() {
    GatigagaeTheme {
        PlacesScreen("콩이", "카페", FakeData.places("카페", 0), Gear.P, {}, {}, {}, {})
    }
}

@Preview(name = "S3 분할 화면", widthDp = 840, heightDp = 1184)
@Composable
private fun PlacesScreenSplitPreview() {
    GatigagaeTheme {
        PlacesScreen("콩이", "카페", FakeData.places("카페", 0), Gear.D, {}, {}, {}, {})
    }
}
