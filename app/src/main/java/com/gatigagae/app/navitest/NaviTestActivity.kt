package com.gatigagae.app.navitest

import ai.pleos.playground.navi.constants.NaviErrorCode
import ai.pleos.playground.navi.data.DestinationArrivedInfo
import ai.pleos.playground.navi.data.DestinationInfo
import ai.pleos.playground.navi.data.NavigationScreenRequest
import ai.pleos.playground.navi.data.RouteInfo
import ai.pleos.playground.navi.data.RouteStartInfo
import ai.pleos.playground.navi.data.RouteStateInfo
import ai.pleos.playground.navi.helper.NaviHelper
import ai.pleos.playground.navi.helper.listener.NaviHelperEventListener
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gatigagae.app.ui.theme.GatigagaeTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * poiId 테스트 화면
 *
 * 알고 싶은 것: 관광공사에서 받은 장소(이름 + 좌표만 있고 내비 poiId는 없음)로
 *              차량 내비 길안내를 시작할 수 있는가?
 *
 * 테스트 A: poiId 빈 값("") + 서울숲 좌표      ← 이게 되면 성공!
 * 테스트 B: poiId 가짜 값("TEST123") + 서울숲 좌표
 * 테스트 C: Pleos 문서 예시(42dot, 진짜 poiId)  ← 비교용. 이건 돼야 정상
 * 테스트 D: 내비 검색 화면에 "서울숲" 검색       ← A가 안 될 때 쓸 대안
 *
 * 결과는 화면 아래 로그와 Logcat("NaviTest"로 필터)에서 확인해요.
 */
class NaviTestActivity : ComponentActivity() {

