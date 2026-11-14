package com.gatigagae.app.data

import android.content.Context
import org.json.JSONObject

/**
 * 토큰과 반려견 프로필을 기기에 저장하고, 서버 API를 부르는 곳
 * (기기 저장은 안드로이드 기본 SharedPreferences. 따로 라이브러리 추가 없음)
 *
 * - 토큰이 없으면 처음 한 번 사용자 등록해서 받아 둬요.
 * - 토큰이 이상하다고(401) 하면 새로 등록해서 한 번 더 시도해요.
 * - 마지막으로 받은 프로필을 저장해 두니까, 앱을 켜자마자(서버 응답 전에도) 이름을 부를 수 있어요.
 */
class ProfileRepository(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("gatigagae", Context.MODE_PRIVATE)

    // ── 저장해 둔 프로필 ──

    fun cachedProfile(): DogProfile? {
        val raw = prefs.getString(KEY_PROFILE, null) ?: return null
        return runCatching {
            val json = JSONObject(raw)
            DogProfile(
                name = json.optString("name"),
                breed = json.optString("breed"),
                age = json.optString("age"),
                note = json.optString("note"),
                preferredTemp = json.optInt("preferredTemp", 23),
            )
        }.getOrNull()
    }

    fun saveProfile(profile: DogProfile?) {
        if (profile == null) {
            prefs.edit().remove(KEY_PROFILE).apply()
            return
        }
        val json = JSONObject()
            .put("name", profile.name)
            .put("breed", profile.breed)
            .put("age", profile.age)
            .put("note", profile.note)
            .put("preferredTemp", profile.preferredTemp)
        prefs.edit().putString(KEY_PROFILE, json.toString()).apply()
    }

    // ── 서버 ──

    /** 서버에서 내 반려견 다시 받기 (없으면 null) + 기기에 저장 */
    suspend fun loadMyPet(): DogProfile? {
        val pet = withToken { ProfileApi.myPet(it) }
        saveProfile(pet)
        return pet
    }

    suspend fun issueQr(): ProfileApi.QrInfo = withToken { ProfileApi.issueQr(it) }

    suspend fun qrStatus(): String = withToken { ProfileApi.qrStatus(it) }

    private suspend fun token(): String {
        prefs.getString(KEY_TOKEN, null)?.let { return it }
        val newToken = ProfileApi.createUser()
        prefs.edit().putString(KEY_TOKEN, newToken).apply()
        return newToken
    }

    private suspend fun <T> withToken(block: suspend (String) -> T): T {
        return try {
            block(token())
        } catch (e: ProfileApi.ServerError) {
            if (e.httpCode != 401) throw e
            // 토큰이 만료·무효 → 새로 등록하고 한 번만 다시
            prefs.edit().remove(KEY_TOKEN).apply()
            block(token())
        }
    }

    private companion object {
        const val KEY_TOKEN = "accessToken"
        const val KEY_PROFILE = "profile"
    }
}
