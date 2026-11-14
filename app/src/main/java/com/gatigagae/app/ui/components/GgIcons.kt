package com.gatigagae.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Figma에 있는 선 아이콘 4개 (마이크, 뒤로, 온도계, 시계)
 * 이미지 파일 없이 코드로 그려요. 색은 쓰는 곳에서 Icon(tint = …)으로 정해요.
 *
 * 쓰는 법: Icon(GgIcons.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(58.dp))
 */
object GgIcons {

    val Mic: ImageVector by lazy {
        lineIcon("Mic") {
            // 마이크 몸통 (둥근 막대)
            moveTo(12f, 2f)
            arcToRelative(3f, 3f, 0f, false, false, -3f, 3f)
            verticalLineToRelative(7f)
            arcToRelative(3f, 3f, 0f, false, false, 6f, 0f)
            verticalLineTo(5f)
            arcToRelative(3f, 3f, 0f, false, false, -3f, -3f)
            close()
            // 받침 곡선
            moveTo(19f, 10f)
            verticalLineToRelative(2f)
            arcToRelative(7f, 7f, 0f, false, true, -14f, 0f)
            verticalLineToRelative(-2f)
            // 기둥
            moveTo(12f, 19f)
            verticalLineToRelative(3f)
        }
    }

    val ChevronLeft: ImageVector by lazy {
        lineIcon("ChevronLeft") {
            moveTo(15f, 18f)
            lineTo(9f, 12f)
            lineTo(15f, 6f)
        }
    }

    val Thermometer: ImageVector by lazy {
        lineIcon("Thermometer") {
            moveTo(14f, 4f)
            verticalLineToRelative(10.54f)
            arcToRelative(4f, 4f, 0f, true, true, -4f, 0f)
            verticalLineTo(4f)
            arcToRelative(2f, 2f, 0f, false, true, 4f, 0f)
            close()
        }
    }

    val Clock: ImageVector by lazy {
        lineIcon("Clock") {
            // 원
            moveTo(12f, 2f)
            arcToRelative(10f, 10f, 0f, true, false, 0f, 20f)
            arcToRelative(10f, 10f, 0f, true, false, 0f, -20f)
            close()
            // 바늘
            moveTo(12f, 6f)
            verticalLineToRelative(6f)
            lineToRelative(4f, 2f)
        }
    }

    /** 24×24 칸에 1.5 두께 선으로 그리는 아이콘 */
    private fun lineIcon(name: String, block: PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = block,
        ).build()
}
