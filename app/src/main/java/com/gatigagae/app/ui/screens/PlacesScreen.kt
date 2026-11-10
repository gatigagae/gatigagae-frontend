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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gatigagae.app.data.FakeData
import com.gatigagae.app.data.Gear
import com.gatigagae.app.data.PetStatus
import com.gatigagae.app.data.Place
import com.gatigagae.app.data.TextUtils
import com.gatigagae.app.ui.components.GearChip
import com.gatigagae.app.ui.components.GgIcons
import com.gatigagae.app.ui.components.PrimaryButton
import com.gatigagae.app.ui.components.SecondaryButton
import com.gatigagae.app.ui.components.StatusBadge
import com.gatigagae.app.ui.theme.GatigagaeTheme
import com.gatigagae.app.ui.theme.GgBackground
import com.gatigagae.app.ui.theme.GgBlack
import com.gatigagae.app.ui.theme.GgBorder
import com.gatigagae.app.ui.theme.GgButtonBorder
import com.gatigagae.app.ui.theme.GgCard
import com.gatigagae.app.ui.theme.GgChipText
import com.gatigagae.app.ui.theme.GgTextCaption
import com.gatigagae.app.ui.theme.GgTextPrimary
import com.gatigagae.app.ui.theme.GgTextSecondary

/**
 * S3 추천 장소 리스트 (Figma 18:188 / 결과 없음 201:1749)
 *
 * Figma S3는 내비 옆 오른쪽 절반(1280×1440)에 그려져 있어요 → × 2/3 하면 분할 화면(840dp)과 거의 같아요.
 * 화면이 넓으면(앱 전체 화면) 왼쪽에 지도 자리 + 오른쪽 목록,
 * 화면이 좁으면(내비 옆 분할 화면) 목록만 보여줘요.
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
    onCall: (Place) -> Unit = {},
    loading: Boolean = false, // 서버에서 찾는 중
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
                        .padding(34.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(GgCard)
                        .border(1.dp, GgBorder, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "지도 자리\n(내비 분할 화면이면 여기에 차량 내비가 떠요)",
                        color = GgTextSecondary,
                        fontSize = 22.sp,
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
                onCall = onCall,
                loading = loading,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}

/*
 * Figma 값(× 2/3)
 *  - 바깥 여백: 왼쪽 51 → 34dp, 위 90 → 60dp, 아래 37 → 25dp
 *  - 뒤로 버튼 108 → 72dp (모서리 21.6 → 14), 화살표 50.4 → 34dp, 제목 54 → 36sp, 안내 30.6 → 20sp
 *  - 장소 카드 높이 306 → 204dp, 카드 사이 41 → 27dp, 모서리 28.8 → 19dp, 여백 왼쪽 50.4 / 오른쪽 39.6 → 34 / 26dp
 *  - 번호 원 79.2 → 53dp, 장소명 46.8 → 31sp, 거리 32.4 → 22sp, 설명 30.6 → 20sp
 *  - 길안내 버튼 126×190.8 → 84×127dp, 글자 36 → 24sp
 *  - 아래 버튼 높이 111.6 → 74dp, 사이 32.4 → 22dp
 */
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
    onCall: (Place) -> Unit,
    loading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(start = 34.dp, end = 34.dp, top = 60.dp, bottom = 25.dp)) {
        // ── 상단 바 (S3-12 뒤로, S3-13 제목, S3-14 안내, S3-15 기어) ──
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, GgButtonBorder, RoundedCornerShape(14.dp))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(GgIcons.ChevronLeft, contentDescription = "뒤로", tint = GgTextPrimary, modifier = Modifier.size(34.dp))
            }
            Spacer(Modifier.width(29.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "${TextUtils.withRang(dogName)} 갈 수 있는 $category",
                    color = GgTextPrimary,
                    fontSize = 36.sp,
                    lineHeight = 45.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "\"첫 번째로 가줘\"처럼 말하거나 길안내를 누르세요",
                    color = GgTextSecondary,
                    fontSize = 20.sp,
                    lineHeight = 26.sp,
                )
            }
            Spacer(Modifier.width(16.dp))
            GearChip(gear)
        }

        Spacer(Modifier.height(41.dp))

        // ── 장소 카드 3장 (S3-03) / 결과 없음 (S3-08) ──
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text("${TextUtils.withRang(dogName)} 갈 수 있는 곳을 찾는 중…", color = GgTextSecondary, fontSize = 28.sp)
            }
        } else if (places.isEmpty()) {
            EmptyResult(
                onBackToMain = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        } else {
            // 주행 중 긴 스크롤 금지 → 스크롤 없이 3장만 (S3-06)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(27.dp),
            ) {
                places.forEachIndexed { index, place ->
                    PlaceCard(number = index + 1, place = place, onNavigate = onNavigate, onCall = onCall)
                }
            }
        }

        Spacer(Modifier.height(22.dp))

        // ── 하단 버튼 (S3-11 재탐색, S3-07 다시 말하기) ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            SecondaryButton("재탐색", onClick = onMore, height = 74.dp, fontSize = 24.sp, modifier = Modifier.weight(558f))
            SecondaryButton(
                "다시 말하기",
                onClick = onSpeakAgain,
                height = 74.dp,
                fontSize = 24.sp,
                icon = GgIcons.Mic,
                modifier = Modifier.weight(583f),
            )
        }
    }
}

