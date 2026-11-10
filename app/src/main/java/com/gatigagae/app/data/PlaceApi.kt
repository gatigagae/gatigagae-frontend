package com.gatigagae.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * 준석님 서버의 장소 검색 API (Swagger: https://api.ditda.kr/swagger-ui/index.html)
 *
 *   GET https://api.ditda.kr/api/v1/places/search
 *       ?text=근처에 콩이랑 갈 수 있는 카페 찾아줘
 *       &latitude=37.55&longitude=126.93
 *       &radius=5000(생략 가능, 최대 20000)&page=0(생략 가능)
 *
 * ※ 노션에는 POST라고 적혀 있지만 Swagger(실제 서버)는 GET + 쿼리 파라미터예요.
 * ※ 라이브러리(Retrofit) 없이 안드로이드 기본 기능(HttpURLConnection + JSONObject)으로 불러요. (Gradle 추가 없음)
 */
object PlaceApi {

    private const val BASE_URL = "https://api.ditda.kr"

    /** 검색 결과 한 번 */
    data class SearchResult(
        val category: String?,   // 화면용 한글 종류 (카페/식당/공원/장소). 서버가 못 알아들으면 null
        val places: List<Place>,
        val hasNext: Boolean,
    )

    /** 실패하면 예외를 던져요 (부르는 쪽에서 try/catch) */
    suspend fun search(
        text: String,
        latitude: Double,
        longitude: Double,
        page: Int = 0,
        radius: Int = 20_000,
    ): SearchResult = withContext(Dispatchers.IO) {
        val query = listOf(
            "text" to text,
            "latitude" to latitude.toString(),
            "longitude" to longitude.toString(),
            "radius" to radius.toString(),
            "page" to page.toString(),
        ).joinToString("&") { (key, value) -> "$key=${URLEncoder.encode(value, "UTF-8")}" }

        val connection = URL("$BASE_URL/api/v1/places/search?$query").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 5_000
            connection.readTimeout = 8_000
            connection.setRequestProperty("Accept", "application/json")

            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
            parse(body, code)
        } finally {
            connection.disconnect()
        }
    }

    /** 공통 응답 형식 {success, code, message, result, error, timestamp} 풀기 */
    internal fun parse(body: String, httpCode: Int = 200): SearchResult {
        val json = JSONObject(body)
        if (!json.optBoolean("success", false)) {
            // 실패: code 예) REQ_400_02, error 예) ["[latitude] 위도는 필수입니다."]
            val detail = json.opt("error")?.toString() ?: ""
            throw IllegalStateException("서버 실패($httpCode) ${json.optString("code")} ${json.optString("message")} $detail")
        }
        val result = json.getJSONObject("result")
        val serverCategory = if (result.isNull("category")) null else result.getString("category")
        val koCategory = categoryToKorean(serverCategory)

        val array = result.optJSONArray("places")
        val places = (0 until (array?.length() ?: 0)).map { i ->
            val p = array!!.getJSONObject(i)
            val conditionsJson = p.optJSONArray("conditions")
            val conditions = (0 until (conditionsJson?.length() ?: 0)).map { conditionsJson!!.getString(it) }
            Place(
                id = p.optString("contentId"),
                name = p.optString("name"),
                category = koCategory ?: "장소",
                distanceM = p.optInt("distance"),
                lat = p.getDouble("latitude"),
                lng = p.getDouble("longitude"),
                address = p.optString("address"),
                // 관광공사 "반려동물 동반 여행" 데이터에서 온 곳이라 전부 "동반 가능"
                petStatus = PetStatus.CONFIRMED,
                petSummary = conditions.joinToString(" · ").ifBlank { "동반 조건 정보가 없어요" },
                phone = if (p.isNull("tel")) null else p.optString("tel").ifBlank { null },
            )
        }
        return SearchResult(koCategory, places, result.optBoolean("hasNext", false))
    }

    /** 서버 종류 → 화면 글자 */
    fun categoryToKorean(category: String?): String? = when (category) {
        "CAFE" -> "카페"
        "RESTAURANT" -> "식당"
        "PARK" -> "공원"
        "ALL" -> "장소"
        else -> null
    }
}
