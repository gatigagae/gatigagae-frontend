package com.gatigagae.app

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.gatigagae.app.data.TextUtils
import com.gatigagae.app.sdk.NaviController
import com.gatigagae.app.sdk.VoiceController
import com.gatigagae.app.ui.DevPanel
import com.gatigagae.app.ui.components.GgDialog
import com.gatigagae.app.ui.screens.BoardingScreen
import com.gatigagae.app.ui.screens.DestinationCheckScreen
import com.gatigagae.app.ui.screens.MainScreen
import com.gatigagae.app.ui.screens.PlacesScreen
import com.gatigagae.app.ui.screens.REST_MINUTES
import com.gatigagae.app.ui.screens.ProfileScreen
import com.gatigagae.app.ui.screens.VoiceSheet
import com.gatigagae.app.ui.theme.GgBackground
import kotlinx.coroutines.delay

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
fun GatigagaeApp(
    voice: VoiceController? = null, // null이면(미리보기) 말하기·듣기를 가짜로
    navi: NaviController? = null,   // null이면(미리보기) 길안내를 가짜로
) {
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
    var voiceSimulateFail by remember { mutableStateOf(false) } // 개발용: 인식 실패 화면 보기

    // ── 장소 추천 ──
    var category by remember { mutableStateOf("카페") }
    var page by remember { mutableIntStateOf(0) }
    var places by remember { mutableStateOf<List<Place>>(emptyList()) }

    // ── 목적지 ──
    var hasActiveRoute by remember { mutableStateOf(false) }
    var placeToConfirm by remember { mutableStateOf<Place?>(null) }
    var routeStartedPlace by remember { mutableStateOf<Place?>(null) } // "[장소명]으로 안내합니다" 팝업
    var destinationCheck by remember { mutableStateOf<DestinationCheck?>(null) }

    val dogName = profile?.name ?: ""

    // 안내 문구 (Pleos 화면에서 안드로이드 기본 Toast가 안 보여서, 앱 안에 직접 띄워요)
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastId by remember { mutableIntStateOf(0) }

    fun toast(message: String) {
        toastMessage = message
        toastId += 1 // 같은 문구를 또 눌러도 2.5초 다시 세기
    }

    fun openVoice(preset: String?, simulateFail: Boolean = false) {
        voicePreset = preset
        voiceSimulateFail = simulateFail
        showVoiceSheet = true
    }

    fun showPlaces(newCategory: String) {
        category = newCategory
        page = 0
        places = FakeData.places(newCategory, 0) // TODO(서버): 준석님 장소 검색 API로 교체
        screen = Screen.PLACES
    }

    // S4 목적지 설정 → 차량 내비 길안내 (poiId 없이 좌표로, A-1에서 확인)
    fun startRoute(place: Place) {
        if (navi != null && !navi.requestRoute(place)) {
            toast("내비 연결 중이에요. 잠시 후 다시 눌러 주세요")
            return
        }
        hasActiveRoute = true
        routeStartedPlace = place // 시나리오 6: "[장소명](으)로 안내합니다" 팝업
        voice?.speak("${TextUtils.withRo(place.name)} 안내합니다")
    }

    // 내비가 알려주는 "안내 중인지" 값을 따라가기 (내비에서 직접 안내 종료해도 반영)
    DisposableEffect(navi) {
        navi?.onRouteActiveChanged = { active -> hasActiveRoute = active }
        onDispose { navi?.onRouteActiveChanged = null }
    }

    // 연속 주행 2시간이 되면 한 번 말해 주기 (S2-08)
    val overdue = drivingMinutes >= REST_MINUTES
    LaunchedEffect(overdue) {
        if (overdue && dogMode) {
            voice?.speak("${TextUtils.withRang(dogName)} 달린 지 2시간이 넘었어요. 잠시 쉬어 가요.")
        }
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
                voice = voice,
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
                onCall = { place -> toast("${place.name} 전화 연결은 12/3 범위예요") },
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
                simulateFail = voiceSimulateFail,
                voice = voice,
                // S3 화면에서 연 경우: "첫 번째로 가줘" → 그 장소로 길안내
                onChoice = if (screen == Screen.PLACES) {
                    { index ->
                        showVoiceSheet = false
                        val place = places.getOrNull(index)
                        if (place == null) {
                            toast("${index + 1}번째 장소가 없어요")
                        } else if (hasActiveRoute) {
                            placeToConfirm = place
                        } else {
                            startRoute(place)
                        }
                    }
                } else null,
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

        // S4-02 경로 변경 확인 팝업
        val confirmPlace = placeToConfirm
        if (confirmPlace != null) {
            GgDialog(
                title = "기존 경로를 변경할까요?", // Figma 133:412
                confirmText = "네",
                dismissText = "취소",
                onConfirm = {
                    placeToConfirm = null
                    startRoute(confirmPlace)
                },
                onDismiss = { placeToConfirm = null },
            )
        }

        // S4 안내 시작 팝업 (시나리오 6, Figma 133:410) — 버튼 없이 3초 뒤 자동으로 닫혀요. 눌러도 닫혀요.
        val startedPlace = routeStartedPlace
        if (startedPlace != null) {
            LaunchedEffect(startedPlace) {
                delay(3_000)
                routeStartedPlace = null
            }
            GgDialog(
                title = "${TextUtils.withRo(startedPlace.name)} 안내합니다",
                onDismiss = { routeStartedPlace = null },
            )
        }

        // 앱 안 안내 문구 (아래 가운데, 2.5초 뒤 사라짐)
        val message = toastMessage
        if (message != null) {
            LaunchedEffect(toastId) {
                delay(2_500)
                toastMessage = null
            }
            InAppToast(message, Modifier.align(Alignment.BottomCenter))
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
                onShowVoiceFail = { openVoice(null, simulateFail = true) },
                onShowEmptyPlaces = {
                    places = emptyList()
                    screen = Screen.PLACES
                },
                onOpenVoiceTest = {
                    openTestScreen(context, "com.gatigagae.app.voicetest.VoiceTestActivity") { message -> toast(message) }
                },
                modifier = Modifier.align(Alignment.CenterStart), // 왼쪽 가운데 (화면 버튼과 안 겹치게)
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

/** 앱 안 안내 문구 (검은 둥근 상자) */
@Composable
private fun InAppToast(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        color = Color.White,
        fontSize = 24.sp,
        modifier = modifier
            .padding(bottom = 140.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xE6222222))
            .padding(horizontal = 32.dp, vertical = 18.dp),
    )
}