/** 장소 카드 1장 (Figma Place 컴포넌트) */
@Composable
private fun PlaceCard(number: Int, place: Place, onNavigate: (Place) -> Unit, onCall: (Place) -> Unit) {
    val shape = RoundedCornerShape(19.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 204.dp)
            .clip(shape)
            .background(GgCard, shape)
            .border(1.dp, GgBorder, shape)
            .padding(start = 34.dp, end = 26.dp, top = 20.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(31.dp),
    ) {
        // S3-04: 번호 원
        Box(
            modifier = Modifier
                .size(53.dp)
                .clip(CircleShape)
                .background(GgBlack),
            contentAlignment = Alignment.Center,
        ) {
            Text("$number", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = place.name,
                    color = GgTextPrimary,
                    fontSize = 31.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.width(14.dp))
                Text(
                    text = TextUtils.distance(place.distanceM),
                    color = GgTextCaption,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(bottom = 3.dp),
                )
            }
            Text(place.petSummary, color = GgChipText, fontSize = 20.sp, maxLines = 2)
            StatusBadge(place.petStatus) // S3-05
        }

        // 동반 정보가 없는 곳은 "전화 연결" 버튼도 보여줘요 (Figma 3번 카드)
        if (place.petStatus == PetStatus.UNKNOWN) {
            SecondaryButton(
                "전화 연결",
                onClick = { onCall(place) },
                height = 84.dp,
                fontSize = 24.sp,
                modifier = Modifier.widthIn(min = 127.dp), // 글자 길이만큼 넓어져요 (글자 잘림 방지)
            )
        }
        // S3-10: 길안내 버튼
        PrimaryButton(
            "길안내",
            onClick = { onNavigate(place) },
            height = 84.dp,
            fontSize = 24.sp,
            modifier = Modifier.width(127.dp),
        )
    }
}

/** 결과 없음 (Figma 201:1749): 제목 60 → 40sp, 설명 40 → 27sp, 버튼 558×111.6 → 372×74dp */
@Composable
private fun EmptyResult(onBackToMain: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("검색 결과가 없어요", color = Color.Black, fontSize = 40.sp, lineHeight = 50.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(13.dp))
        Text(
            "다른 키워드로 검색해보세요",
            color = Color.Black.copy(alpha = 0.8f),
            fontSize = 27.sp,
            lineHeight = 33.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(80.dp))
        PrimaryButton(
            "메인으로 돌아가기",
            onClick = onBackToMain,
            height = 74.dp,
            fontSize = 24.sp,
            modifier = Modifier.width(372.dp),
        )
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

@Preview(name = "S3 결과 없음", widthDp = 840, heightDp = 1184)
@Composable
private fun PlacesScreenEmptyPreview() {
    GatigagaeTheme {
        PlacesScreen("콩이", "카페", emptyList(), Gear.P, {}, {}, {}, {})
    }
}
