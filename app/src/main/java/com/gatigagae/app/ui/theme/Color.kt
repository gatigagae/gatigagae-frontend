package com.gatigagae.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 같이가개 색상 — Figma 디자인(현대차공모전) 값 그대로
 * 이름은 예전 코드와 같게 두고 값만 Figma 색으로 바꿨어요. (다른 파일은 안 고쳐도 됨)
 */

// ── 주 색 ──
val GgBlue = Color(0xFF3359D6)          // 주 버튼, 반려견 모드 뱃지, 마이크 버튼, 설정 온도 숫자, 진행 바
val GgBlueLight = Color(0xFFEEF2FF)     // "동반 가능" 뱃지 배경
val GgMicHalo = Color(0xFFDCE4FB)       // 음성 시트 마이크 후광

// ── 상태 색 ──
val GgOrange = Color(0xFFC8701E)        // "알수없음" 뱃지 글자·테두리
val GgOrangeLight = Color(0xFFFFF6EC)   // "알수없음" 뱃지 배경
val GgRed = Color(0xFFE72F2F)           // 인식 실패 마이크, 재탐색 버튼, 2시간 경과, "동반 불가"
val GgRedLight = Color(0xFFFDECEC)      // "동반 불가" 뱃지 배경

// ── 바탕·카드·테두리 ──
val GgBackground = Color(0xFFFFFFFF)    // 화면 배경
val GgCard = Color(0xFFF3F4F6)          // 회색 카드 (프로필·온도·주행·마이크·장소 카드, S1 팝업)
val GgBorder = Color(0xFFE3E5E8)        // 카드 테두리
val GgButtonBorder = Color(0xFFC5CAD1)  // 보조(흰) 버튼·뒤로 버튼 테두리
val GgChipBorder = Color(0xFFD6DAE0)    // 칩 테두리 (기어 칩, 예시 문구 칩, 해석 칩)
val GgPhotoBorder = Color(0xFFB5BAC2)   // 반려견 사진 점선
val GgTrack = Color(0xFFE1E4E8)         // 진행 바 바탕

// ── 글자 ──
val GgTextPrimary = Color(0xFF16181D)   // 기본 글자
val GgTextSecondary = Color(0xFF5B616B) // 보조 글자 (라벨, "듣고 있어요")
val GgTextCaption = Color(0xFF8A9099)   // 더 흐린 글자 (카드 맨 아래 설명, 거리)
val GgChipText = Color(0xFF30343B)      // 칩 글자, 장소 설명
val GgBlack = Color(0xFF16181D)         // 장소 번호 원

val GgScrim = Color(0x99000000)         // 바텀시트 뒤 어두운 배경
