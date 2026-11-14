package com.gatigagae.app.sdk

import ai.pleos.playground.stt.SpeechToText
import ai.pleos.playground.stt.listener.ResultListener
import ai.pleos.playground.tts.TextToSpeech
import ai.pleos.playground.tts.constant.ErrorType
import ai.pleos.playground.tts.constant.Mode
import ai.pleos.playground.tts.listener.EventListener
import ai.pleos.playground.tts.listener.OnInitializeListener
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.Locale
import ai.pleos.playground.stt.constant.ErrorType as SttErrorType
import ai.pleos.playground.stt.constant.Mode as SttMode
import ai.pleos.playground.stt.listener.OnInitializeListener as SttInitListener

/**
 * 말하기(TTS) + 듣기(STT)를 앱 전체에서 같이 쓰는 곳
 * 음성 테스트 화면(A-5, A-6)에서 확인한 방법 그대로예요.
 *
 * 쓰는 법
 *   voice.speak("콩이랑 같이 타나요?") { 다 말한 뒤 할 일 }
 *   voice.listen(
 *       onPartial = { 말하는 중간 글자 },
 *       onResult = { 최종 문장 },
 *       onFail = { 못 알아들음 / 시간 초과 },
 *   )
 *
 * 콜백은 전부 메인 스레드에서 불려요 (화면 상태를 바로 바꿔도 돼요).
 * 엔진이 준비 안 됐으면 speak는 바로 onDone, listen은 바로 onFail을 불러요.
 */
class VoiceController(context: Context) {

    private val appContext = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private val audioManager = appContext.getSystemService(AudioManager::class.java)

    private var tts: TextToSpeech? = null
    private var stt: SpeechToText? = null
    var ttsReady = false
        private set
    var sttReady = false
        private set

