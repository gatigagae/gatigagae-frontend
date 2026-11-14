package com.gatigagae.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * 우리 백엔드 서버의 사용자·반려견 API (Swagger: https://api.ditda.kr/swagger-ui/index.html)
 *
 * 흐름 (11/12 실제 서버로 확인함)
 *  1. 앱 처음 실행: POST /api/v1/users → accessToken (기기에 저장)
 *  2. 프로필 입력: POST /api/v1/pets/me/qr-code → QR에 담을 url (10분 유효)
 *  3. QR 띄운 동안 3초마다: GET /api/v1/pets/me/qr-code → WAITING / COMPLETED / EXPIRED
 *  4. COMPLETED면: GET /api/v1/pets/me → 반려견 정보 (맞춤 온도 temperature 포함)
 *
 * 로그인이 필요한 API는 헤더에 "Authorization: Bearer {accessToken}"
 */
object ProfileApi {

    private const val BASE_URL = "https://api.ditda.kr"

    /** 서버가 success=false로 답했을 때. httpCode 401이면 토큰 문제, code "PET_404_01"이면 반려견 없음 */
    class ServerError(val httpCode: Int, val code: String, message: String) : Exception(message)

    data class QrInfo(val url: String, val expiresInSeconds: Long)

    /** 사용자 등록 → accessToken */
    suspend fun createUser(): String {
        val result = call("POST", "/api/v1/users", token = null)
        return result.getString("accessToken")
    }

    /** 프로필 입력 QR 발급 */
    suspend fun issueQr(token: String): QrInfo {
        val result = call("POST", "/api/v1/pets/me/qr-code", token)
        return QrInfo(result.getString("url"), result.optLong("expiresInSeconds", 600))
    }

    /** QR 입력 상태: "WAITING", "COMPLETED", "EXPIRED" */
    suspend fun qrStatus(token: String): String {
        val result = call("GET", "/api/v1/pets/me/qr-code", token)
        return result.optString("status", "WAITING")
    }

    /** 내 반려견. 아직 등록 안 했으면 null */
    suspend fun myPet(token: String): DogProfile? {
        return try {
            toProfile(call("GET", "/api/v1/pets/me", token))
        } catch (e: ServerError) {
            if (e.code == "PET_404_01") null else throw e
        }
    }

    /** 서버 반려견 응답 → 앱 프로필 */
    internal fun toProfile(pet: JSONObject): DogProfile {
        val note = if (pet.isNull("note")) "" else pet.optString("note")
        val breedName = if (pet.isNull("breedName")) "" else pet.optString("breedName")
        return DogProfile(
            name = pet.optString("name"),
            breed = breedName,
            age = if (pet.isNull("age")) "" else "${pet.optInt("age")}살",
            note = note,
            preferredTemp = pet.optInt("temperature", 22), // 서버가 견종·나이·몸무게로 계산한 맞춤 온도
        )
    }

    /** 공통 응답 {success, code, message, result, error, timestamp}에서 result만 꺼내기 */
    private suspend fun call(method: String, path: String, token: String?): JSONObject = withContext(Dispatchers.IO) {
        val connection = URL(BASE_URL + path).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 5_000
            connection.readTimeout = 8_000
            connection.setRequestProperty("Accept", "application/json")
            if (token != null) connection.setRequestProperty("Authorization", "Bearer $token")
            if (method == "POST") {
                connection.doOutput = true
                connection.outputStream.use { } // 본문 없는 POST
            }
            val httpCode = connection.responseCode
            val stream = if (httpCode in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: "{}"
            val json = JSONObject(body)
            if (!json.optBoolean("success", false)) {
                throw ServerError(httpCode, json.optString("code"), json.optString("message", "서버 오류 ($httpCode)"))
            }
            json.optJSONObject("result") ?: JSONObject("{}")
        } finally {
            connection.disconnect()
        }
    }
}
