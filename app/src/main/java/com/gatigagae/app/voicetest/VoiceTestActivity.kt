package com.gatigagae.app.voicetest

import ai.pleos.playground.stt.SpeechToText
import ai.pleos.playground.stt.listener.ResultListener
import ai.pleos.playground.tts.TextToSpeech
import ai.pleos.playground.tts.constant.ErrorType
import ai.pleos.playground.tts.constant.Mode
import ai.pleos.playground.tts.listener.EventListener
import ai.pleos.playground.tts.listener.OnInitializeListener
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
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
import com.gatigagae.app.data.VoiceRequestParser
import com.gatigagae.app.ui.theme.GatigagaeTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import ai.pleos.playground.stt.constant.ErrorType as SttErrorType
import ai.pleos.playground.stt.constant.Mode as SttMode
import ai.pleos.playground.stt.listener.OnInitializeListener as SttInitListener

/**
 * 음성 테스트 화면 (A-5, A-6)
 *
 * A-5: 앱이 화면에 안 보일 때도 말할 수 있나?  → "10초 뒤 말하기" 누르고 바로 앱 숨기기
 * A-6: 음성 인식이 되나? "괜찮아"도 알아듣나?    → "듣기" 누르고 말하기
 *
 * ⚠ 에뮬레이터는 PC 마이크를 켜야 들려요:  adb emu avd hostmicon
 *   (켜 두면 주변 소리도 다 들어가요. 테스트 끝나면 adb emu avd hostmicoff)
 */
class VoiceTestActivity : ComponentActivity() {

    private val logs = mutableStateListOf<String>()
    private var ttsReady by mutableStateOf(false)
    private var sttReady by mutableStateOf(false)
    private var listening by mutableStateOf(false)
    private var partialText by mutableStateOf("")
    private var micLevel by mutableStateOf(0.0) // 마이크 소리 크기 (듣는 중 실시간)

    // 마이크 진단용: 이번 듣기에서 소리 신호를 몇 번 받았고, 가장 큰 값이 얼마였는지
    private var rmsCount = 0
    private var maxRms = -100.0
    private var micReported = true

    private var tts: TextToSpeech? = null
    private var stt: SpeechToText? = null
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var audioManager: AudioManager

