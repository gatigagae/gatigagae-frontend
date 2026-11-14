package com.gatigagae.app.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gatigagae.app.R
import com.gatigagae.app.data.FakeData
import com.gatigagae.app.data.Gear
import com.gatigagae.app.data.Place
import com.gatigagae.app.data.TextUtils
import com.gatigagae.app.ui.components.BasicButton
import com.gatigagae.app.ui.components.GoldButton
import com.gatigagae.app.ui.components.PawBadge
import com.gatigagae.app.ui.components.TopBar
import com.gatigagae.app.ui.components.WhiteCard
import com.gatigagae.app.ui.theme.BgPrimary
import com.gatigagae.app.ui.theme.Divider10
import com.gatigagae.app.ui.theme.GatigagaeTheme
import com.gatigagae.app.ui.theme.Gold100
import com.gatigagae.app.ui.theme.Gold500
import com.gatigagae.app.ui.theme.Gold700
import com.gatigagae.app.ui.theme.Gold900
import com.gatigagae.app.ui.theme.Surface
import com.gatigagae.app.ui.theme.TextPrimary
import com.gatigagae.app.ui.theme.TextSecondary
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max

/** (예전 탭 목록. 새 시안(11/14)에서 탭이 빠져서 화면에는 안 써요) */
val PLACE_TABS = listOf("전체", "카페", "식당", "공원")

/**
 * S3 검색 결과 (Figma 11/14 수정본: "S3 - L" 413:35156 / "S3 - P" 413:34583 / 결과 없음 413:34741 / 장소 정보 413:34782)
 *
 * 탭은 빠졌어요. 목록·장소 정보는 새 시안 모양.
 * 지도 그림은 기획 결정(11/14)으로 1차에는 남겨요 ("지도 연동 가능성"을 보여 주려고. 실제 지도는 2차에 검토).
 *   가로: [지도 그림 | 목록 660]  세로: [지도 그림 300] / [목록]
 *   지도는 실제 지도가 아니라 그림 위에, 현재 위치 기준 방향·거리 비율로 번호 핀을 찍어요.
 * [··· 거리순] / "반려견 동반 가능 3곳 · 2.8km 이내" / 장소 카드(번호 · 이름 · 정보 · 뱃지 · [안내 시작])
 * 장소 카드를 누르면 "장소 정보" (동반 조건, 주소, 전화) → [안내 시작][전화하기]
 * 하단 버튼은 시안대로 없어요 (다른 장소는 ← 뒤로 가서 다시 말하기).
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
    loading: Boolean = false,
    dogMode: Boolean = true,
    onCategoryChange: (String) -> Unit = {},
    origin: Pair<Double, Double>? = null,
    hasNext: Boolean = false,
    radiusKm: Int = 20,
    onWiden: (() -> Unit)? = null, // 결과 없음에서 반경 넓히기 (20km면 null)
    loadDetail: (suspend (Place) -> Place?)? = null, // 장소 상세 API (영업 시간·주차·전화·동반 조건)
) {
    var detail by remember(places) { mutableStateOf<Place?>(null) }
    // 장소 정보를 열면 상세 API로 채워요 (실패하면 검색 결과 정보 그대로)
    val detailId = detail?.id
    LaunchedEffect(detailId) {
        val current = detail ?: return@LaunchedEffect
        val loaded = loadDetail?.invoke(current) ?: return@LaunchedEffect
        if (detail?.id == loaded.id) detail = loaded
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(BgPrimary)
    ) {
        val portrait = maxWidth < 1100.dp
        Column(
            modifier = Modifier.fillMaxSize().padding(start = 49.dp, end = 49.dp, top = 25.dp, bottom = 49.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            val shown = detail
            TopBar(
                gear = gear,
                dogMode = dogMode,
                title = if (shown != null) "장소 정보" else "검색 결과", // 기획 요청(11/14): "추천 장소" → "검색 결과"
                onBack = { if (detail != null) detail = null else onBack() },
            )

            // 지도 그림 (핀 누르면 그 장소 정보)
            val map = @Composable { m: Modifier ->
                PlaceMap(places, places.indexOfFirst { it.id == shown?.id }, origin, m, onPinClick = { i -> places.getOrNull(i)?.let { detail = it } })
            }

            when {
                shown != null && portrait -> {
                    map(Modifier.fillMaxWidth().height(300.dp))
                    PlaceDetail(shown, true, onNavigate, onCall, Modifier.fillMaxWidth().weight(1f))
                }
                shown != null -> Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    map(Modifier.weight(1f).fillMaxHeight())
                    PlaceDetail(shown, true, onNavigate, onCall, Modifier.width(660.dp).fillMaxHeight())
                }
                loading -> CenterMessage(
                    title = "찾고 있어요",
                    message = "${TextUtils.withRang(dogName)} 갈 수 있는 ${if (category == "장소" || category == "전체") "곳" else category} 찾는 중이에요",
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
                places.isEmpty() -> EmptyResult(category, radiusKm, onWiden, onSpeakAgain, onBack, Modifier.fillMaxWidth().weight(1f))
                portrait -> {
                    map(Modifier.fillMaxWidth().height(300.dp))
                    PlaceList(places, radiusKm, true, onNavigate, onMore, onSpeakAgain, { detail = it }, Modifier.fillMaxWidth().weight(1f))
                }
                else -> Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    map(Modifier.weight(1f).fillMaxHeight())
                    PlaceList(places, radiusKm, false, onNavigate, onMore, onSpeakAgain, { detail = it }, Modifier.width(660.dp).fillMaxHeight())
                }
            }
        }
    }
}

/** 목록 칸: [··· 거리순] / 요약 / 카드들 / [다른 곳 보기][다시 말하기] */
@Composable
private fun PlaceList(
    places: List<Place>,
    radiusKm: Int,
    portrait: Boolean,
    onNavigate: (Place) -> Unit,
    onMore: () -> Unit,
    onSpeakAgain: () -> Unit,
    onOpen: (Place) -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // 정렬 (오른쪽)
        Row(Modifier.fillMaxWidth()) {
            Spacer(Modifier.weight(1f))
            SortDropdown()
        }
        Text(
            "반려견 동반 가능 ${places.size}곳 · ${radiusKm}km 이내",
            color = TextSecondary,
            fontSize = 26.sp,
            lineHeight = 34.sp,
        )
        places.forEachIndexed { i, place ->
            PlaceCard(i + 1, place, portrait, onNavigate, onOpen = { onOpen(place) })
        }
        // 새 시안(11/14): 하단 [다른 곳 보기][다시 말하기] 없음. 다른 장소는 ← 뒤로 가서 다시 말하기
    }
}

