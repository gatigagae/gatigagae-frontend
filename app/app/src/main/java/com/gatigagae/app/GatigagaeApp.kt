package com.gatigagae.app

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.gatigagae.app.data.DestinationCheck
import com.gatigagae.app.data.DogProfile
import com.gatigagae.app.data.FakeData
import com.gatigagae.app.data.Gear
import com.gatigagae.app.data.Place
import com.gatigagae.app.ui.DevPanel
import com.gatigagae.app.ui.screens.BoardingScreen
import com.gatigagae.app.ui.screens.DestinationCheckScreen
import com.gatigagae.app.ui.screens.MainScreen
import com.gatigagae.app.ui.screens.PlacesScreen
import com.gatigagae.app.ui.screens.ProfileScreen
import com.gatigagae.app.ui.screens.VoiceSheet
import com.gatigagae.app.ui.theme.GgBackground

/** 시연 영상 찍을 때 false로 바꾸면 개발용 패널이 사라져요 */
const val SHOW_DEV_PANEL = true

/** 앱의 화면 목록 (IA 구조도 Depth 2) */
enum class Screen {
    BOARDING,          // S1 탑승 확인
    PROFILE,           // S2-2 프로필 등록·수정
    MAIN,              // S2 메인
    PLACES,            // S3 추천 장소
    DESTINATION_CHECK, // S5 동반 여부 안내
}

/**
 * 앱 전체 흐름(유저플로우)을 관리하는 곳.
 * "지금 어느 화면인지"와 "화면들이 같이 쓰는 값"을 여기 모아 두고, 각 화면에는 필요한 것만 넘겨요.
 */