    // 말할 때: 라디오 소리를 잠깐 줄이고(duck) 말하기
    // USAGE_ASSISTANT = 음성 비서 소리 (예전 SONIFICATION은 "시스템 효과음" 볼륨이라 작게 들렸어요)
    private val speechAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANT)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()
    private val speechFocus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        .setAudioAttributes(speechAttributes)
        .build()

    // 들을 때: 다른 소리를 잠깐 멈추고 듣기
    private val listenFocus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )
        .build()

    // 말을 안 끝내도 10초 지나면 마이크 끄기
    private val listenTimeout = Runnable {
        log("⏱ 10초 지나서 듣기 멈춤")
        stopListening()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioManager = getSystemService(AudioManager::class.java)
        setContent {
            GatigagaeTheme {
                VoiceTestContent()
            }
        }
        setUpTts()
        setUpStt()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        runCatching { tts?.removeEventListener(ttsEvents) }
        runCatching { tts?.release() }
        runCatching { stt?.removeListener(sttListener) }
        runCatching { stt?.release() }
        audioManager.abandonAudioFocusRequest(speechFocus)
        audioManager.abandonAudioFocusRequest(listenFocus)
        super.onDestroy()
    }

    private fun log(message: String) {
        Log.d(TAG, message)
        val time = SimpleDateFormat("HH:mm:ss", Locale.KOREA).format(Date())
        handler.post { logs.add(0, "[$time] $message") }
    }

    // ───────────────────────── TTS (말하기) ─────────────────────────
    private val ttsEvents = object : EventListener {
        override fun onStart() { log("🔊 말하기 시작") }
        override fun onReady() {}
        override fun onDone() {
            log("🔊 말하기 끝")
            audioManager.abandonAudioFocusRequest(speechFocus)
        }
        override fun onError(errorType: ErrorType) {
            log("❌ 말하기 오류: $errorType")
            audioManager.abandonAudioFocusRequest(speechFocus)
        }
        override fun onStop() { audioManager.abandonAudioFocusRequest(speechFocus) }
        override fun onUpdatedRms(rms: Double) {}
    }

    private fun setUpTts() {
        log("TTS 준비 중…")
        try {
            val engine = TextToSpeech(applicationContext, Mode.ON_DEVICE)
            engine.initialize(object : OnInitializeListener {
                override fun onSuccess() {
                    try {
                        engine.setLocale(Locale.KOREA) // Locale.KOREAN이 아니라 KOREA (지역까지 있어야 해요)
                        engine.setAudioAttributes(speechAttributes)
                        engine.addEventListener(ttsEvents)
                        handler.post { ttsReady = true }
                        log("✅ TTS 준비 완료")
                    } catch (e: Exception) {
                        log("❌ TTS 설정 실패: ${e.javaClass.simpleName} ${e.message ?: ""}")
                    }
                }
                override fun onFailure(errorType: ErrorType) {
                    log("❌ TTS 준비 실패: $errorType")
                }
            })
            tts = engine
        } catch (e: Exception) {
            log("❌ TTS 만들기 실패: ${e.javaClass.simpleName} ${e.message ?: ""}")
        }
    }

    private fun speak(text: String) {
        val engine = tts
        if (engine == null || !ttsReady) {
            log("TTS가 아직 준비 안 됐어요")
            return
        }
        if (audioManager.requestAudioFocus(speechFocus) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            log("❌ 오디오 포커스를 못 받았어요")
            return
        }
        log("말하기 요청: \"$text\"")
        try {
            engine.speak(text, true)
        } catch (e: Exception) {
            log("❌ speak 예외: ${e.javaClass.simpleName} ${e.message ?: ""}")
            audioManager.abandonAudioFocusRequest(speechFocus)
        }
    }

    // ───────────────────────── STT (듣기) ─────────────────────────
    private val sttListener = object : ResultListener {
        override fun onReady() {
            log("🎤 듣기 준비 완료 — 지금 말하세요")
            handler.removeCallbacks(listenTimeout)
            handler.postDelayed(listenTimeout, LISTEN_TIMEOUT_MS)
        }
        override fun onStartedRecognition() {
            log("🗣 말소리를 감지했어요 (인식 시작)")
        }
        override fun onUpdatedEpdData(on: Long, off: Long) {}
        override fun onUpdatedRms(rms: Double) {
            rmsCount += 1
            if (rms > maxRms) maxRms = rms
            handler.post { micLevel = rms }
        }
        override fun onUpdated(stt: String, completed: Boolean) {
            handler.post { partialText = stt }
            if (completed && stt.isNotBlank()) {
                log("✅ 인식 결과: \"$stt\"")
                explain(stt)
            }
        }
        override fun onEndedRecognition() {
            log("🎤 듣기 끝")
            reportMic()
            handler.removeCallbacks(listenTimeout)
            audioManager.abandonAudioFocusRequest(listenFocus)
            handler.post { listening = false }
        }
        override fun onError(errorType: SttErrorType) {
            log("❌ 인식 오류: $errorType")
            reportMic()
            handler.removeCallbacks(listenTimeout)
            audioManager.abandonAudioFocusRequest(listenFocus)
            handler.post { listening = false }
        }
    }

    private fun setUpStt() {
        log("STT 준비 중…")
        try {
            val engine = SpeechToText(applicationContext, SttMode.ON_DEVICE)
            engine.initialize(object : SttInitListener {
                override fun onSuccess() {
                    try {
                        engine.setLocale(Locale.KOREA)
                        engine.addListener(sttListener)
                        handler.post { sttReady = true }
                        log("✅ STT 준비 완료")
                    } catch (e: Exception) {
                        log("❌ STT 설정 실패: ${e.javaClass.simpleName} ${e.message ?: ""}")
                    }
                }
                override fun onFailure(errorType: SttErrorType) {
                    log("❌ STT 준비 실패: $errorType")
                }
            })
            stt = engine
        } catch (e: Exception) {
            log("❌ STT 만들기 실패: ${e.javaClass.simpleName} ${e.message ?: ""}")
        }
    }

    private fun startListening() {
        val engine = stt
        if (engine == null || !sttReady) {
            log("STT가 아직 준비 안 됐어요")
            return
        }
        if (audioManager.requestAudioFocus(listenFocus) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            log("❌ 오디오 포커스를 못 받았어요 (다른 소리가 나는 중)")
            return
        }
        partialText = ""
        listening = true
        rmsCount = 0
        maxRms = -100.0
        micLevel = 0.0
        micReported = false
        log("듣기 요청… (처음엔 준비에 몇 초 걸릴 수 있어요)")
        // 엔진이 준비되기까지 최대 20초 기다리기
        handler.postDelayed(listenTimeout, PREPARE_TIMEOUT_MS)
        try {
            engine.request()
        } catch (e: Exception) {
            log("❌ request 예외: ${e.javaClass.simpleName} ${e.message ?: ""}")
            stopListening()
        }
    }

    private fun stopListening() {
        reportMic()
        handler.removeCallbacks(listenTimeout)
        runCatching { stt?.stop() }
        audioManager.abandonAudioFocusRequest(listenFocus)
        listening = false
    }

    /**
     * 듣기가 끝날 때 마이크 진단 한 줄 남기기
     * - 신호 0번 → 엔진이 마이크 소리를 아예 못 받음 (hostmicon·윈도우 마이크 권한 확인)
     * - 신호는 왔는데 말할 때도 값이 거의 안 변함 → PC 마이크 소리가 안 들어옴
     * - 값이 크게 변했는데 결과가 없음 → 마이크는 OK, 인식 엔진 쪽 문제
     */
    private fun reportMic() {
        if (micReported) return
        micReported = true
        val max = if (rmsCount == 0) "-" else String.format(Locale.KOREA, "%.1f", maxRms)
        log("🎚 마이크 신호 ${rmsCount}번 받음 · 가장 큰 값 $max")
    }

    /** 들은 문장을 우리 앱 규칙(VoiceRequestParser)으로 해석해서 보여주기 */
    private fun explain(text: String) {
        val request = VoiceRequestParser.parse(text)
        log("→ 해석: 종류=${request.category ?: "?"}, 범위=${request.range}, 의도=${request.intent ?: "-"}")
        val choice = VoiceRequestParser.parseChoice(text)
        if (choice != null) log("→ 장소 선택: ${choice + 1}번째")
        when {
            VoiceRequestParser.isDecline(text) -> log("→ 대답: 거절 (아니/괜찮아)")
            VoiceRequestParser.isAccept(text) -> log("→ 대답: 수락 (응/네)")
        }
    }

    // ───────────────────────── 화면 ─────────────────────────
    @Composable
    private fun VoiceTestContent() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp)
        ) {
            Text("음성 테스트 (TTS·STT)", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "TTS ${if (ttsReady) "● 준비됨" else "○ 준비 중"}   ·   STT ${if (sttReady) "● 준비됨" else "○ 준비 중"}" +
                    if (listening) "   ·   🎤 듣는 중" else "",
                fontSize = 18.sp,
                color = Color(0xFF495057),
            )
            if (listening) {
                // 말하면 막대가 길어져야 정상. 계속 0이면 마이크 소리가 안 들어오는 것
                val bars = micLevel.coerceIn(0.0, 10.0).toInt()
                Text(
                    text = "🎚 마이크 소리 ${"▮".repeat(bars)}${"▯".repeat(10 - bars)}  (${String.format(Locale.KOREA, "%.1f", micLevel)})",
                    fontSize = 18.sp,
                    color = Color(0xFF3359D6),
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "A-5: '10초 뒤 말하기' 누르고 바로 앱을 숨기세요(홈·내비 화면으로). 10초 뒤 소리가 나면 성공\n" +
                    "A-6: PC 마이크를 켜고(adb emu avd hostmicon) '듣기' 누른 뒤 말하기",
                fontSize = 15.sp,
                color = Color(0xFF6B7280),
            )
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TestButton("🔊 지금 말하기", Modifier.weight(1f)) {
                    speak("콩이랑 달린 지 2시간이 넘었어요. 콩이와 운전자를 위해 잠시 쉬어 가요.")
                }
                TestButton("🔊 10초 뒤 말하기", Modifier.weight(1f)) {
                    log("10초 뒤에 말할게요. 지금 앱을 숨기세요!")
                    handler.postDelayed({ speak("근처에 콩이랑 갈 수 있는 휴게소를 찾아드릴까요?") }, 10_000L)
                }
                TestButton(if (listening) "🎤 듣기 멈추기" else "🎤 듣기", Modifier.weight(1f)) {
                    if (listening) stopListening() else startListening()
                }
                TestButton("로그 지우기", Modifier.weight(1f)) { logs.clear() }
            }
            Spacer(Modifier.height(12.dp))

            // 지금 들리는 말 (중간 결과)
            Text(
                text = if (partialText.isBlank()) "…" else "\"$partialText\"",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "말해볼 문장: 근처에 콩이랑 갈 수 있는 카페 찾아줘 / 첫 번째로 가줘 / 카페에 곧 도착하니 괜찮아 / 응",
                fontSize = 15.sp,
                color = Color(0xFF6B7280),
            )
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
        const val TAG = "VoiceTest"
        const val LISTEN_TIMEOUT_MS = 10_000L
        const val PREPARE_TIMEOUT_MS = 20_000L // 처음 켤 때 엔진 준비에 9초쯤 걸린 기록이 있어요
    }
}
