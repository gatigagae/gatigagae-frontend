package com.gatigagae.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gatigagae.app.ui.theme.GgBorder
import com.gatigagae.app.ui.theme.GgCard
import com.gatigagae.app.ui.theme.GgTextPrimary

/**
 * 같이가개 공통 팝업 (Figma "기존 경로를 변경할까요?" / "{장소명}(으)로 안내합니다" 프레임)
 * Figma 872×415 → 581×277dp, 모서리 30 → 20dp, 글자 50 → 33sp, 버튼 높이 115 → 77dp
 * Figma에서 이 팝업은 "오른쪽 같이가개 앱 위에" 떠요 (내비 쪽 아님).
 *
 * - confirmText가 null이면 버튼 없이 문구만 (안내 시작 팝업). 닫기는 부르는 쪽에서 몇 초 뒤 자동으로.
 * - dismissText가 있으면 왼쪽에 흰 버튼(취소), 오른쪽에 파란 버튼(네)
 */
@Composable
fun GgDialog(
    title: String,
    onDismiss: () -> Unit,
    message: String? = null,
    confirmText: String? = null,
    onConfirm: () -> Unit = {},
    dismissText: String? = null,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false), // 기본 좁은 폭 대신 Figma 폭
    ) {
        val shape = RoundedCornerShape(20.dp)
        Column(
            modifier = Modifier
                .width(581.dp)
                .heightIn(min = 277.dp)
                .clip(shape)
                .background(GgCard, shape)
                .border(1.dp, GgBorder, shape)
                .padding(horizontal = 43.dp, vertical = 17.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(Modifier.height(if (confirmText == null) 0.dp else 50.dp))
            Text(
                text = title,
                color = GgTextPrimary,
                fontSize = 33.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
            if (message != null) {
                Spacer(Modifier.height(10.dp))
                SubText(message, fontSize = 20.sp)
            }
            if (confirmText != null) {
                Spacer(Modifier.height(44.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(22.dp),
                ) {
                    if (dismissText != null) {
                        SecondaryButton(dismissText, onClick = onDismiss, height = 77.dp, fontSize = 30.sp, modifier = Modifier.weight(157f))
                    }
                    PrimaryButton(confirmText, onClick = onConfirm, height = 77.dp, fontSize = 30.sp, modifier = Modifier.weight(324f))
                }
            }
        }
    }
}
