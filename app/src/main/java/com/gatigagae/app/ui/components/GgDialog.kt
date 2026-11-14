package com.gatigagae.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gatigagae.app.R
import com.gatigagae.app.ui.theme.Gold500
import com.gatigagae.app.ui.theme.Surface
import com.gatigagae.app.ui.theme.TextPrimary
import com.gatigagae.app.ui.theme.TextTertiary

/**
 * 같이가개 공통 팝업 (Figma: 2시간 경과, 하차 확인, 프로필 수정 QR 팝업 모양)
 * 흰 바탕 · 모서리 48 · 폭 최대 808 · 위에 아이콘 · 제목 36 굵게 · 설명 26 · 버튼 [골드][회색]
 *
 * - confirmText가 null이면 버튼 없이 문구만 (안내 시작 팝업)
 * - showClose = true면 왼쪽 위 X 버튼 (QR 팝업)
 * - content를 넣으면 설명 아래에 붙어요 (QR 그림 등)
 */
@Composable
fun GgDialog(
    title: String,
    onDismiss: () -> Unit,
    message: String? = null,
    confirmText: String? = null,
    onConfirm: () -> Unit = {},
    dismissText: String? = null,
    iconRes: Int? = null,
    iconTint: Color = Gold500,
    showClose: Boolean = false,
    content: (@Composable () -> Unit)? = null,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val shape = RoundedCornerShape(48.dp)
        Box(
            modifier = Modifier
                .widthIn(max = 808.dp)
                .fillMaxWidth(0.94f)
                .popupShadow(48.dp)
                .clip(shape)
                .background(Surface, shape),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (iconRes != null) {
                    Icon(painterResource(iconRes), contentDescription = null, tint = iconTint, modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(24.dp))
                } else if (showClose) {
                    Spacer(Modifier.height(24.dp))
                }
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 36.sp,
                    lineHeight = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                )
                if (message != null) {
                    Spacer(Modifier.height(16.dp))
                    Text(message, color = TextTertiary, fontSize = 28.sp, lineHeight = 36.sp, textAlign = TextAlign.Center)
                }
                if (content != null) {
                    Spacer(Modifier.height(32.dp))
                    content()
                }
                if (confirmText != null) {
                    Spacer(Modifier.height(40.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        GoldButton(confirmText, onClick = onConfirm, modifier = Modifier.weight(1f))
                        if (dismissText != null) {
                            BasicButton(dismissText, onClick = onDismiss, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            if (showClose) {
                Icon(
                    painterResource(R.drawable.ic_close),
                    contentDescription = "닫기",
                    tint = TextPrimary,
                    modifier = Modifier
                        .padding(28.dp)
                        .size(56.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onDismiss)
                        .padding(4.dp),
                )
            }
        }
    }
}
