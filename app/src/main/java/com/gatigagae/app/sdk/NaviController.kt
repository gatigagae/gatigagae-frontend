package com.gatigagae.app.sdk

import ai.pleos.playground.navi.constants.NaviErrorCode
import ai.pleos.playground.navi.data.DestinationArrivedInfo
import ai.pleos.playground.navi.data.DestinationInfo
import ai.pleos.playground.navi.data.RouteInfo
import ai.pleos.playground.navi.data.RouteStartInfo
import ai.pleos.playground.navi.data.RouteStateInfo
import ai.pleos.playground.navi.helper.NaviHelper
import ai.pleos.playground.navi.helper.listener.NaviHelperEventListener
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.gatigagae.app.data.Place

/**
 * 차량 내비(NaviHelper) 연결 — poiId 테스트(A-1)에서 확인한 방법 그대로예요.
 * 결과: poiId를 비워 두고("") 좌표 + 이름만 보내도 길안내가 시작돼요.
 *
 * onRouteActiveChanged: 안내 중인지 바뀔 때 (true = 안내 중)
 * onDestination: 내비 목적지가 바뀌었을 때 (S5 동반 여부 확인에 쓸 예정)
 * 콜백은 전부 메인 스레드에서 불려요.
 */
class NaviController(context: Context) {

    private val appContext = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private var helper: NaviHelper? = null
    private var connecting = false

    var connected = false
        private set

    var onRouteActiveChanged: ((Boolean) -> Unit)? = null
    var onDestination: ((DestinationInfo) -> Unit)? = null

    /** 앱 시작할 때 한 번. 실패하면 길안내를 누를 때 다시 시도해요 */
    fun connect() {
        if (connected || connecting) return
        connecting = true
        val navi = NaviHelper(appContext)
        try {
            navi.initialize(
                {
                    connecting = false
                    connected = true
                    helper = navi
                    navi.addListener(listener)
                    log("내비 연결 성공")
                    runCatching { navi.getRouteStateInfo() } // 이미 안내 중인지 물어보기
                },
                { error ->
                    connecting = false
                    connected = false
                    log("내비 연결 실패: ${error.message}")
                },
            )
        } catch (e: Exception) {
            connecting = false
            log("내비 initialize 예외: ${e.message}")
        }
    }

    /**
     * 길안내 요청. 연결이 안 돼 있으면 false (다시 연결 시도도 해요)
     * 진짜로 안내가 시작됐는지는 onRouteActiveChanged(true)로 알 수 있어요.
     */
    fun requestRoute(place: Place): Boolean {
        val navi = helper
        if (navi == null || !connected) {
            connect()
            return false
        }
        return try {
            navi.requestRoute(
                RouteInfo(
                    latitude = place.lat,
                    longitude = place.lng,
                    poiName = place.name,
                    poiId = "",       // 관광공사 장소는 내비 poiId가 없어요 (A-1에서 빈 값으로 성공)
                    poiSubId = "0",
                    address = place.address,
                )
            )
            log("길안내 요청: ${place.name} (${place.lat}, ${place.lng})")
            true
        } catch (e: Exception) {
            log("requestRoute 예외: ${e.message}")
            false
        }
    }

    fun cancelRoute() {
        runCatching { helper?.cancelRoute() }
    }

    fun release() {
        handler.removeCallbacksAndMessages(null)
        runCatching { helper?.removeListener(listener) }
        runCatching { helper?.release() }
        helper = null
        connected = false
    }

    private fun routeActive(active: Boolean) {
        handler.post { onRouteActiveChanged?.invoke(active) }
    }

    private val listener = object : NaviHelperEventListener {
        override fun onRouteStateInfo(routeStateInfo: RouteStateInfo) {
            routeActive(routeStateInfo.isRouting)
        }
        override fun onDestinationInfo(destinationInfo: DestinationInfo) {
            log("현재 목적지: ${destinationInfo.name}")
            handler.post { onDestination?.invoke(destinationInfo) }
        }
        override fun onDestinationChanged() {
            runCatching { helper?.getDestinationInfo() }
        }
        override fun onRouteStarted(routeStartInfo: RouteStartInfo) {
            log("안내 시작")
            routeActive(true)
        }
        override fun onRoutingCompleted() {}
        override fun onRouteCancelled() {
            log("안내 취소")
            routeActive(false)
        }
        override fun onDestinationArrived(destinationArrivedInfo: DestinationArrivedInfo) {
            log("목적지 도착")
            routeActive(false)
        }
        override fun onError(errorCode: NaviErrorCode) {
            log("내비 오류: ${errorCode.name}")
        }
    }

    private fun log(message: String) {
        Log.d(TAG, message)
    }

    private companion object {
        const val TAG = "Navi"
    }
}
