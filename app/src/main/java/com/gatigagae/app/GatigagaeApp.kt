package com.gatigagae.app

import android.content.Context
import android.content.Intent
import android.util.Log
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import com.gatigagae.app.data.VoiceRequestParser
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.gatigagae.app.data.DestinationCheck
import com.gatigagae.app.data.DogProfile
import com.gatigagae.app.data.FakeData
import com.gatigagae.app.data.Gear
import com.gatigagae.app.data.Place
import com.gatigagae.app.data.PlaceApi
import com.gatigagae.app.data.ProfileRepository
import com.gatigagae.app.data.TextUtils
import com.gatigagae.app.sdk.CarLocation
import com.gatigagae.app.sdk.NaviController
import com.gatigagae.app.sdk.VoiceController
import com.gatigagae.app.ui.DevPanel
import com.gatigagae.app.ui.components.GgDialog
import com.gatigagae.app.ui.screens.BoardingScreen
import com.gatigagae.app.ui.screens.DestinationCheckScreen
import com.gatigagae.app.ui.screens.MainScreen
import com.gatigagae.app.ui.screens.PlacesScreen
import com.gatigagae.app.ui.screens.REST_MINUTES
import com.gatigagae.app.ui.screens.QrDialog
import com.gatigagae.app.ui.screens.VoiceSheet
import com.gatigagae.app.ui.screens.placeConfirmLine
import com.gatigagae.app.ui.theme.AlertRed
import com.gatigagae.app.ui.theme.GgBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** 시연 영상 찍을 때 false로 바꾸면 개발용 패널이 사라져요 */
const val SHOW_DEV_PANEL = true

