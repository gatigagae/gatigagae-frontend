package com.gatigagae.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 같이가개 색상 — Figma 디자인 시안(Foundation › Colors) 값 그대로
 * "따뜻한 골드를 메인 컬러로 쓰고, 시인성이 필요한 곳은 같은 색조의 더 진한 단계를 씁니다."
 */

// ── 골드 단계 (메인) ──
val Gold100 = Color(0xFFFFF4DB) // 연한 바탕: 반려견 모드 뱃지, 맞춤 설정, 동반 가능 뱃지
val Gold300 = Color(0xFFFFBF47)
val Gold500 = Color(0xFFCC8400) // 메인: 마이크, 주 버튼, 진행 바
val Gold600 = Color(0xFFB37200)
val Gold700 = Color(0xFF996300) // 선택된 번호, "프로필 수정" 글자
val Gold800 = Color(0xFF7A5000) // 연한 바탕 위 글자
val Gold900 = Color(0xFF2E1E00) // 큰 숫자·제목 글자

// ── 바탕·면 ──
val BgPrimary = Color(0xFFF7F8FA)     // 화면 배경
val Surface = Color(0xFFFFFFFF)       // 카드
val SurfaceGray = Color(0xFFF4F5F7)   // 반려견 카드
val ChipGray = Color(0xFFECEDEF)      // 기어 칩, 진행 바 바탕
val ButtonBasic = Color(0x0D131417)   // 회색 버튼 (검정 5%)
val Divider10 = Color(0x1A131417)     // 얇은 구분선
val Divider20 = Color(0x33131417)     // 화면 테두리
val DashedBorder = Color(0xFFC9CDD3)  // "추가" 점선

// ── 글자 ──
val TextPrimary = Color(0xFF131417)
val TextSecondary = Color(0xFF6B6F76)  // 라벨 ("실내 온도")
val TextTertiary = Color(0xA3131417)   // 보조 설명 (검정 64%)

// ── 상태 ──
val SwitchOn = Color(0xFF02C265)  // 공조 스위치
val AlertRed = Color(0xFFD93025)  // 2시간 경과, 인식 실패
val AlertRedLight = Color(0xFFFDECEA)
val Scrim = Color(0x80131417)     // 팝업 뒤 어둡게

// ───────── 예전 이름 (다른 파일이 그대로 컴파일되게 새 색으로 연결) ─────────
val GgBlue = Gold500
val GgBlueLight = Gold100
val GgMicHalo = Color(0xFFF3DDB0)
val GgOrange = Gold700
val GgOrangeLight = Gold100
val GgRed = AlertRed
val GgRedLight = AlertRedLight
val GgBackground = BgPrimary
val GgCard = Surface
val GgBorder = Divider10
val GgButtonBorder = Divider10
val GgChipBorder = Divider10
val GgPhotoBorder = DashedBorder
val GgTrack = ChipGray
val GgTextPrimary = TextPrimary
val GgTextSecondary = TextSecondary
val GgTextCaption = TextTertiary
val GgChipText = TextPrimary
val GgBlack = Gold900
val GgScrim = Scrim