    private val logs = mutableStateListOf<String>()
    private var connected by mutableStateOf(false)
    private var navi: NaviHelper? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    // 마지막으로 요청한 좌표 (내비가 실제로 같은 곳으로 안내하는지 비교용)
    private var lastRequest: Pair<Double, Double>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GatigagaeTheme {
                NaviTestContent()
            }
        }
    }

    override fun onDestroy() {
        runCatching { navi?.removeListener(listener) }
        runCatching { navi?.release() }
        super.onDestroy()
    }

    // ───────────────────────── 로그 남기기 ─────────────────────────
    private fun log(message: String) {
        Log.d(TAG, message)
        val time = SimpleDateFormat("HH:mm:ss", Locale.KOREA).format(Date())
        // 내비 콜백은 다른 스레드에서 올 수 있어서 화면 갱신은 메인 스레드에서 해요
        mainHandler.post { logs.add(0, "[$time] $message") }
    }

    // ───────────────────────── 내비가 보내주는 이벤트 ─────────────────────────
    private val listener = object : NaviHelperEventListener {
        override fun onRouteStateInfo(routeStateInfo: RouteStateInfo) {
            log("안내 상태: mode=${routeStateInfo.naviMode}, 탐색중=${routeStateInfo.isRouting}, 약관동의=${routeStateInfo.termsAgreement}")
        }

        override fun onDestinationInfo(destinationInfo: DestinationInfo) {
            log("현재 목적지: ${destinationInfo.name} / ${destinationInfo.latitude}, ${destinationInfo.longitude} / poiId='${destinationInfo.poiId}'")
            val request = lastRequest
            if (request != null) {
                val diffLat = abs(destinationInfo.latitude - request.first)
                val diffLng = abs(destinationInfo.longitude - request.second)
                if (diffLat < 0.001 && diffLng < 0.001) {
                    log("→ 요청한 좌표와 일치 ✅")
                } else {
                    log("→ 요청한 좌표와 다름 ⚠ (위도 차이 ${"%.4f".format(diffLat)}, 경도 차이 ${"%.4f".format(diffLng)})")
                }
            }
        }

        override fun onDestinationChanged() {
            log("이벤트: 목적지 변경됨 → 현재 목적지 다시 물어봄")
            runCatching { navi?.getDestinationInfo() }
        }

        override fun onRouteStarted(routeStartInfo: RouteStartInfo) {
            log("이벤트: 안내 시작 ✅ (거리 ${routeStartInfo.distance}m, 예상 ${routeStartInfo.duration}초)")
        }

        override fun onRoutingCompleted() {
            log("이벤트: 경로 탐색 완료 ✅")
        }

        override fun onRouteCancelled() {
            log("이벤트: 안내 취소됨")
        }

        override fun onDestinationArrived(destinationArrivedInfo: DestinationArrivedInfo) {
            log("이벤트: 목적지 도착")
        }

        override fun onError(errorCode: NaviErrorCode) {
            log("❌ 내비 오류: ${errorCode.name}")
        }
    }

    // ───────────────────────── 버튼 동작 ─────────────────────────
    private fun connect() {
        if (navi != null) {
            log("이미 연결했어요 (상태: ${if (connected) "연결됨" else "응답 기다리는 중"})")
            return
        }
        log("NaviHelper 연결 시도…")
        val helper = NaviHelper(applicationContext)
        navi = helper
        try {
            helper.initialize(
                {
                    // 연결 성공
                    mainHandler.post { connected = true }
                    helper.addListener(listener)
                    log("✅ 내비 연결 성공 (onConnected)")
                    helper.getRouteStateInfo()
                },
                { error ->
                    // 연결 실패
                    log("❌ 내비 연결 실패 (onFailed): ${error.javaClass.simpleName} ${error.message ?: ""}")
                    navi = null
                },
            )
        } catch (e: Exception) {
            log("❌ initialize 예외: ${e.javaClass.simpleName} ${e.message ?: ""}")
            navi = null
        }
    }

    private fun requestRoute(label: String, name: String, lat: Double, lng: Double, poiId: String, address: String) {
        val helper = navi
        if (helper == null || !connected) {
            log("먼저 '① 내비 연결'을 눌러 주세요")
            return
        }
        lastRequest = Pair(lat, lng)
        log("$label 요청 → $name ($lat, $lng) / poiId='$poiId'")
        try {
            helper.requestRoute(
                RouteInfo(
                    latitude = lat,
                    longitude = lng,
                    poiName = name,
                    poiId = poiId,
                    poiSubId = "0",
                    address = address,
                )
            )
            log("requestRoute 호출 끝. 결과는 아래 '안내 시작' / '오류' 이벤트로 판단해요")
        } catch (e: Exception) {
            log("❌ requestRoute 예외: ${e.javaClass.simpleName} ${e.message ?: ""}")
        }
    }

    private fun runIfConnected(action: (NaviHelper) -> Unit) {
        val helper = navi
        if (helper == null || !connected) {
            log("먼저 '① 내비 연결'을 눌러 주세요")
            return
        }
        try {
            action(helper)
        } catch (e: Exception) {
            log("❌ 예외: ${e.javaClass.simpleName} ${e.message ?: ""}")
        }
    }

    // ───────────────────────── 화면 ─────────────────────────
    @Composable
    private fun NaviTestContent() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp)
        ) {
            Text("poiId 테스트 (NaviHelper)", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(
                text = if (connected) "● 내비 연결됨" else "○ 내비 연결 안 됨",
                color = if (connected) Color(0xFF2B8A3E) else Color(0xFF868E96),
                fontSize = 18.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "순서: ① 연결 → ② 테스트 A → 내비 화면에 서울숲 경로가 뜨는지 보기 → 이 앱으로 돌아오기 → ⑥ 현재 목적지 확인",
                fontSize = 15.sp,
                color = Color(0xFF6B7280),
            )
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TestButton("① 내비 연결", Modifier.weight(1f)) { connect() }
                TestButton("② A: poiId 빈 값", Modifier.weight(1f)) {
                    requestRoute("테스트 A", SEOUL_FOREST_NAME, SEOUL_FOREST_LAT, SEOUL_FOREST_LNG, "", SEOUL_FOREST_ADDRESS)
                }
                TestButton("③ B: 가짜 poiId", Modifier.weight(1f)) {
                    requestRoute("테스트 B", SEOUL_FOREST_NAME, SEOUL_FOREST_LAT, SEOUL_FOREST_LNG, "TEST123", SEOUL_FOREST_ADDRESS)
                }
                TestButton("④ C: 문서 예시", Modifier.weight(1f)) {
                    requestRoute("테스트 C", "42dot", 37.48544722, 127.03636666, "36khp37hh8gqcm5nqhe5", "서울시 강남구 남부순환로 2621")
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TestButton("⑤ D: 내비 검색", Modifier.weight(1f)) {
                    runIfConnected {
                        log("테스트 D: 내비 검색 화면에 '$SEOUL_FOREST_NAME' 검색 요청")
                        it.requestNavigationScreen(NavigationScreenRequest.SearchPlace(SEOUL_FOREST_NAME))
                    }
                }
                TestButton("⑥ 현재 목적지 확인", Modifier.weight(1f)) {
                    runIfConnected {
                        log("현재 목적지 물어보는 중…")
                        it.getDestinationInfo()
                    }
                }
                TestButton("⑦ 안내 취소", Modifier.weight(1f)) {
                    runIfConnected {
                        log("안내 취소 요청")
                        it.cancelRoute()
                    }
                }
                TestButton("로그 지우기", Modifier.weight(1f)) { logs.clear() }
            }
            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F3F5))
                    .padding(12.dp)
            ) {
                items(logs) { line ->
                    Text(line, fontSize = 15.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }

    @Composable
    private fun TestButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
        Button(onClick = onClick, modifier = modifier.height(56.dp)) {
            Text(text, fontSize = 15.sp)
        }
    }

    private companion object {
        const val TAG = "NaviTest"

        // 테스트 목적지: 서울숲 (반려견 동반 가능한 공원, 관광공사 데이터에 있을 만한 장소)
        const val SEOUL_FOREST_NAME = "서울숲"
        const val SEOUL_FOREST_LAT = 37.544389
        const val SEOUL_FOREST_LNG = 127.037442
        const val SEOUL_FOREST_ADDRESS = "서울특별시 성동구 뚝섬로 273"
    }
}
