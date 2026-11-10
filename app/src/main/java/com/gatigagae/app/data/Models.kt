package com.gatigagae.app.data

/**
 * 앱에서 쓰는 데이터 모양 모음.
 * 지금은 가짜 데이터(FakeData)로 채우고, 나중에 차량 SDK·준석님 서버 값으로 바꿔요.
 */

/** 기어 상태. 지금은 개발용 패널로 바꾸고, 나중에 차량 기어 값(AAOS Car API)으로 교체 */
enum class Gear(val label: String) {
    P("P · 정차 중"),
    D("D · 주행 중"),
}

/** 반려견 프로필 (S2-2에서 입력) */
data class DogProfile(
    val name: String,
    val breed: String = "",
    val age: String = "",
    val note: String = "",
    val preferredTemp: Int = 23, // 반려견 설정 온도(℃)
)

/** 동반 가능 상태 */
enum class PetStatus {
    CONFIRMED,   // 동반 가능 확인됨
    UNKNOWN,     // 정보 없음 (방문 전 확인 필요)
    NOT_ALLOWED, // 동반 불가 (데이터에 불가라고 적혀 있을 때만)
}

/** 추천 장소 1곳. 준석님 서버 응답(JSON)과 같은 모양으로 맞출 예정 */
data class Place(
    val id: String,
    val name: String,
    val category: String,   // 카페, 식당, 공원 …
    val distanceM: Int,     // 현재 위치에서 거리(m)
    val lat: Double,
    val lng: Double,
    val address: String,
    val petStatus: PetStatus,
    val petSummary: String, // 동반 조건 한 줄. 예: "소형견 · 실내 동반 · 목줄 필수"
    val phone: String? = null, // 전화번호 (없으면 null)
)

/** 내비 목적지 동반 여부 판정 결과 (S5) */
data class DestinationCheck(
    val placeName: String,
    val status: PetStatus,
    val detail: String,
)

/** 음성 요청 해석 결과 (S2-1). 예: "근처 카페 찾아줘" → 종류=카페, 범위=근처, 의도=찾아줘 */
data class VoiceRequest(
    val text: String,
    val category: String?,
    val range: String?,
    val intent: String?,
)
