package com.gatigagae.app.data

/**
 * 음성 요청 해석 (S2-1-05, 기획서 "키워드 규칙" 표 기준)
 * 말한 문장에서 종류·범위·의도를 단어로 찾아요. LLM 없이 단순 비교만 해요.
 */
object VoiceRequestParser {

    // 말한 단어 → 장소 종류. 위에서부터 차례로 찾아요.
    private val categoryWords = listOf(
        "동물병원" to "동물병원",
        "병원" to "동물병원",
        "아파" to "동물병원",
        "휴게소" to "휴게소",
        "쉬어" to "휴게소",
        "카페" to "카페",
        "커피" to "카페",
        "식당" to "식당",
        "음식점" to "식당",
        "밥" to "식당",
        "공원" to "공원",
        "산책" to "공원",
        "캠핑" to "캠핑",
    )

    private val rangeWords = listOf(
        "가는 길" to "가는 길",
        "가는길" to "가는 길",
        "근처" to "근처",
        "주변" to "근처",
    )

    private val intentWords = listOf("찾아줘", "알려줘", "추천", "가자", "가줘")

    fun parse(text: String): VoiceRequest {
        val category = categoryWords.firstOrNull { text.contains(it.first) }?.second
        val range = rangeWords.firstOrNull { text.contains(it.first) }?.second ?: "근처"
        val intent = intentWords.firstOrNull { text.contains(it) }
        return VoiceRequest(text = text, category = category, range = range, intent = intent)
    }

    // 예/아니오 대답 (S1 탑승 확인, S6 "휴게소 찾아드릴까요?")
    private val declineWords = listOf("괜찮", "아니", "됐어", "필요 없", "필요없", "안 할", "안할", "나중에", "혼자")
    private val acceptWords = listOf("응", "네", "예", "좋아", "그래", "찾아줘", "같이", "부탁")

    /** "카페에 곧 도착하니 괜찮아" → true. 거절 단어를 먼저 봐요 ("아니 괜찮아"도 거절) */
    fun isDecline(text: String): Boolean = declineWords.any { text.contains(it) }

    /** "응", "네 찾아줘" → true. 거절 단어가 섞여 있으면 false */
    fun isAccept(text: String): Boolean = !isDecline(text) && acceptWords.any { text.contains(it) }

    /** S3 음성 선택: "첫 번째로 가줘" → 0, "두 번째" → 1, "세 번째" → 2, 못 알아들으면 null */
    fun parseChoice(text: String): Int? {
        val t = text.replace(" ", "")
        return when {
            t.contains("첫번") || t.contains("첫째") || t.contains("1번") -> 0
            t.contains("두번") || t.contains("둘째") || t.contains("2번") -> 1
            t.contains("세번") || t.contains("셋째") || t.contains("3번") -> 2
            else -> null
        }
    }
}