/** 앱의 화면 목록 (IA 구조도 Depth 2) */
enum class Screen {
    BOARDING,          // S1 탑승 확인
    PROFILE,           // S2-3 프로필 등록·수정 QR (입력은 휴대폰 웹)
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
    profiles: ProfileRepository? = null, // null이면(미리보기) 가짜 프로필
) {
    val context = LocalContext.current

    // ── 지금 보여줄 화면 ──
    var screen by remember { mutableStateOf(Screen.BOARDING) }

    // ── 차량 상태 (지금은 가짜, 개발용 패널로 바꿈) ──
    var gear by remember { mutableStateOf(Gear.P) }
    var drivingMinutes by remember { mutableIntStateOf(84) }

    // ── 반려견 ──
    var dogMode by remember { mutableStateOf(false) }
    // 처음엔 기기에 저장해 둔 프로필(지난번 값) → 서버에서 최신 값을 받으면 바꿔요
    var profile by remember {
        mutableStateOf<DogProfile?>(if (profiles == null) FakeData.profile else profiles.cachedProfile())
    }

    // ── 음성 시트 ──
    var showVoiceSheet by remember { mutableStateOf(false) }
    var voicePreset by remember { mutableStateOf<String?>(null) }
    var voiceSimulateFail by remember { mutableStateOf(false) } // 개발용: 인식 실패 화면 보기

    // ── 장소 추천 ──
    var category by remember { mutableStateOf("카페") }
    var page by remember { mutableIntStateOf(0) }
    var places by remember { mutableStateOf<List<Place>>(emptyList()) }
    var searchText by remember { mutableStateOf("") }    // 서버에 보낸 문장 (재탐색 때 같은 문장 + 다음 page)
    var hasNext by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // ── 목적지 ──
    var hasActiveRoute by remember { mutableStateOf(false) }
    var placeToConfirm by remember { mutableStateOf<Place?>(null) } // S4 "OO로 안내할까요?" 팝업
    var destinationCheck by remember { mutableStateOf<DestinationCheck?>(null) }

    // ── 팝업 ──
    var showQr by remember { mutableStateOf(false) }          // S2-3 프로필 수정 QR
    var showRestPrompt by remember { mutableStateOf(false) }  // S6 주행 2시간 경과
    var showGetOff by remember { mutableStateOf(false) }      // S7 하차 확인

    // ── 공조 (화면만. 실제 공조 연결은 2차) ──
    var hvacOn by remember { mutableStateOf(true) }
    var customTemp by remember { mutableStateOf<Int?>(null) } // null이면 서버 맞춤 온도

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

    // 우리 백엔드 서버에서 장소 찾기 (S3). 서버가 안 되면 예시 장소로 대신 보여줘요.
    // 검색 반경 (기획·백엔드 결정 11/15): 처음 5km → 결과 없으면 [10km로 넓히기] → [20km로 넓히기]
    var radiusM by remember { mutableStateOf(5_000) }

    fun searchPlaces(newPage: Int) {
        scope.launch {
            loading = true
            val (lat, lng) = CarLocation.current(context)
            try {
                val result = PlaceApi.search(searchText, lat, lng, page = newPage, radius = radiusM)
                result.category?.let { category = it }
                places = result.places
                hasNext = result.hasNext
                page = newPage
            } catch (e: Exception) {
                Log.e("PlaceApi", "장소 검색 실패", e)
                toast("서버에 연결하지 못해서 예시 장소를 보여줘요")
                places = FakeData.places(category, newPage)
                hasNext = FakeData.places(category, newPage + 1).isNotEmpty()
                page = newPage
            }
            loading = false
        }
    }

    // text: 서버에 보낼 문장. 종류(카페 등)를 앞에 붙여서 서버가 확실히 알아듣게 해요.
    fun showPlaces(newCategory: String, text: String = "근처 $newCategory 찾아줘") {
        category = newCategory
        searchText = when {
            newCategory == "전체" -> "반려견이랑 갈 곳 찾아줘" // 서버가 ALL(전체)로 알아듣는 문장
            text.contains(newCategory) -> text
            else -> "$newCategory $text"
        }
        places = emptyList()
        hasNext = false
        radiusM = 5_000
        screen = Screen.PLACES
        searchPlaces(0)
    }

    // S4 목적지 설정 → 차량 내비 길안내 (poiId 없이 좌표로, A-1에서 확인)
    fun startRoute(place: Place) {
        if (navi != null && !navi.requestRoute(place)) {
            toast("내비 연결 중이에요. 잠시 후 다시 눌러 주세요")
            return
        }
        hasActiveRoute = true
        // S4 "안내할까요?"에서 이미 확인했으니 팝업 대신 안내 문구 + 음성만
        toast("${TextUtils.withRo(place.name)} 안내를 시작했어요")
        voice?.speak("${TextUtils.withRo(place.name)} 안내합니다")
    }

    // 앱 켤 때 서버에서 내 반려견 받아오기 (처음이면 사용자 등록도 여기서)
    LaunchedEffect(profiles) {
        if (profiles != null) {
            try {
                profile = profiles.loadMyPet() // 등록 안 했으면 null → 메인에 "프로필이 없어요"
            } catch (e: Exception) {
                Log.e("Profile", "반려견 불러오기 실패 (저장해 둔 값 사용)", e)
            }
        }
    }

    // 내비가 알려주는 "안내 중인지" 값을 따라가기 (내비에서 직접 안내 종료해도 반영)
    DisposableEffect(navi) {
        navi?.onRouteActiveChanged = { active -> hasActiveRoute = active }
        onDispose { navi?.onRouteActiveChanged = null }
    }

    // S7 하차 확인 (기획 결정 11/14): 목적지에 도착한 뒤 기어를 P로 바꿀 때 물어봐요.
    // 도착 신호 → arrivedPending = true → 기어 P가 되는 순간 팝업. (도착 때 이미 P면 바로)
    var arrivedPending by remember { mutableStateOf(false) }
    fun onArrivedAtDestination() {
        if (!dogMode) {
            toast("반려견 모드가 꺼져 있어서 하차 확인을 띄우지 않아요")
            return
        }
        if (gear == Gear.P) showGetOff = true else arrivedPending = true
    }
    DisposableEffect(navi) {
        navi?.onArrived = { onArrivedAtDestination() }
        onDispose { navi?.onArrived = null }
    }
    LaunchedEffect(gear) {
        if (gear == Gear.P && arrivedPending) {
            arrivedPending = false
            if (dogMode) showGetOff = true
        }
    }

    // 연속 주행 2시간이 되면 팝업 + 음성 (S6, Figma "주행 2시간이 지났어요")
    // 음성은 팝업 쪽(ListenYesNo)에서 읽고 "응/괜찮아"를 들어요
    val overdue = drivingMinutes >= REST_MINUTES
    LaunchedEffect(overdue) {
        if (!overdue) return@LaunchedEffect
        if (dogMode) showRestPrompt = true else toast("반려견 모드가 꺼져 있어서 휴식 권고를 띄우지 않아요")
    }

    // 메인 화면 (S1 알림도 이 위에 떠요)
    val mainScreen = @Composable {
        MainScreen(
            profile = profile,
            dogMode = dogMode,
            gear = gear,
            indoorTemp = FakeData.INDOOR_TEMP, // TODO(SDK 2차): getHvacTemperatureCurrent
            drivingMinutes = drivingMinutes,   // TODO(SDK 2차): 기어 D 누적 시간
            onEditProfile = { showQr = true },
            onMicClick = { openVoice(null) },
            onExampleClick = { text -> openVoice(text) },
            onRestBannerClick = { showRestPrompt = true },
            hvacOn = hvacOn,
            onHvacChange = { on ->
                hvacOn = on
                // Figma 344:13797 토스트 문구
                toast(if (on) "공조를 맞춤 설정 온도 ${customTemp ?: profile?.preferredTemp ?: 22}°C로 맞췄어요" else "공조를 껐어요")
            },
            customTemp = customTemp ?: profile?.preferredTemp ?: 22,
            onCustomTempChange = { t -> customTemp = t.coerceIn(17, 30) },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GgBackground)
    ) {
        when (screen) {
            // S1: 메인 화면 위에 위쪽 알림 카드 (Figma S1 - L)
            Screen.BOARDING -> {
                mainScreen()
                BoardingScreen(
                    profile = profile,
                    gear = gear,
                    voice = voice,
                    onYes = {
                        dogMode = true
                        screen = Screen.MAIN
                        if (profile == null) showQr = true // S1-09: 프로필이 없으면 등록부터
                    },
                    onNo = {
                        dogMode = false
                        screen = Screen.MAIN
                    },
                    onTimeout = {
                        dogMode = false
                        screen = Screen.MAIN
                    },
                )
            }

            Screen.MAIN, Screen.PROFILE -> mainScreen()

            Screen.PLACES -> PlacesScreen(
                dogName = dogName,
                category = category,
                places = places,
                loading = loading,
                gear = gear,
                dogMode = dogMode,
                hasNext = hasNext,
                radiusKm = radiusM / 1000,
                loadDetail = { p ->
                    val (lat, lng) = CarLocation.current(context)
                    PlaceApi.detail(p, lat, lng)
                },
                onWiden = when (radiusM) {
                    5_000 -> ({ radiusM = 10_000; places = emptyList(); searchPlaces(0) })
                    10_000 -> ({ radiusM = 20_000; places = emptyList(); searchPlaces(0) })
                    else -> null
                },
                origin = remember(places) { CarLocation.current(context) },
                onCategoryChange = { tab -> if (!loading && tab != category) showPlaces(tab) },
                onBack = { screen = Screen.MAIN },
                // S4 (Figma 344:52820): 안내 시작을 누르면 "OO로 안내할까요?" 확인 팝업
                onNavigate = { place -> placeToConfirm = place },
                onMore = {
                    // 재탐색 = 같은 문장으로 다음 page
                    if (loading) return@PlacesScreen
                    if (hasNext) searchPlaces(page + 1) else toast("근처에 다른 장소가 없어요")
                },
                onSpeakAgain = { openVoice(null) },
                onCall = { place ->
                    toast(place.phone?.let { "${place.name} 전화번호: $it (전화 연결은 12/3 범위)" } ?: "전화번호 정보가 없어요")
                },
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
                        } else {
                            placeToConfirm = place // S4 확인 팝업 (음성으로 "응"이라고 해도 돼요)
                        }
                    }
                } else null,
                onCancel = { showVoiceSheet = false },
                onSearch = { request ->
                    showVoiceSheet = false
                    when (request.category) {
                        "휴게소" -> toast("S6 휴게소 권고는 12/3 범위예요")
                        "동물병원" -> toast("S8 응급 동물병원은 12/3 범위예요")
                        else -> showPlaces(request.category ?: "카페", request.text)
                    }
                },
            )
        }

        // S2-3 프로필 수정 QR 팝업 (Figma "프로필 수정")
        if (showQr) {
            QrDialog(
                repository = profiles,
                onDone = { saved ->
                    showQr = false
                    if (saved != null) {
                        profile = saved
                        customTemp = null // 새 프로필의 맞춤 온도로
                        toast("반려견 프로필이 업데이트됐어요") // Figma ivi_toast_popup 문구
                        voice?.speak("${saved.name} 프로필을 저장했어요")
                    }
                },
                onCancel = { showQr = false },
            )
        }

        // S6 주행 2시간 경과 (Figma S2 - P 2시간 경과 팝업)
        if (showRestPrompt) {
            // 시나리오: "괜찮아" → 닫고 기존 길안내 그대로 / "응" → 쉴 곳 찾기
            ListenYesNo(
                voice = voice,
                question = "주행 2시간이 지났어요. ${TextUtils.withWa(dogName)} 쉬어 갈 곳을 찾아드릴까요?",
                onYes = {
                    showRestPrompt = false
                    showPlaces("공원")
                },
                onNo = {
                    showRestPrompt = false
                    toast("알겠어요. 안내를 계속할게요")
                },
            )
            GgDialog(
                title = "주행 2시간이 지났어요",
                message = "${TextUtils.withWa(dogName)} 쉬어 갈 곳을 찾아드릴까요?", // 휴게소가 빠져서 "쉬어 갈 곳"(공원)으로
                iconRes = R.drawable.ic_pet_alert,
                iconTint = AlertRed,
                confirmText = "쉴 곳 찾기",
                onConfirm = {
                    showRestPrompt = false
                    showPlaces("공원") // 반려견과 쉬어 가기 좋은 공원 (디자인 확정되면 바꾸기)
                },
                dismissText = "괜찮아요",
                onDismiss = { showRestPrompt = false },
            )
        }

        // S7 하차 확인 (Figma S7 - L). 도착 후 기어 P일 때 떠요 (개발용 패널로도 띄울 수 있어요)
        if (showGetOff) {
            ListenYesNo(
                voice = voice,
                question = "${TextUtils.withWa(dogName)} 함께 내리셨나요?",
                onYes = {
                    showGetOff = false
                    dogMode = false
                    toast("반려견 모드를 껐어요")
                },
                onNo = {
                    showGetOff = false
                    toast("차 안에 반려견이 있어요. 실내 온도를 지켜볼게요")
                },
            )
            GgDialog(
                title = "반려견과 함께 내리셨나요?",
                message = "“응” 또는 “아니”라고 말해 주세요",
                iconRes = R.drawable.ic_paw,
                confirmText = "네, 함께 내렸어요",
                onConfirm = {
                    showGetOff = false
                    dogMode = false
                    toast("반려견 모드를 껐어요")
                },
                dismissText = "아니요",
                onDismiss = {
                    showGetOff = false
                    toast("차 안에 반려견이 있어요. 실내 온도를 지켜볼게요")
                },
            )
        }

        // S4 목적지 설정 확인 (Figma S4 - L 344:52820): "멍멍 테라스로 안내할까요?" [안내 시작][다른 곳 보기]
        // 이미 안내 중이면 기존 경로가 바뀐다는 걸 한 줄로 알려요 (S4-02)
        val confirmPlace = placeToConfirm
        if (confirmPlace != null) {
            val ask = "${TextUtils.withRo(confirmPlace.name)} 안내할까요?"
            ListenYesNo(
                voice = voice,
                question = if (hasActiveRoute) "지금 경로를 바꿔서 $ask" else ask,
                onYes = {
                    placeToConfirm = null
                    startRoute(confirmPlace)
                },
                onNo = { placeToConfirm = null },
            )
            GgDialog(
                title = ask,
                message = placeConfirmLine(confirmPlace) +
                    if (hasActiveRoute) "\n지금 안내 중인 경로가 바뀌어요" else "",
                iconRes = R.drawable.ic_pin,
                confirmText = "안내 시작",
                dismissText = "다른 곳 보기",
                onConfirm = {
                    placeToConfirm = null
                    startRoute(confirmPlace)
                },
                onDismiss = { placeToConfirm = null },
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
                // 처음부터 다시 (리허설·녹화용): 프로필·반려견 모드·기어·주행 시간·팝업을 모두 처음 상태로 → S1부터
                onClearProfile = {
                    profile = null
                    profiles?.saveProfile(null)
                    customTemp = null
                    dogMode = false
                    gear = Gear.P
                    drivingMinutes = 84
                    arrivedPending = false
                    showRestPrompt = false
                    showGetOff = false
                    placeToConfirm = null
                    showVoiceSheet = false
                    screen = Screen.BOARDING
                },
                onSimulateDestination = { index ->
                    destinationCheck = FakeData.destinationChecks[index]
                    screen = Screen.DESTINATION_CHECK
                },
                onOpenNaviTest = {
                    openTestScreen(context, "com.gatigagae.app.navitest.NaviTestActivity") { message -> toast(message) }
                },
                onShowVoiceFail = { openVoice(null, simulateFail = true) },
                onShowGetOff = { showGetOff = true },
                onArrive = {
                    onArrivedAtDestination()
                    toast(if (gear == Gear.P) "도착 (기어 P라 바로 확인)" else "도착 신호 받음 → 기어를 P로 바꾸면 하차 확인")
                },
                onUseFakeProfile = {
                    profile = FakeData.profile
                    profiles?.saveProfile(FakeData.profile)
                },
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

/**
 * 팝업이 떠 있는 동안: 질문을 소리로 읽고 → "응/네" 또는 "괜찮아/아니"를 들어요 (S1과 같은 방식, 2번까지)
 * 버튼을 눌러 팝업이 닫히면 듣기도 멈춰요.
 */
@Composable
private fun ListenYesNo(
    voice: VoiceController?,
    question: String,
    onYes: () -> Unit,
    onNo: () -> Unit,
) {
    val currentOnYes by rememberUpdatedState(onYes)
    val currentOnNo by rememberUpdatedState(onNo)
    DisposableEffect(voice) {
        var active = true
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
                            tries < 2 -> listenAnswer()
                        }
                    },
                    onFail = { if (active && tries < 2) listenAnswer() },
                )
            }
            voice.speak(question) { listenAnswer() }
        }
        onDispose {
            active = false
            voice?.stopListening()
        }
    }
}

/** 앱 안 안내 문구 (검은 둥근 상자) */
@Composable
private fun InAppToast(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        color = Color.White,
        fontSize = 28.sp,
        modifier = modifier
            .padding(bottom = 64.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xEB131417)) // Pleos ivi_toast_popup 느낌의 진한 회색
            .padding(horizontal = 48.dp, vertical = 32.dp),
    )
}
