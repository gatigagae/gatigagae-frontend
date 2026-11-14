package com.gatigagae.app.data

/**
 * 가짜 데이터. 화면을 먼저 만들 때 써요.
 * - 장소 목록 → 준석님 서버 API가 나오면 교체
 * - 실내 온도 → Vehicle SDK HVAC 값으로 교체
 * - 음성 문장 → STT 결과로 교체
 */
object FakeData {

    val profile = DogProfile(
        name = "콩이",
        breed = "말티즈",
        age = "3살",
        note = "더위에 약해요",
        preferredTemp = 23,
    )

    const val INDOOR_TEMP = 26.5

    /** STT가 붙기 전까지 "들은 척"할 문장 */
    fun spokenText(dogName: String): String =
        "근처에 ${TextUtils.withRang(dogName)} 갈 수 있는 카페 찾아줘"

    private val distances = listOf(800, 1400, 2300, 3100, 4200, 5600)

    private val summaries = listOf(
        "소형견 · 실내 동반 · 목줄 필수",
        "모든 크기 · 야외 테라스만",
        "소형견 · 이동장 필수",
        "중형견까지 · 실내 동반",
        "모든 크기 · 목줄 필수",
        "소형견 · 실내 동반",
    )

    /** 종류별 가짜 장소. page 0 → 1~3번째, page 1 → 4~6번째, page 2 → 없음 */
    fun places(category: String, page: Int): List<Place> {
        val all = (0 until 6).map { i ->
            Place(
                id = "$category-$i",
                name = "[$category ${'A' + i}]",
                category = category,
                distanceM = distances[i],
                lat = 37.5444 + i * 0.002,
                lng = 127.0374 + i * 0.002,
                address = "서울특별시 성동구 (가짜 주소 ${i + 1})",
                petStatus = PetStatus.CONFIRMED, // 관광공사 동반여행 데이터에서 온 곳이라 전부 "확인됨"
                petSummary = summaries[i],
            )
        }
        return all.drop(page * 3).take(3)
    }

    /** S5 테스트용: 0 = 동반 가능, 1 = 정보 없음, 2 = 동반 불가 */
    val destinationChecks = listOf(
        DestinationCheck(
            placeName = "서울숲",
            status = PetStatus.CONFIRMED,
            detail = "모든 크기 동반 가능 · 목줄 필수",
        ),
        DestinationCheck(
            placeName = "[식당 X]",
            status = PetStatus.UNKNOWN,
            detail = "동반 정보가 등록되어 있지 않아요. 방문 전 확인을 권장해요.",
        ),
        DestinationCheck(
            placeName = "[미술관 Y]",
            status = PetStatus.NOT_ALLOWED,
            detail = "반려동물 동반이 안 되는 곳으로 등록되어 있어요.",
        ),
    )
}
