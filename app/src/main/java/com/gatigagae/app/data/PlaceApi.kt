package com.gatigagae.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * 우리 백엔드 서버의 장소 검색 API (Swagger: https://api.ditda.kr/swagger-ui/index.html)
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
        // 11/15 서버 변경: 검색 응답은 placeId·name·latitude·longitude·distance만. 나머지는 상세 API(detail)로.
        // (예전 필드 contentId·address·tel·conditions가 와도 읽을 수 있게 opt로)
        val places = (0 until (array?.length() ?: 0)).mapNotNull { i ->
            val p = array!!.getJSONObject(i)
            val lat = p.optDouble("latitude", Double.NaN)
            val lng = p.optDouble("longitude", Double.NaN)
            if (lat.isNaN() || lng.isNaN()) return@mapNotNull null // 좌표가 없으면 길안내를 못 해서 빼요
            val conditionsJson = p.optJSONArray("conditions")
            val conditions = (0 until (conditionsJson?.length() ?: 0)).map { conditionsJson!!.getString(it) }
            Place(
                id = p.optString("placeId").ifBlank { p.optString("contentId") },
                name = p.optString("name"),
                category = categoryToKorean(p.optString("category").ifBlank { null }) ?: koCategory ?: "장소",
                distanceM = p.optInt("distance"),
                lat = lat,
                lng = lng,
                address = p.optString("address"),
                petStatus = PetStatus.CONFIRMED,
                petSummary = conditions.joinToString(" · "),
                phone = if (p.isNull("tel")) null else p.optString("tel").ifBlank { null },
                // 11/15 서버: 검색 결과에도 allowedSize·restrictions가 와요 (기획: 검색 결과 카드에 칩 표시)
                sizeLabel = if (p.isNull("allowedSize")) null else p.optString("allowedSize").ifBlank { null }?.let { sizeToKorean(it) },
                rules = rulesOf(p),
            )
        }
        // 기획 결정: "보조견만 가능" 장소 제외 (서버가 거르지만 예전 응답 대비 앱에서도 한 번 더)
        val petFriendly = places.filterNot { it.petSummary.contains("보조견") }
        return SearchResult(koCategory, petFriendly, result.optBoolean("hasNext", false))
    }

    /**
     * 장소 상세 (11/15 배포): GET /api/v1/places/{placeId}?latitude&longitude
     * → address, tel, businessHours, closedDays, parkingAvailable, allowedSize, restriction
     * 실패하면 null (화면은 검색 결과 정보만으로 보여줘요)
     */
    suspend fun detail(place: Place, latitude: Double, longitude: Double): Place? = withContext(Dispatchers.IO) {
        if (place.id.isBlank() || place.id.toLongOrNull() == null) return@withContext null
        val url = "$BASE_URL/api/v1/places/${place.id}?latitude=$latitude&longitude=$longitude"
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 5_000
            connection.readTimeout = 8_000
            connection.setRequestProperty("Accept", "application/json")
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: return@withContext null
            parseDetail(place, body)
        } catch (e: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    internal fun parseDetail(place: Place, body: String): Place? {
        val json = JSONObject(body)
        if (!json.optBoolean("success", false)) return null
        val r = json.optJSONObject("result") ?: return null
        fun str(key: String): String? = if (r.isNull(key)) null else r.optString(key).ifBlank { null }
        // 11/15 서버 코드화: allowedSize = ALL / SMALL_MEDIUM / SMALL, restrictions = ["LEASH", "MUZZLE", …]
        val size = str("allowedSize")?.let { sizeToKorean(it) }
        val rules = rulesOf(r)
        val oldText = str("restriction") // 예전 글자 응답 대비
        val pet = (listOfNotNull(size) + rules + listOfNotNull(oldText)).joinToString(" · ")
        val hours = listOfNotNull(str("businessHours"), str("closedDays")?.let { "휴무 $it" }).joinToString(" · ")
        return place.copy(
            category = categoryToKorean(str("category")) ?: place.category,
            address = str("address") ?: place.address,
            phone = str("tel") ?: place.phone,
            petSummary = pet.ifBlank { place.petSummary },
            hours = hours.ifBlank { null },
            parking = if (r.isNull("parkingAvailable")) null else r.optBoolean("parkingAvailable", false),
            sizeLabel = size ?: place.sizeLabel,
            rules = rules.ifEmpty { place.rules },
        )
    }

    /** restrictions 배열 → 한글 목록 */
    private fun rulesOf(obj: JSONObject): List<String> {
        val arr = obj.optJSONArray("restrictions") ?: return emptyList()
        return (0 until arr.length()).map { restrictionToKorean(arr.getString(it)) }
    }

    /** 함께 들어갈 수 있는 크기 코드 → 화면 글자 */
    fun sizeToKorean(code: String): String = when (code) {
        "ALL" -> "크기 제한 없음"
        "SMALL_MEDIUM" -> "소형·중형견만"
        "SMALL" -> "소형견만"
        else -> code
    }

    /** 동반 조건 코드 → 화면 글자 (백엔드 노션 "데이터 형식" 표의 뜻 그대로, 나중에 칩 문구). 모르는 코드는 그대로 */
    fun restrictionToKorean(code: String): String = when (code) {
        "LEASH" -> "목줄"
        "POOP_BAG" -> "배변봉투"
        "OUTDOOR_ONLY" -> "실외만 동반"
        "AREA_LIMITED" -> "일부 공간만"
        "CARRIER" -> "케이지"
        "STROLLER" -> "유모차·가방"
        "HOLD" -> "안고 입장"
        "MUZZLE" -> "입마개"
        "MANNER_BELT" -> "매너벨트"
        "VACCINATED" -> "접종 필수"
        "NEUTERED" -> "중성화 필수"
        "REGISTERED" -> "동물등록 필수"
        "NO_DANGEROUS_BREED" -> "맹견 불가"
        "BREED_LIMITED" -> "일부 견종 불가"
        "NO_AGGRESSIVE" -> "공격성 있으면 제한"
        "NO_DISEASE" -> "질환 있으면 불가"
        "NO_SENIOR" -> "노령견 제한"
        "NO_BARKING" -> "짖음 심하면 제한"
        "BRING_SUPPLIES" -> "용품 지참"
        "MAX_PETS" -> "마리 수 제한"
        else -> code
    }

    /** 서버 종류 → 화면 글자 */
    fun categoryToKorean(category: String?): String? = when (category) {
        "CAFE" -> "카페"
        "RESTAURANT" -> "식당"
        "PARK" -> "공원"
        "ATTRACTION" -> "관광지"
        "HOSPITAL" -> "동물병원"
        "PHARMACY" -> "동물약국"
        "GROOMING" -> "미용"
        "PET_HOTEL" -> "펫호텔"
        "ACCOMMODATION" -> "숙소"
        "PET_SHOP" -> "펫숍"
        "ALL" -> "장소"
        else -> null
    }
}