// ───────────────────────── 정렬 ─────────────────────────

/** "거리순 ⌄" (ivi_dropdown: 폭 240, 높이 80, 모서리 28, 테두리 2) — 지금은 거리순만 있어요 */
@Composable
private fun SortDropdown() {
    val shape = RoundedCornerShape(28.dp)
    Row(
        modifier = Modifier
            .width(240.dp)
            .height(80.dp)
            .shadow(6.dp, shape)
            .clip(shape)
            .background(Surface)
            .border(2.dp, Divider10, shape)
            .padding(horizontal = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("거리순", color = TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Icon(painterResource(R.drawable.ic_chevron_down), contentDescription = null, tint = TextPrimary, modifier = Modifier.size(48.dp))
    }
}

// ───────────────────────── 지도 그림 ─────────────────────────

/** 지도 그림 + 현재 위치 + 번호 핀 (선택된 핀: 진한 골드 72 / 나머지: 골드 56, 흰 테두리) */
@Composable
private fun PlaceMap(
    places: List<Place>,
    selected: Int,
    origin: Pair<Double, Double>?,
    modifier: Modifier,
    onPinClick: (Int) -> Unit,
) {
    BoxWithConstraints(modifier = modifier.clip(RoundedCornerShape(32.dp)).background(Color(0xFFEEF0F2))) {
        Image(
            painterResource(R.drawable.map_illust),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        if (places.isEmpty()) return@BoxWithConstraints
        val w = maxWidth
        val h = maxHeight
        // 현재 위치는 가운데, 장소는 실제 방향·거리 비율로 (가장 먼 곳이 가장자리 안쪽에 오게)
        val oLat = origin?.first ?: places.map { it.lat }.average()
        val oLng = origin?.second ?: places.map { it.lng }.average()
        val k = cos(Math.toRadians(oLat))
        val offsets = places.map { (it.lng - oLng) * k to (it.lat - oLat) }
        val far = max(offsets.maxOfOrNull { max(abs(it.first), abs(it.second)) } ?: 0.0, 1e-6)

        fun pos(dx: Double, dy: Double, size: Dp): Pair<Dp, Dp> {
            val x = w / 2 + (w / 2 - 60.dp) * (dx / far).toFloat() - size / 2
            val y = h / 2 - (h / 2 - 60.dp) * (dy / far).toFloat() - size / 2
            return x to y
        }

        val (cx, cy) = pos(0.0, 0.0, 72.dp)
        Image(painterResource(R.drawable.map_my_location), contentDescription = "현재 위치", modifier = Modifier.offset(cx, cy).size(72.dp))

        offsets.forEachIndexed { i, (dx, dy) ->
            val isSel = i == selected
            val size = if (isSel) 72.dp else 56.dp
            val (x, y) = pos(dx, dy, size)
            Box(
                modifier = Modifier
                    .offset(x, y)
                    .size(size)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(if (isSel) 5.dp else 4.dp)
                    .clip(CircleShape)
                    .background(if (isSel) Gold700 else Gold500)
                    .clickable { onPinClick(i) },
                contentAlignment = Alignment.Center,
            ) {
                Text("${i + 1}", color = Color.White, fontSize = if (isSel) 32.sp else 26.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ───────────────────────── 목록 ─────────────────────────

/**
 * 장소 카드 (Figma 413:35156): 흰 바탕 모서리 32, 높이 196(세로 208)
 * 번호(골드 글자 32) · 이름 32 굵게 · "카페 · 1.2km · 4분" 26 · 동반 가능 뱃지 · [안내 시작] 198×100
 */
@Composable
private fun PlaceCard(number: Int, place: Place, portrait: Boolean, onNavigate: (Place) -> Unit, onOpen: () -> Unit) {
    val shape = RoundedCornerShape(32.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Surface, shape)
            .clickable(onClick = onOpen)
            .padding(horizontal = if (portrait) 36.dp else 28.dp, vertical = if (portrait) 32.dp else 28.dp),
        horizontalArrangement = Arrangement.spacedBy(if (portrait) 24.dp else 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "$number",
            color = Gold700,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(32.dp),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(place.name, color = Gold900, fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subLine(place), color = TextSecondary, fontSize = 26.sp, lineHeight = 34.sp, maxLines = 1)
            PawBadge()
        }
        GoldButton("안내 시작", onClick = { onNavigate(place) }, large = true, modifier = Modifier.widthIn(min = 198.dp)) // → S4 (글자가 잘리지 않게 최소 198)
    }
}

@Composable
private fun ListActions(hasNext: Boolean, onMore: () -> Unit, onSpeakAgain: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        BasicButton(if (hasNext) "다른 곳 보기" else "재탐색", onClick = onMore, modifier = Modifier.weight(1f))
        BasicButton("다시 말하기", onClick = onSpeakAgain, modifier = Modifier.weight(1f))
    }
}

/** S4 안내 확인 팝업용: "카페 · 1.2km · 약 4분 · 반려견 동반 가능" */
fun placeConfirmLine(place: Place): String = "${subLine(place, approx = true)} · 반려견 동반 가능"

/** "카페 · 1.2km · 약 4분" (분은 시속 30km 기준 대략값) */
private fun subLine(place: Place, approx: Boolean = false): String {
    val minutes = max(1, (place.distanceM + 499) / 500)
    return "${place.category} · ${TextUtils.distance(place.distanceM)} · ${if (approx) "약 " else ""}${minutes}분"
}

// ───────────────────────── 장소 정보 ─────────────────────────

/**
 * 장소 정보 (Figma 413:34782): 흰 카드 하나에 여백 48
 * 이름 40 · 정보 26 · 뱃지 / 구분선 / 정보 줄(아이콘 32 · 이름 22 · 값 28, 줄 간격 48) / 가운데 [안내 시작][전화하기] 400×100
 * 상세 API(11/15)로 동반 조건·영업 시간·주차(가능/불가능)·주소·전화를 채워요. 없는 정보 줄은 숨겨요.
 */
@Composable
private fun PlaceDetail(place: Place, portrait: Boolean, onNavigate: (Place) -> Unit, onCall: (Place) -> Unit, modifier: Modifier) {
    WhiteCard(modifier = modifier, padding = androidx.compose.foundation.layout.PaddingValues(48.dp)) {
        Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
            Text(place.name, color = Gold900, fontSize = 40.sp, lineHeight = 52.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Text(subLine(place, approx = true), color = TextSecondary, fontSize = 26.sp, lineHeight = 34.sp)
            Spacer(Modifier.height(16.dp))
            PawBadge()
            Spacer(Modifier.height(48.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Divider10))
            Spacer(Modifier.height(48.dp))
            Column(verticalArrangement = Arrangement.spacedBy(48.dp)) {
                InfoRow(R.drawable.ic_paw, "반려견 동반", place.petSummary.ifBlank { "반려견 동반 가능" })
                place.hours?.let { InfoRow(R.drawable.ic_clock, "영업 시간", it) }
                place.parking?.let { InfoRow(R.drawable.ic_parking, "주차", if (it) "가능" else "불가능") }
                if (place.address.isNotBlank()) InfoRow(R.drawable.ic_pin, "주소", place.address)
                place.phone?.let { InfoRow(R.drawable.ic_phone, "전화", it) }
            }
        }
        Spacer(Modifier.height(32.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        ) {
            val buttonModifier = if (portrait) Modifier.weight(1f) else Modifier.width(400.dp)
            GoldButton("안내 시작", onClick = { onNavigate(place) }, large = true, modifier = buttonModifier)
            BasicButton("전화하기", onClick = { onCall(place) }, large = true, enabled = place.phone != null, modifier = buttonModifier)
        }
    }
}

@Composable
private fun InfoRow(iconRes: Int, label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        Icon(painterResource(iconRes), contentDescription = null, tint = TextSecondary, modifier = Modifier.size(32.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, color = TextSecondary, fontSize = 22.sp, lineHeight = 28.sp)
            Text(value.ifBlank { "정보 없음" }, color = TextPrimary, fontSize = 28.sp, lineHeight = 36.sp)
        }
    }
}

// ───────────────────────── 빈 화면 ─────────────────────────

/**
 * 결과 없음 (Figma 413:34741): 아이콘(연한 골드 원 120) · "근처에서 찾지 못했어요" 36 · 설명 26 · 세로 버튼 2개(폭 600)
 * 반경 결정(11/15): 5km → [10km로 넓히기] → [20km로 넓히기]. 20km에서도 없으면 [다른 곳 말하기][메인으로].
 */
@Composable
private fun EmptyResult(category: String, radiusKm: Int, onWiden: (() -> Unit)?, onSpeakAgain: () -> Unit, onBack: () -> Unit, modifier: Modifier) {
    CenterMessage(
        title = "근처에서 찾지 못했어요",
        message = "${radiusKm}km 이내에 반려견과 갈 수 있는 ${TextUtils.withIga(if (category == "장소" || category == "전체") "곳" else category)} 없어요",
        modifier = modifier,
    ) {
        Spacer(Modifier.height(32.dp))
        Column(Modifier.widthIn(max = 600.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (onWiden != null) {
                GoldButton("${radiusKm * 2}km로 넓히기", onClick = onWiden, large = true, modifier = Modifier.fillMaxWidth())
                BasicButton("다른 곳 말하기", onClick = onSpeakAgain, large = true, modifier = Modifier.fillMaxWidth())
            } else {
                GoldButton("다른 곳 말하기", onClick = onSpeakAgain, large = true, modifier = Modifier.fillMaxWidth())
                BasicButton("메인으로", onClick = onBack, large = true, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun CenterMessage(title: String, message: String?, modifier: Modifier, extra: @Composable () -> Unit = {}) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(
            modifier = Modifier.size(120.dp).clip(CircleShape).background(Gold100),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.ic_paw_line), contentDescription = null, tint = Gold700, modifier = Modifier.size(56.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(title, color = TextPrimary, fontSize = 36.sp, lineHeight = 44.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        if (message != null) {
            Spacer(Modifier.height(20.dp))
            Text(message, color = TextSecondary, fontSize = 26.sp, lineHeight = 34.sp, textAlign = TextAlign.Center)
        }
        extra()
    }
}

@Preview(name = "S3 가로", widthDp = 1700, heightDp = 1184)
@Composable
private fun PlacesScreenPreview() {
    GatigagaeTheme { PlacesScreen("콩이", "카페", FakeData.places("카페", 0), Gear.P, {}, {}, {}, {}) }
}

@Preview(name = "S3 세로", widthDp = 840, heightDp = 1184)
@Composable
private fun PlacesScreenPortraitPreview() {
    GatigagaeTheme { PlacesScreen("콩이", "카페", FakeData.places("카페", 0), Gear.P, {}, {}, {}, {}) }
}