    // ── 말하기 ──
    // USAGE_ASSISTANT = 음성 비서 소리 (시스템 효과음보다 크게 들려요)
    private val speechAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANT)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()
    private val speechFocus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        .setAudioAttributes(speechAttributes)
        .build()
    private var onSpeakDone: (() -> Unit)? = null

    // ── 듣기 ──
    private val listenFocus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        .setAudioAttributes(speechAttributes)
        .build()
    private var session = 0               // 듣기 요청마다 번호. 예전 요청의 늦은 콜백은 무시
    private var delivered = true          // 이번 듣기에서 결과(성공/실패)를 이미 알려줬는지
    private var lastPartial = ""
    private var onPartial: ((String) -> Unit)? = null
    private var onResult: ((String) -> Unit)? = null
    private var onFail: (() -> Unit)? = null

    /** 앱 시작할 때 한 번 */
    fun start() {
        setUpTts()
        setUpStt()
    }

    /** 앱 끝날 때 한 번 */
    fun release() {
        handler.removeCallbacksAndMessages(null)
        runCatching { tts?.removeEventListener(ttsEvents) }
        runCatching { tts?.release() }
        runCatching { stt?.removeListener(sttListener) }
        runCatching { stt?.release() }
        audioManager.abandonAudioFocusRequest(speechFocus)
        audioManager.abandonAudioFocusRequest(listenFocus)
    }

    // ───────────────────────── 말하기 ─────────────────────────

    fun speak(text: String, onDone: () -> Unit = {}) = speak(text, onDone, waitedMs = 0L)

    private fun speak(text: String, onDone: () -> Unit, waitedMs: Long) {
        val engine = tts
        if (engine == null || !ttsReady) {
            // 앱을 막 켰을 때는 엔진 준비에 몇 초 걸려요 → 최대 8초 기다렸다가 말하기
            if (engine != null && waitedMs < READY_WAIT_MS) {
                handler.postDelayed({ speak(text, onDone, waitedMs + 300) }, 300)
            } else {
                log("TTS 준비 전이라 말하기 건너뜀: $text")
                handler.post(onDone)
            }
            return
        }
        audioManager.requestAudioFocus(speechFocus)
        onSpeakDone = onDone
        log("말하기: $text")
        try {
            engine.speak(text, true)
        } catch (e: Exception) {
            log("speak 예외: ${e.message}")
            finishSpeaking()
        }
    }

    private fun finishSpeaking() {
        audioManager.abandonAudioFocusRequest(speechFocus)
        val done = onSpeakDone
        onSpeakDone = null
        if (done != null) handler.post(done)
    }

    private val ttsEvents = object : EventListener {
        override fun onStart() {}
        override fun onReady() {}
        override fun onDone() { finishSpeaking() }
        override fun onError(errorType: ErrorType) {
            log("말하기 오류: $errorType")
            finishSpeaking()
        }
        override fun onStop() { finishSpeaking() }
        override fun onUpdatedRms(rms: Double) {}
    }

    private fun setUpTts() {
        try {
            val engine = TextToSpeech(appContext, Mode.ON_DEVICE)
            engine.initialize(object : OnInitializeListener {
                override fun onSuccess() {
                    try {
                        engine.setLocale(Locale.KOREA)
                        engine.setAudioAttributes(speechAttributes)
                        engine.addEventListener(ttsEvents)
                        ttsReady = true
                        log("TTS 준비 완료")
                    } catch (e: Exception) {
                        log("TTS 설정 실패: ${e.message}")
                    }
                }
                override fun onFailure(errorType: ErrorType) { log("TTS 준비 실패: $errorType") }
            })
            tts = engine
        } catch (e: Exception) {
            log("TTS 만들기 실패: ${e.message}")
        }
    }

    // ───────────────────────── 듣기 ─────────────────────────

    fun listen(
        onPartial: (String) -> Unit = {},
        onResult: (String) -> Unit,
        onFail: () -> Unit,
    ) {
        stopListening()
        listenWhenReady(onPartial, onResult, onFail, waitedMs = 0L, myWait = ++waitToken)
    }

    private var waitToken = 0 // stopListening/새 listen이 오면 기다리던 요청은 취소

    private fun listenWhenReady(
        onPartial: (String) -> Unit,
        onResult: (String) -> Unit,
        onFail: () -> Unit,
        waitedMs: Long,
        myWait: Int,
    ) {
        if (myWait != waitToken) return
        val engine = stt
        if (engine == null || !sttReady) {
            // 엔진 준비 전이면 최대 8초 기다리기
            if (engine != null && waitedMs < READY_WAIT_MS) {
                handler.postDelayed({ listenWhenReady(onPartial, onResult, onFail, waitedMs + 300, myWait) }, 300)
            } else {
                log("STT 준비 전이라 듣기 실패 처리")
                handler.post(onFail)
            }
            return
        }
        session += 1
        delivered = false
        lastPartial = ""
        this.onPartial = onPartial
        this.onResult = onResult
        this.onFail = onFail

        audioManager.requestAudioFocus(listenFocus)
        val mySession = session
        // 엔진 준비까지 최대 20초 (처음 켤 때 오래 걸릴 수 있어요)
        handler.postDelayed({ timeout(mySession) }, PREPARE_TIMEOUT_MS)
        try {
            engine.request()
            log("듣기 요청")
        } catch (e: Exception) {
            log("request 예외: ${e.message}")
            deliver(null)
        }
    }

    /** 듣기 그만 (결과 콜백도 안 불러요). 화면을 떠날 때 꼭 불러 주세요 */
    fun stopListening() {
        waitToken += 1
        if (!delivered) {
            delivered = true
            runCatching { stt?.stop() }
        }
        clearListenCallbacks()
        audioManager.abandonAudioFocusRequest(listenFocus)
    }

    private fun timeout(mySession: Int) {
        if (mySession != session || delivered) return
        log("듣기 시간 초과")
        runCatching { stt?.stop() }
        deliver(lastPartial.ifBlank { null })
    }

    /** 결과를 딱 한 번만 알려주기. text가 null이면 실패 */
    private fun deliver(text: String?) {
        if (delivered) return
        delivered = true
        audioManager.abandonAudioFocusRequest(listenFocus)
        val result = onResult
        val fail = onFail
        clearListenCallbacks()
        handler.post {
            if (text != null && text.isNotBlank()) result?.invoke(text) else fail?.invoke()
        }
    }

    private fun clearListenCallbacks() {
        onPartial = null
        onResult = null
        onFail = null
    }

    private val sttListener = object : ResultListener {
        override fun onReady() {
            // 준비 끝 → 이제부터 10초 안에 말해야 해요
            val mySession = session
            handler.post {
                handler.postDelayed({ timeout(mySession) }, LISTEN_TIMEOUT_MS)
            }
        }
        override fun onStartedRecognition() {}
        override fun onUpdatedEpdData(on: Long, off: Long) {}
        override fun onUpdatedRms(rms: Double) {}
        override fun onUpdated(stt: String, completed: Boolean) {
            handler.post {
                if (delivered) return@post
                if (stt.isNotBlank()) {
                    lastPartial = stt
                    onPartial?.invoke(stt)
                }
                if (completed) deliver(stt)
            }
        }
        override fun onEndedRecognition() {
            // 테스트해 보니 "듣기 끝"이 최종 결과보다 먼저 와요 → 1.5초 기다렸다가 그래도 없으면 마지막 중간 글자로
            val mySession = session
            handler.postDelayed({
                if (mySession == session && !delivered) deliver(lastPartial.ifBlank { null })
            }, 1_500)
        }
        override fun onError(errorType: SttErrorType) {
            log("인식 오류: $errorType")
            handler.post { deliver(null) }
        }
    }

    private fun setUpStt() {
        try {
            val engine = SpeechToText(appContext, SttMode.ON_DEVICE)
            engine.initialize(object : SttInitListener {
                override fun onSuccess() {
                    try {
                        engine.setLocale(Locale.KOREA)
                        engine.addListener(sttListener)
                        sttReady = true
                        log("STT 준비 완료")
                    } catch (e: Exception) {
                        log("STT 설정 실패: ${e.message}")
                    }
                }
                override fun onFailure(errorType: SttErrorType) { log("STT 준비 실패: $errorType") }
            })
            stt = engine
        } catch (e: Exception) {
            log("STT 만들기 실패: ${e.message}")
        }
    }

    private fun log(message: String) {
        Log.d(TAG, message)
    }

    private companion object {
        const val TAG = "Voice"
        const val LISTEN_TIMEOUT_MS = 10_000L
        const val PREPARE_TIMEOUT_MS = 20_000L
        const val READY_WAIT_MS = 8_000L
    }
}