@Composable
fun GatigagaeApp() {
    val context = LocalContext.current

    // ── 지금 보여줄 화면 ──
    var screen by remember { mutableStateOf(Screen.BOARDING) }

    // ── 차량 상태 (지금은 가짜, 개발용 패널로 바꿈) ──
    var gear by remember { mutableStateOf(Gear.P) }
    var drivingMinutes by remember { mutableIntStateOf(84) }

    // ── 반려견 ──
    var dogMode by remember { mutableStateOf(false) }
    var profile by remember { mutableStateOf<DogProfile?>(FakeData.profile) }

    // ── 음성 시트 ──
    var showVoiceSheet by remember { mutableStateOf(false) }
    var voicePreset by remember { mutableStateOf<String?>(null) }

    // ── 장소 추천 ──
    var category by remember { mutableStateOf("카페") }
    var page by remember { mutableIntStateOf(0) }
    var places by remember { mutableStateOf<List<Place>>(emptyList()) }

    // ── 목적지 ──
    var hasActiveRoute by remember { mutableStateOf(false) }
    var placeToConfirm by remember { mutableStateOf<Place?>(null) }
    var destinationCheck by remember { mutableStateOf<DestinationCheck?>(null) }

    val dogName = profile?.name ?: ""

    fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun openVoice(preset: String?) {
        voicePreset = preset
        showVoiceSheet = true
    }

    fun showPlaces(newCategory: String) {
        category = newCategory
        page = 0
        places = FakeData.places(newCategory, 0) // TODO(서버): 준석님 장소 검색 API로 교체
        screen = Screen.PLACES
    }

    // S4 목적지 설정
    fun startRoute(place: Place) {
        // TODO(S4): poiId 테스트 결과 보고 NaviHelper.requestRoute(...)로 교체
        hasActiveRoute = true
        toast("내비에 '${place.name}' 길안내를 요청했어요 (지금은 가짜)")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GgBackground)
    ) {
        when (screen) {
            Screen.BOARDING -> BoardingScreen(
                profile = profile,
                gear = gear,
                onYes = {
                    dogMode = true
                    // S1-09: 프로필이 없으면 등록부터
                    screen = if (profile == null) Screen.PROFILE else Screen.MAIN
                },
                onNo = {
                    // 기능명세서는 "앱 종료"인데 팀 결정 전이라 일단 메인으로 (반려견 모드 꺼짐)
                    dogMode = false
                    screen = Screen.MAIN
                },
                onTimeout = {
                    dogMode = false
                    screen = Screen.MAIN
                },
            )

            Screen.PROFILE -> ProfileScreen(
                initial = profile,
                gear = gear,
                onSave = { saved ->
                    profile = saved
                    screen = Screen.MAIN
                },
                onCancel = { screen = Screen.MAIN },
            )

            Screen.MAIN -> MainScreen(
                profile = profile,
                dogMode = dogMode,
                gear = gear,
                indoorTemp = FakeData.INDOOR_TEMP, // TODO(SDK): 실제 실내 온도
                drivingMinutes = drivingMinutes,   // TODO(SDK): 기어 D 누적 시간
                onEditProfile = { screen = Screen.PROFILE },
                onMicClick = { openVoice(null) },
                onExampleClick = { text -> openVoice(text) },
                onRestBannerClick = { toast("S6 휴게소 권고는 12/3 범위예요") },
            )

            Screen.PLACES -> PlacesScreen(
                dogName = dogName,
                category = category,
                places = places,
                gear = gear,
                onBack = { screen = Screen.MAIN },
                onNavigate = { place ->
                    // S4-02: 이미 경로가 있으면 바꿀지 먼저 묻기
                    if (hasActiveRoute) placeToConfirm = place else startRoute(place)
                },
                onMore = {
                    val next = FakeData.places(category, page + 1)
                    if (next.isEmpty()) {
                        toast("더 찾으려면 반경을 넓혀야 해요")
                    } else {
                        page += 1
                        places = next
                    }
                },
                onSpeakAgain = { openVoice(null) },
            )

            Screen.DESTINATION_CHECK -> {
                val check = destinationCheck
                if (check != null) {
                    DestinationCheckScreen(
                        dogName = dogName,
                        check = check,
                        gear = gear,
                        onShowAlternatives = { showPlaces("카페") },
                        onKeep = { screen = Screen.MAIN },
                    )
                }
            }
        }

        // S2-1 음성 시트 (메인·장소 화면 위에 겹쳐서 띄움)
        if (showVoiceSheet) {
            VoiceSheet(
                dogName = dogName,
                presetText = voicePreset,
                onCancel = { showVoiceSheet = false },
                onSearch = { request ->
                    showVoiceSheet = false
                    when (request.category) {
                        "휴게소" -> toast("S6 휴게소 권고는 12/3 범위예요")
                        "동물병원" -> toast("S8 응급 동물병원은 12/3 범위예요")
                        else -> showPlaces(request.category ?: "카페")
                    }
                },
            )
        }

        // S4-02 경로 변경 확인 다이얼로그
        val confirmPlace = placeToConfirm
        if (confirmPlace != null) {
            AlertDialog(
                onDismissRequest = { placeToConfirm = null },
                title = { Text("지금 경로를 바꿀까요?") },
                text = { Text("목적지를 '${confirmPlace.name}'(으)로 바꿔요.") },
                confirmButton = {
                    TextButton(onClick = {
                        placeToConfirm = null
                        startRoute(confirmPlace)
                    }) { Text("바꾸기") }
                },
                dismissButton = {
                    TextButton(onClick = { placeToConfirm = null }) { Text("그대로 두기") }
                },
            )
        }

        if (SHOW_DEV_PANEL) {
            DevPanel(
                gear = gear,
                onToggleGear = { gear = if (gear == Gear.P) Gear.D else Gear.P },
                onAddDriving = { drivingMinutes += 30 },
                onShowBoarding = { screen = Screen.BOARDING },
                onClearProfile = {
                    profile = null
                    screen = Screen.MAIN
                },
                onSimulateDestination = { index ->
                    destinationCheck = FakeData.destinationChecks[index]
                    screen = Screen.DESTINATION_CHECK
                },
                onOpenNaviTest = {
                    openTestScreen(context, "com.gatigagae.app.navitest.NaviTestActivity") { message -> toast(message) }
                },
                onOpenVoiceTest = {
                    openTestScreen(context, "com.gatigagae.app.voicetest.VoiceTestActivity") { message -> toast(message) }
                },
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}

/**
 * 테스트 화면(poiId, 음성) 열기.
 * 클래스 이름(글자)으로 여니까, 나중에 테스트 폴더를 지워도 이 파일은 고칠 필요 없어요.
 */
private fun openTestScreen(context: Context, className: String, onFail: (String) -> Unit) {
    try {
        val intent = Intent().setClassName(context, className)
        context.startActivity(intent)
    } catch (e: Exception) {
        onFail("테스트 화면이 없어요 (폴더·매니페스트 확인)")
    }
}
