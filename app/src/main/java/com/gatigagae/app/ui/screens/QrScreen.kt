package com.gatigagae.app.ui.screens

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gatigagae.app.data.DogProfile
import com.gatigagae.app.data.ProfileRepository
import com.gatigagae.app.ui.components.GgDialog
import com.gatigagae.app.ui.components.GoldButton
import com.gatigagae.app.ui.theme.AlertRed
import com.gatigagae.app.ui.theme.Divider10
import com.gatigagae.app.ui.theme.TextSecondary
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.delay

/** QR 상태 확인 간격 */
private const val POLL_INTERVAL_MS = 3_000L

/** QR 점 색 (Figma: 진한 골드 #2E1E00) */
private const val QR_DARK = 0xFF2E1E00.toInt()

/**
 * S2-3 프로필 수정 QR 팝업 (Figma "프로필 수정" 344:15855: 메인 위 팝업, X 닫기, QR 170dp 박스)
 *
 * 1. 서버에서 QR 주소를 받아 QR 그림으로 그려요 (10분 유효)
 * 2. 3초마다 서버에 "저장했나요?" 물어봐요
 * 3. 저장(COMPLETED)되면 반려견 정보를 받아서 onDone
 * 4. 시간이 지나면(EXPIRED) [새 QR 받기]
 *
 * repository가 null이면(미리보기) 가짜 QR만 보여줘요.
 */
@Composable
fun QrDialog(
    repository: ProfileRepository?,
    onDone: (DogProfile?) -> Unit,
    onCancel: () -> Unit,
) {
    var attempt by remember { mutableIntStateOf(0) }
    var qrUrl by remember { mutableStateOf<String?>(null) }
    var secondsLeft by remember { mutableIntStateOf(0) }
    var expired by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val currentOnDone by rememberUpdatedState(onDone)

    // QR 발급 + 상태 확인
    LaunchedEffect(attempt) {
        qrUrl = null
        expired = false
        error = null
        if (repository == null) {
            qrUrl = "https://api.ditda.kr/profile?code=preview"
            secondsLeft = 600
            return@LaunchedEffect
        }
        try {
            val qr = repository.issueQr()
            qrUrl = qr.url
            secondsLeft = qr.expiresInSeconds.toInt()
        } catch (e: Exception) {
            Log.e("QrScreen", "QR 발급 실패", e)
            error = "QR을 받지 못했어요.\n인터넷 연결을 확인해 주세요."
            return@LaunchedEffect
        }
        while (true) {
            delay(POLL_INTERVAL_MS)
            val status = try {
                repository.qrStatus()
            } catch (e: Exception) {
                Log.e("QrScreen", "상태 확인 실패", e)
                continue
            }
            when (status) {
                "COMPLETED" -> {
                    val pet = try { repository.loadMyPet() } catch (e: Exception) { null }
                    currentOnDone(pet)
                    return@LaunchedEffect
                }
                "EXPIRED" -> {
                    expired = true
                    return@LaunchedEffect
                }
            }
        }
    }

    // 남은 시간
    LaunchedEffect(qrUrl) {
        while (qrUrl != null && secondsLeft > 0 && !expired) {
            delay(1_000)
            secondsLeft -= 1
        }
        if (qrUrl != null && secondsLeft <= 0) expired = true
    }

    GgDialog(
        title = "휴대폰으로 프로필을 수정하세요",
        message = "휴대폰 카메라로 QR을 스캔해 주세요",
        onDismiss = onCancel,
        showClose = true,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
            val shape = RoundedCornerShape(24.dp)
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .clip(shape)
                    .background(Color.White)
                    .border(2.dp, Divider10, shape),
                contentAlignment = Alignment.Center,
            ) {
                val bitmap = remember(qrUrl) { qrUrl?.let { makeQr(it, 600) } }
                when {
                    error != null -> Text(error ?: "", color = AlertRed, fontSize = 24.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(20.dp))
                    expired -> Text("시간이 지났어요", color = TextSecondary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    bitmap != null -> Image(bitmap, contentDescription = "프로필 입력 QR 코드", modifier = Modifier.size(280.dp))
                    else -> Text("QR 만드는 중…", color = TextSecondary, fontSize = 24.sp)
                }
            }
            if (expired || error != null) {
                GoldButton("새 QR 받기", onClick = { attempt += 1 }, modifier = Modifier.width(320.dp))
            } else {
                Text(
                    text = "남은 시간 ${secondsLeft / 60}:${(secondsLeft % 60).toString().padStart(2, '0')} · 저장하면 자동으로 반영돼요",
                    color = TextSecondary,
                    fontSize = 24.sp,
                )
            }
        }
    }
}

/** 주소 → QR 그림 (zxing) */
private fun makeQr(text: String, size: Int): ImageBitmap? = runCatching {
    val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size, mapOf(EncodeHintType.MARGIN to 1))
    val pixels = IntArray(size * size) { i ->
        if (matrix.get(i % size, i / size)) QR_DARK else android.graphics.Color.WHITE
    }
    Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888).asImageBitmap()
}.getOrNull()
