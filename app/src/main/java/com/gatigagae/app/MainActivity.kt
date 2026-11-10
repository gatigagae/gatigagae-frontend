package com.gatigagae.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.gatigagae.app.sdk.CarLocation
import com.gatigagae.app.sdk.NaviController
import com.gatigagae.app.sdk.VoiceController
import com.gatigagae.app.ui.theme.GatigagaeTheme

class MainActivity : ComponentActivity() {

    // 차량 SDK (말하기·듣기, 내비). 앱이 켜져 있는 동안 하나씩만 만들어서 같이 써요.
    private lateinit var voice: VoiceController
    private lateinit var navi: NaviController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        voice = VoiceController(this).also { it.start() }
        navi = NaviController(this).also { it.connect() }
        // 장소 검색 기준 위치 권한 (처음 한 번만 물어봐요. 거절해도 기본 위치로 검색돼요)
        if (!CarLocation.hasPermission(this)) {
            requestPermissions(CarLocation.permissions, 1)
        }
        setContent {
            GatigagaeTheme {
                GatigagaeApp(voice = voice, navi = navi)
            }
        }
    }

    override fun onDestroy() {
        voice.release()
        navi.release()
        super.onDestroy()
    }
}

/** 앱 전체 미리보기 (Connect-S-17 앱 영역 크기). 미리보기에선 SDK 없이 가짜로 움직여요 */
@Preview(widthDp = 1700, heightDp = 1184)
@Composable
private fun AppPreview() {
    GatigagaeTheme {
        GatigagaeApp()
    }
}
